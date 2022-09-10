package arcana.screens;

import arcana.ArcanaRegistry;
import arcana.items.TomeOfSharingItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class KnowledgeableDropperScreenHandler extends ScreenHandler{
	
	private final Inventory inventory;
	
	public KnowledgeableDropperScreenHandler(int syncId, PlayerInventory pInv){
		this(syncId, pInv, new SimpleInventory(9), new SimpleInventory(1));
	}
	
	public KnowledgeableDropperScreenHandler(int syncId,
	                                         PlayerInventory pInv,
	                                         Inventory mainDropperInv,
	                                         Inventory tomeSlotInv){
		super(ArcanaRegistry.KNOWLEDGEABLE_DROPPER_SCREEN_HANDLER, syncId);
		
		inventory = mainDropperInv;
		inventory.onOpen(pInv.player);
		
		for(int row = 0; row < 3; row++)
			for(int col = 0; col < 3; col++)
				addSlot(new Slot(inventory, col + row * 3, 62 + col * 18, 17 + row * 18));
		
		addSlot(new Slot(tomeSlotInv, 0, 134, 35){
			public boolean canInsert(ItemStack stack){
				return stack.getItem() instanceof TomeOfSharingItem;
			}
		});
		
		for(int row = 0; row < 3; ++row)
			for(int col = 0; col < 9; col++)
				addSlot(new Slot(pInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
		
		for(int idx = 0; idx < 9; idx++)
			addSlot(new Slot(pInv, idx, 8 + idx * 18, 142));
	}
	
	public ItemStack transferSlot(PlayerEntity player, int index){
		ItemStack itemStack = ItemStack.EMPTY;
		Slot slot = slots.get(index);
		if(slot.hasStack()){
			ItemStack itemStack2 = slot.getStack();
			itemStack = itemStack2.copy();
			if(index < 10){
				if(!insertItem(itemStack2, 10, 46, true))
					return ItemStack.EMPTY;
			}else if(!insertItem(itemStack2, 0, 10, false))
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
		return inventory.canPlayerUse(player);
	}
	
	public void close(PlayerEntity player){
		super.close(player);
		inventory.onClose(player);
	}
}