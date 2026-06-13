package arcana.screens;

import arcana.ArcanaRegistry;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
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

import static arcana.Arcana.arcId;

public class DistilleryPathfinderScreen extends HandledScreen<DistilleryPathfinderScreen.Handler>{
	
	private static final Identifier texture = arcId("textures/gui/container/distillery_pathfinder.png");
	
	public DistilleryPathfinderScreen(Handler handler, PlayerInventory inventory, Text title){
		super(handler, inventory, title);
	}
	
	protected void init(){
		super.init();
		titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2;
	}
	
	protected void drawBackground(DrawContext ctx, float delta, int mouseX, int mouseY){
		RenderSystem.setShaderColor(1, 1, 1, 1);
		ctx.drawTexture(texture, x, y, 0, 0, backgroundWidth, backgroundHeight);
		
		if(handler.getBurnTime() > 0 && handler.getMaxBurnTime() > 0){
			int pixels = (int)Math.ceil(13 * handler.getBurnTime() / (double)handler.getMaxBurnTime());
			ctx.drawTexture(texture, x + 57, y + 37 + (13 - pixels), 176, 12 - pixels, 14, pixels + 1);
		}
		
		if(handler.getProgress() > 0){
			float overtime = handler.getProgress() + delta - 20*8;
			if(overtime > 0){
				float redness = (float)(Math.max(Math.sin(overtime / 4f), 0) * (overtime / 100f));
				RenderSystem.setShaderColor(1, 1 - redness, 1 - redness, 1);
			}
			
			// TODO: slow down smoothly
			int pixels = (int)Math.min(Math.ceil(22 * handler.getProgress() / (20 * 8d /* seconds */)), 21 /* limit */);
			ctx.drawTexture(texture, x + 80, y + 35, 177, 14, pixels, 16);
		}
	}
	
	public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
		renderBackground(ctx, mouseX, mouseY, delta);
		super.render(ctx, mouseX, mouseY, delta);
		drawMouseoverTooltip(ctx, mouseX, mouseY);
	}
	
	public static class Handler extends ScreenHandler{
		
		// "main" material inventory
		private final Inventory inventory;
		// [burn time, max burn time, progress]
		private final PropertyDelegate props;
		
		public Handler(int syncId, PlayerInventory pInv){
			this(syncId, pInv, new SimpleInventory(1), new SimpleInventory(1), new ArrayPropertyDelegate(3));
		}
		
		public Handler(int syncId, PlayerInventory pInv, Inventory material, Inventory fuel, PropertyDelegate props){
			super(ArcanaRegistry.DISTILLERY_PATHFINDER_SCREEN_HANDLER, syncId);
			
			this.props = props;
			
			inventory = material;
			inventory.onOpen(pInv.player);
			
			// to-melt slot
			addSlot(new Slot(material, 0, 56, 17));
			
			// fuel slot
			addSlot(new Slot(fuel, 0, 56, 53){
				public boolean canInsert(ItemStack stack){
					return AbstractFurnaceBlockEntity.canUseAsFuel(stack);
				}
			});
			
			// player inventory slots
			for(int row = 0; row < 3; ++row)
				for(int col = 0; col < 9; col++)
					addSlot(new Slot(pInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
			
			for(int idx = 0; idx < 9; idx++)
				addSlot(new Slot(pInv, idx, 8 + idx * 18, 142));
			
			addProperties(props);
		}
		
		public int getBurnTime(){
			return props.get(0);
		}
		
		public int getMaxBurnTime(){
			return props.get(1);
		}
		
		public int getProgress(){
			return props.get(2);
		}
		
		public ItemStack quickMove(PlayerEntity player, int index){
			// TODO: quick move
			return ItemStack.EMPTY;
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