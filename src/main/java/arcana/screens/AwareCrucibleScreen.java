package arcana.screens;

import arcana.ArcanaRegistry;
import arcana.util.InventoryUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import static arcana.Arcana.arcId;

@Environment(EnvType.CLIENT)
public class AwareCrucibleScreen extends HandledScreen<AwareCrucibleScreen.Handler>{
	
	private static final Identifier TEXTURE = arcId("textures/gui/container/aware_crucible.png");
	
	public AwareCrucibleScreen(Handler handler, PlayerInventory inventory, Text title){
		super(handler, inventory, title);
	}
	
	protected void init(){
		backgroundHeight = 131;
		super.init();
	}
	
	protected void drawBackground(DrawContext ctx, float delta, int mouseX, int mouseY){
		RenderSystem.setShaderColor(1, 1, 1, 1);
		ctx.drawTexture(TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight);
	}
	
	public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
		renderBackground(ctx, mouseX, mouseY, delta);
		super.render(ctx, mouseX, mouseY, delta);
		drawMouseoverTooltip(ctx, mouseX, mouseY);
	}
	
	protected void drawForeground(DrawContext matrices, int mouseX, int mouseY){
		// no-op - don't draw label
	}
	
	public static class Handler extends ScreenHandler{
		
		private final Inventory inventory;
		
		public Handler(int syncId, PlayerInventory pInv){
			this(syncId, pInv, new SimpleInventory(1));
		}
		
		public Handler(int syncId, PlayerInventory pInv, Inventory inventory){
			super(ArcanaRegistry.AWARE_CRUCIBLE_SCREEN_HANDLER, syncId);
			this.inventory = inventory;
			inventory.onOpen(pInv.player);
			
			// inventory slot
			addSlot(new Slot(inventory, 0, 80, 14));
			
			// player inventory slots
			for(int row = 0; row < 3; ++row)
				for(int col = 0; col < 9; col++)
					addSlot(new Slot(pInv, col + row * 9 + 9, 8 + col * 18, 49 + row * 18));
			
			for(int idx = 0; idx < 9; idx++)
				addSlot(new Slot(pInv, idx, 8 + idx * 18, 107));
		}
		
		public ItemStack quickMove(PlayerEntity player, int index){
			return InventoryUtil.quickMove(this, inventory, index);
		}
		
		public boolean canUse(PlayerEntity player){
			return inventory.canPlayerUse(player);
		}
		
		public void onClosed(PlayerEntity player){
			super.onClosed(player);
			inventory.onClose(player);
		}
	}
}