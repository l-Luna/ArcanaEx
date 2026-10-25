package arcana.screens;

import arcana.ArcanaRegistry;
import arcana.api.RequestableScreenHandler;
import arcana.client.RenderHelper;
import arcana.network.PkScreenRequest;
import arcana.research.Icon;
import arcana.util.ArrayInventory;
import arcana.util.InventoryUtil;
import arcana.util.MnemonicInventoryData;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static arcana.Arcana.arcId;

@Environment(EnvType.CLIENT)
public class MnemonicChestScreen extends HandledScreen<MnemonicChestScreen.Handler>{
	
	private static final Identifier TEXTURE = arcId("textures/gui/container/mnemonic_chest.png");
	
	public MnemonicChestScreen(Handler handler, PlayerInventory inventory, Text title){
		super(handler, inventory, title);
	}
	
	protected void init(){
		super.init();
		backgroundHeight = 169;
		
		titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2;
		titleY -= 2;
		y = (height - backgroundHeight) / 2;
	}
	
	public void render(DrawContext ctx, int mouseX, int mouseY, float delta){
		renderBackground(ctx, mouseX, mouseY, delta);
		super.render(ctx, mouseX, mouseY, delta);
		drawMouseoverTooltip(ctx, mouseX, mouseY);
		
		if (focusedSlot instanceof MnemonicSlot mnSlot
				&& !mnSlot.hasStack()
				&& handler.getCursorStack().isEmpty()
				/*&& !player.isSpectator()*/){
			ItemStack shape = mnSlot.getRememberedShape();
			if(shape != null){
				ctx.drawTooltip(textRenderer, List.of(
						Text.translatable("container.arcana.mnemonic.remembers", Text.empty().append(shape.getName()).formatted(shape.getRarity().getFormatting())),
						Text.translatable("container.arcana.mnemonic.clear_hint")
				), mouseX, mouseY);
			}
		}
	}
	
	protected void drawBackground(DrawContext ctx, float delta, int mouseX, int mouseY){
		int x = (width - backgroundWidth) / 2;
		int y = (height - backgroundHeight) / 2;
		RenderSystem.setShaderColor(1, 1, 1, 1);
		ctx.drawTexture(TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight);
	}
	
	protected void drawForeground(DrawContext ctx, int mouseX, int mouseY){
		ctx.drawText(textRenderer, title, titleX, titleY, 0xC0C0C0, false);
	}
	
	protected void drawSlot(DrawContext context, Slot slot){
		if(slot instanceof MnemonicSlot mnSlot && mnSlot.getStack().isEmpty()){
			ItemStack remembered = mnSlot.getRememberedShape();
			if(remembered != null){
				int seed = slot.x + slot.y * this.backgroundWidth;
				RenderHelper.renderIcon(context, new Icon(remembered), mnSlot.x, mnSlot.y, -100, 1, 1, 0.4f, 0.4f, 0.4f, 1);
				// for showing durability bars or other metadata
				context.drawItemInSlot(textRenderer, remembered, mnSlot.x, mnSlot.y, "");
				context.getMatrices().push();
				context.getMatrices().translate(0, 0, 200);
				final String label = "?";
				context.drawText(textRenderer, Text.literal(label), mnSlot.x + 19 - 2 - textRenderer.getWidth(label), mnSlot.y + 6 + 3, 0x515151, true);
				context.getMatrices().pop();
			}
		}
		super.drawSlot(context, slot);
	}
	
