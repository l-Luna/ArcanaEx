package arcana.screens;

import arcana.ArcanaRegistry;
import arcana.blocks.ResearchTableBlock;
import arcana.blocks.ResearchTableBlockEntity;
import arcana.items.ResearchNotesItem;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;

public class ResearchTableScreenHandler extends ScreenHandler{
	
	public ResearchTableScreenHandler(int syncId, PlayerInventory playerInv){
		this(syncId, playerInv, ScreenHandlerContext.EMPTY);
	}
	
	public ResearchTableScreenHandler(int syncId, PlayerInventory inv, ScreenHandlerContext ctx){
		super(ArcanaRegistry.RESEARCH_TABLE_SCREEN_HANDLER, syncId);
		
		// player inventory
		for(int i = 0; i < 3; ++i)
			for(int j = 0; j < 9; ++j)
				addSlot(new Slot(inv, j + i * 9 + 9, 119 + j * 18, 181 + i * 18));
		
		// compact hotbar
		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 3; j++)
				addSlot(new Slot(inv, j + i * 3, 59 + j * 18, 181 + i * 18));
		
		Inventory entityInv = ctx.get((world, pos) -> {
			BlockState state = world.getBlockState(pos);
			if(!state.get(ResearchTableBlock.left))
				pos = pos.offset(state.get(ResearchTableBlock.facing));
			return ((ResearchTableBlockEntity)world.getBlockEntity(pos)).inventory;
		}).orElse(new SimpleInventory(2));
		
		addSlot(new Slot(entityInv, 0, 74, 10));
		addSlot(new Slot(entityInv, 1, 92, 10){
			public boolean canInsert(ItemStack stack){
				return stack.getItem() instanceof ResearchNotesItem;
			}
		});
	}
	
	public ItemStack transferSlot(PlayerEntity player, int index){
		ItemStack itemStack = ItemStack.EMPTY;
		Slot slot = slots.get(index);
		if(slot != null && slot.hasStack()){
			ItemStack itemStack2 = slot.getStack();
			itemStack = itemStack2.copy();
			if(index >= 36){
				if(!insertItem(itemStack2, 0, 36, true))
					return ItemStack.EMPTY;
			}else if(!insertItem(itemStack2, 36, 38, true))
				return ItemStack.EMPTY;
			
			if(itemStack2.isEmpty())
				slot.setStack(ItemStack.EMPTY);
			else
				slot.markDirty();
			
			if(itemStack2.getCount() == itemStack.getCount())
				return ItemStack.EMPTY;
			
			slot.onTakeItem(player, itemStack2);
		}
		
		return itemStack;
	}
	
	public boolean canUse(PlayerEntity player){
		return true;
	}
}