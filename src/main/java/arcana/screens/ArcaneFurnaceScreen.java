package arcana.screens;

import arcana.ArcanaRegistry;
import arcana.ArcanaTags;
import arcana.blocks.be.ArcaneFurnaceBlockEntity;
import arcana.util.InventoryUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper.Argb;

import static arcana.Arcana.arcId;

public class ArcaneFurnaceScreen extends HandledScreen<ArcaneFurnaceScreen.Handler>{
	
	private static final Identifier texture = arcId("textures/gui/container/arcane_furnace.png");
	
	public ArcaneFurnaceScreen(Handler handler, PlayerInventory inventory, Text title){
		super(handler, inventory, title);
	}
	
	protected void init(){
		backgroundHeight = 163;
		super.init();
		titleY = (backgroundWidth - textRenderer.getWidth(title)) / 2;
	}
	
	protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY){
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1, 1, 1, 1);
		RenderSystem.setShaderTexture(0, texture);
		drawTexture(matrices, x, y, 0, 0, backgroundWidth, backgroundHeight);
		
		if(handler.getBurnTime() > 0 && handler.getMaxBurnTime() > 0){
			int pixels = (int)Math.ceil(14 * handler.getBurnTime() / (double)handler.getMaxBurnTime());
			drawTexture(matrices, x + 33, y + 30 + (14 - pixels), 176, 14 - pixels, 13, pixels);
		}
		
		if(handler.getSubstrateAmount() > 0 && handler.getMaxSubstrateAmount() > 0){
			int pixels = (int)Math.ceil(12 * handler.getSubstrateAmount() / (double)handler.getMaxSubstrateAmount());
			int colour = handler.getSubstrateColour();
			RenderSystem.setShaderColor(Argb.getRed(colour) / 255f, Argb.getGreen(colour) / 255f, Argb.getBlue(colour) / 255f, 1f);
			drawTexture(matrices, x + 57, y + 32 + (12 - pixels), 202, 12 - pixels, 13, pixels);
		}
		RenderSystem.setShaderColor(1, 1, 1, 1);
		
		if(handler.getProgress() > 0 && handler.getMaxProgress() > 0){
			int pixels = (int)Math.ceil(20 * handler.getProgress() / (double)handler.getMaxProgress());
			drawTexture(matrices, x + 79, y + 30, 215, 0, pixels, 13);
		}
		
		if(handler.getAspectTotal() > 0){
			int pixels = (int)Math.ceil(52 * handler.getAspectTotal() / (double)ArcaneFurnaceBlockEntity.capacity);
			drawTexture(matrices, x + 142, y + 11 + (52 - pixels), 176, 14 + (52 - pixels), 16, pixels);
		}
	}
	
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		renderBackground(matrices);
		super.render(matrices, mouseX, mouseY, delta);
		drawMouseoverTooltip(matrices, mouseX, mouseY);
	}
	
	protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY){
		// no-op - don't draw label
	}
	
	public static class Handler extends ScreenHandler{
		
		// main inventory
		private final Inventory inventory;
		// [burn time, max burn time, substrate amount, max substrate amount, substrate colour, progress, max progress, aspect total]
		private final PropertyDelegate props;
		
		public Handler(int syncId, PlayerInventory pInv){
			this(syncId, pInv, new SimpleInventory(4), new ArrayPropertyDelegate(8));
		}
		
		public Handler(int syncId, PlayerInventory pInv, Inventory inventory, PropertyDelegate props){
			super(ArcanaRegistry.ARCANE_FURNACE_SCREEN_HANDLER, syncId);
			
			this.props = props;
			
			this.inventory = inventory;
			inventory.onOpen(pInv.player);
			
			// to-melt slot
			addSlot(new Slot(inventory, 0, 43, 10));
			
			// fuel slot
			addSlot(new Slot(inventory, 1, 31, 48){
				public boolean canInsert(ItemStack stack){
					return AbstractFurnaceBlockEntity.canUseAsFuel(stack);
				}
			});
			
			// substrate slot
			addSlot(new Slot(inventory, 2, 55, 48){
				public boolean canInsert(ItemStack stack){
					return stack.isIn(ArcanaTags.SUBSTRATES);
				}
			});
			
			// husks slot
			addSlot(new Slot(inventory, 3, 107, 28){
				public boolean canInsert(ItemStack stack){
					return false;
				}
			});
			
			// player inventory slots
			for(int row = 0; row < 3; ++row)
				for(int col = 0; col < 9; col++)
					addSlot(new Slot(pInv, col + row * 9 + 9, 8 + col * 18, 81 + row * 18));
			
			for(int idx = 0; idx < 9; idx++)
				addSlot(new Slot(pInv, idx, 8 + idx * 18, 139));
			
			addProperties(props);
		}
		
		public int getBurnTime(){
			return props.get(0);
		}
		
		public int getMaxBurnTime(){
			return props.get(1);
		}
		
		public int getSubstrateAmount(){
			return props.get(2);
		}
		
		public int getMaxSubstrateAmount(){
			return props.get(3);
		}
		
		public int getSubstrateColour(){
			return props.get(4);
		}
		
		public int getProgress(){
			return props.get(5);
		}
		
		public int getMaxProgress(){
			return props.get(6);
		}
		
		public int getAspectTotal(){
			return props.get(7);
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