	protected void onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType){
		if(slot instanceof MnemonicSlot mnSlot
				&& !mnSlot.hasStack()
				&& mnSlot.getRememberedShape() != null
				/*&& !player.isSpectator()*/
				&& actionType == SlotActionType.PICKUP){
			new PkScreenRequest(handler, "clear_slot", mnSlot.getIndex(), 0).sendToServer();
			handler.mnemonics.clearRememberedStack(mnSlot.getIndex());
		}
		
		super.onMouseClick(slot, slotId, button, actionType);
	}
	
	public static class Handler extends ScreenHandler implements RequestableScreenHandler{
		
		private final MnemonicInventoryData mnemonics;
		private final ArrayInventory inventory;
		
		public Handler(int syncId, PlayerInventory pInv, MnemonicInventoryData data){
			this(syncId, pInv, new ArrayInventory(27), data);
			// on the client side, `data` is a copy not attached to any inventory, so attach it to our local copy
			inventory.addListener(data);
		}
		
		public Handler(int syncId, PlayerInventory pInv, ArrayInventory inventory, MnemonicInventoryData mnemonics){
			super(ArcanaRegistry.MNEMONIC_CHEST_SCREEN_HANDLER, syncId);
			this.inventory = inventory;
			this.mnemonics = mnemonics;
			
			for(int row = 0; row < 3; ++row)
				for(int col = 0; col < 9; col++)
					addSlot(new MnemonicSlot(inventory, col + row * 9, 8 + col * 18, 19 + row * 18, mnemonics));
			
			for(int row = 0; row < 3; ++row)
				for(int col = 0; col < 9; col++)
					addSlot(new Slot(pInv, col + row * 9 + 9, 8 + col * 18, 87 + row * 18));
			
			for(int idx = 0; idx < 9; idx++)
				addSlot(new Slot(pInv, idx, 8 + idx * 18, 145));
		}
		
		public ItemStack quickMove(PlayerEntity player, int slot){
			return InventoryUtil.quickMove(this, inventory, slot);
		}
		
		public boolean insertItem(ItemStack stack, int startIndex, int endIndex, boolean fromLast){
			// only insert into slots that match (either existing item or remembered slot)
			
			boolean moved = false;
			int idx = startIndex;
			if(fromLast)
				idx = endIndex - 1;
			
			// first, try to join with existing stacks
			if(stack.isStackable()){
				while(!stack.isEmpty() && (fromLast ? idx >= startIndex : idx < endIndex)){
					Slot slot = slots.get(idx);
					ItemStack stackThere = slot.getStack();
					if(!stackThere.isEmpty() && ItemStack.areItemsAndComponentsEqual(stack, stackThere)){
						int newCount = stackThere.getCount() + stack.getCount();
						int maxCount = slot.getMaxItemCount(stackThere);
						if(newCount <= maxCount){
							stack.setCount(0);
							stackThere.setCount(newCount);
							slot.markDirty();
							moved = true;
						}else if(stackThere.getCount() < maxCount){
							stack.decrement(maxCount - stackThere.getCount());
							stackThere.setCount(maxCount);
							slot.markDirty();
							moved = true;
						}
					}
					
					if(fromLast)
						idx--;
					else
						idx++;
				}
			}
			
			// otherwise, insert into remembered slots
			if(!stack.isEmpty()){
				if(fromLast)
					idx = endIndex - 1;
				else
					idx = startIndex;
				
				while(fromLast ? idx >= startIndex : idx < endIndex){
					Slot slot = slots.get(idx);
					ItemStack stackThere = slot.getStack();
					// only difference from vanilla
					if(stackThere.isEmpty() && slot.canInsert(stack) && (!(slot instanceof MnemonicSlot) || mnemonics.filter(idx, stack))){
						int maxCount = slot.getMaxItemCount(stack);
						slot.setStack(stack.split(Math.min(stack.getCount(), maxCount)));
						slot.markDirty();
						moved = true;
						break;
					}
					
					if(fromLast)
						idx--;
					else
						idx++;
				}
			}
			
			// otherwise, insert into empty slots (similar to above)
			if(!stack.isEmpty()){
				if(fromLast)
					idx = endIndex - 1;
				else
					idx = startIndex;
				
				while(fromLast ? idx >= startIndex : idx < endIndex){
					Slot slot = slots.get(idx);
					ItemStack stackThere = slot.getStack();
					// only difference from vanilla
					if(stackThere.isEmpty() && slot.canInsert(stack) && !(slot instanceof MnemonicSlot && mnemonics.getRememberedShapes().containsKey(idx))){
						int maxCount = slot.getMaxItemCount(stack);
						slot.setStack(stack.split(Math.min(stack.getCount(), maxCount)));
						slot.markDirty();
						moved = true;
						break;
					}
					
					if(fromLast)
						idx--;
					else
						idx++;
				}
			}
			
			return moved;
		}
		
		public boolean canUse(PlayerEntity player){
			return true;
		}
		
		public void handleRequest(@Nullable String tag, int index, int data){
			if("clear_slot".equals(tag))
				mnemonics.clearRememberedStack(index);
		}
	}
}