package arcana.screens;

import arcana.ArcanaRegistry;
import arcana.blocks.be.CrystallizationPressBlockEntity;
import arcana.util.InventoryUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;

import static arcana.Arcana.arcId;

public class CrystallizationPressScreen extends HandledScreen<CrystallizationPressScreen.Handler>{
	
	private static final Identifier texture = arcId("textures/gui/container/crystallization_press.png");
	
	public CrystallizationPressScreen(Handler handler, PlayerInventory inventory, Text title){
		super(handler, inventory, title);
	}
	
	protected void init(){
		super.init();
		titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2;
	}
	
	protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY){
		RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
		RenderSystem.setShaderColor(1, 1, 1, 1);
		RenderSystem.setShaderTexture(0, texture);
		drawTexture(matrices, x, y, 0, 0, backgroundWidth, backgroundHeight);
		
		if(handler.getQuartzAmount() > 0){
			int pixels = (int)Math.ceil(18 * handler.getQuartzAmount() / (double)CrystallizationPressBlockEntity.maxQuartzLevel);
			drawTexture(matrices, x + 44, y + 44 + (18 - pixels), 176, 18 - pixels, 4, pixels);
		}
		
		if(handler.getProgress() > 0){
			int pixels = (int)Math.ceil(22 * handler.getProgress() / (double)CrystallizationPressBlockEntity.maxProgress);
			drawTexture(matrices, x + 97, y + 33, 180, 0, pixels, 13);
		}
		
		if(handler.getEssentiaAmount() > 0){
			int pixels = (int)Math.ceil(52 * handler.getEssentiaAmount() / (double)CrystallizationPressBlockEntity.capacity);
			int colour = handler.getEssentiaColour();
			RenderSystem.setShaderColor(ColorHelper.Argb.getRed(colour) / 255f, ColorHelper.Argb.getGreen(colour) / 255f, ColorHelper.Argb.getBlue(colour) / 255f, 1f);
			drawTexture(matrices, x + 67, y + 13 + (52 - pixels), 202, 52 - pixels, 16, pixels);
		}
	}
	
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta){
		renderBackground(matrices);
		super.render(matrices, mouseX, mouseY, delta);
		drawMouseoverTooltip(matrices, mouseX, mouseY);
	}
	
	protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY){
		// no-op, no labels
	}
	
	public static class Handler extends ScreenHandler{
		
		// [quartz amount, essentia amount, essentia colour, progress]
		private final PropertyDelegate propertyDelegate;
		private final Inventory inventory;
		
		public Handler(int syncId, PlayerInventory pInv){
			this(syncId, pInv, new SimpleInventory(2), new ArrayPropertyDelegate(4));
		}
		
		public Handler(int syncId, PlayerInventory pInv, Inventory inventory, PropertyDelegate properties){
			super(ArcanaRegistry.CRYSTALLIZATION_PRESS_SCREEN_HANDLER, syncId);
			this.propertyDelegate = properties;
			this.inventory = inventory;
			
			this.inventory.onOpen(pInv.player);
			
			// quartz slot
			addSlot(new Slot(inventory, 0, 22, 31){
				public boolean canInsert(ItemStack stack){
					return stack.isOf(Items.QUARTZ);
				}
			});
			
			// output slot
			addSlot(new Slot(inventory, 1, 137, 31){
				public boolean canInsert(ItemStack stack){
					return false;
				}
			});
			
			// player inventory slots
			for(int row = 0; row < 3; ++row)
				for(int col = 0; col < 9; col++)
					addSlot(new Slot(pInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
			
			for(int idx = 0; idx < 9; idx++)
				addSlot(new Slot(pInv, idx, 8 + idx * 18, 142));
			
			addProperties(propertyDelegate);
		}
		
		public int getQuartzAmount(){
			return propertyDelegate.get(0);
		}
		
		public int getEssentiaAmount(){
			return propertyDelegate.get(1);
		}
		
		public int getEssentiaColour(){
			return propertyDelegate.get(2);
		}
		
		public int getProgress(){
			return propertyDelegate.get(3);
		}
		
		public ItemStack transferSlot(PlayerEntity player, int index){
			return InventoryUtil.transferSlot(this, inventory, index);
		}
		
		public boolean canUse(PlayerEntity player){
			return inventory.canPlayerUse(player);
		}
		
		public void close(PlayerEntity player){
			super.close(player);
			inventory.onClose(player);
		}
	}
}