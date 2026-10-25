package arcana.screens;

import arcana.client.RenderHelper;
import arcana.network.PkScreenRequest;
import arcana.research.Icon;
import arcana.util.MnemonicInventoryData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MnemonicSlot extends Slot{
	
	private final MnemonicInventoryData mnemonics;
	
	public MnemonicSlot(Inventory inventory, int index, int x, int y, MnemonicInventoryData mnemonics){
		super(inventory, index, x, y);
		this.mnemonics = mnemonics;
	}
	
	public @Nullable ItemStack getRememberedShape(){
		return mnemonics.getRememberedShapes().get(getIndex());
	}
	
	@Environment(EnvType.CLIENT)
	public void drawTooltip(DrawContext context, TextRenderer textRenderer, boolean isSpectator, int mouseX, int mouseY){
		if(!hasStack()){
			ItemStack shape = getRememberedShape();
			if(shape != null){
				List<Text> tooltip = new ArrayList<>();
				tooltip.add(Text.translatable("container.arcana.mnemonic.remembers", Text.empty().append(shape.getName()).formatted(shape.getRarity().getFormatting())));
				if(!isSpectator)
					tooltip.add(Text.translatable("container.arcana.mnemonic.clear_hint"));
				context.drawTooltip(textRenderer, tooltip, mouseX, mouseY);
			}
		}
	}
	
	@Environment(EnvType.CLIENT)
	public void drawContent(DrawContext context, TextRenderer textRenderer){
		if(getStack().isEmpty()){
			ItemStack remembered = getRememberedShape();
			if(remembered != null){
				RenderHelper.renderIcon(context, new Icon(remembered), x, y, -100, 1, 1, 0.4f, 0.4f, 0.4f, 1);
				// for showing durability bars or other metadata
				context.drawItemInSlot(textRenderer, remembered, x, y, "");
				context.getMatrices().push();
				context.getMatrices().translate(0, 0, 200);
				final String label = "?";
				context.drawText(textRenderer, label, x + 19 - 2 - textRenderer.getWidth(label), y + 6 + 3, 0x515151, true);
				context.getMatrices().pop();
			}
		}
	}
	
	@Environment(EnvType.CLIENT)
	public void onClick(ScreenHandler handler, MnemonicInventoryData mnemonics, SlotActionType actionType){
		if(!hasStack() && getRememberedShape() != null && actionType == SlotActionType.PICKUP){
			new PkScreenRequest(handler, "clear_slot", getIndex(), 0).sendToServer();
			mnemonics.clearRememberedStack(getIndex());
		}
	}
	
	//
	
	public static boolean insertItem(ScreenHandler handler, MnemonicInventoryData mnemonics, ItemStack stack, int startIndex, int endIndex, boolean fromLast){
		// only insert into slots that match (either existing item or remembered slot)
		
		boolean moved = false;
		int idx = startIndex;
		if(fromLast)
			idx = endIndex - 1;
		
		// first, try to join with existing stacks
		if(stack.isStackable()){
			while(!stack.isEmpty() && (fromLast ? idx >= startIndex : idx < endIndex)){
				Slot slot = handler.slots.get(idx);
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
				Slot slot = handler.slots.get(idx);
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
				Slot slot = handler.slots.get(idx);
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
}