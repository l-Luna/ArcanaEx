package arcana.screens;

import arcana.ArcanaRegistry;
import arcana.items.FocusItem;
import arcana.util.ArrayInventory;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import static arcana.Arcana.arcId;

@Environment(EnvType.CLIENT)
public class FocusPouchScreen extends HandledScreen<FocusPouchScreen.Handler>{
	
	private static final Identifier texture = arcId("textures/gui/container/focus_pouch.png");
	
	public FocusPouchScreen(Handler handler, PlayerInventory inventory, Text title){
		super(handler, inventory, title);
		backgroundWidth = 180;
		backgroundHeight = 174;
	}
	
	protected void init(){
		super.init();
		titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2;
		titleY -= 2;
	}
	
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta){
		renderBackground(matrices);
		super.render(matrices, mouseX, mouseY, delta);
		drawMouseoverTooltip(matrices, mouseX, mouseY);
	}
	
	protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY){
		RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
		RenderSystem.setShaderColor(1, 1, 1, 1);
		RenderSystem.setShaderTexture(0, texture);
		drawTexture(matrices, x, y, 0, 0, backgroundWidth, backgroundHeight);
		
		// TODO: last focus location
	}
	
	protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY){
		textRenderer.draw(matrices, title, titleX, titleY, 0xC0C0C0);
	}
	
	public static class Handler extends ScreenHandler{
		
		private static class FocusSlot extends Slot{
			public FocusSlot(Inventory inventory, int index, int x, int y){
				super(inventory, index, x, y);
			}
			
			public boolean canInsert(ItemStack stack){
				return stack.getItem() instanceof FocusItem;
			}
		}
		
		private final ArrayInventory inventory;
		
		public Handler(int syncId, PlayerInventory pInv){
			this(syncId, pInv, new ArrayInventory(9*3));
		}
		
		public Handler(int syncId, PlayerInventory pInv, ArrayInventory inventory){
			super(ArcanaRegistry.FOCUS_POUCH_SCREEN_HANDLER, syncId);
			this.inventory = inventory;
			
			// quick access slots
			for(int idx = 0; idx < 9; idx++)
				addSlot(new FocusSlot(inventory, idx, 10 + idx * 18, 14));
			
			// storage slots
			for(int row = 0; row < 2; ++row)
				for(int col = 0; col < 9; col++)
					addSlot(new FocusSlot(inventory, col + row * 9 + 9, 10 + col * 18, 41 + row * 18));
			
			// player inventory slots
			for(int row = 0; row < 3; ++row)
				for(int col = 0; col < 9; col++)
					addSlot(new Slot(pInv, col + row * 9 + 9, 10 + col * 18, 92 + row * 18));
			
			for(int idx = 0; idx < 9; idx++)
				addSlot(new Slot(pInv, idx, 10 + idx * 18, 150));
		}
		
		public ItemStack transferSlot(PlayerEntity player, int index){
			// TODO: quick move
			return ItemStack.EMPTY;
		}
		
		public boolean canUse(PlayerEntity player){
			return true;
		}
	}
}