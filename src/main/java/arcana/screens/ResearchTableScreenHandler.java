package arcana.screens;

import arcana.ArcanaRegistry;
import arcana.blocks.ResearchTableBlock;
import arcana.blocks.ResearchTableBlockEntity;
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
		addSlot(new Slot(entityInv, 1, 92, 10));
	}
	
	public ItemStack transferSlot(PlayerEntity player, int index){
		Slot slot = slots.get(index);
		return slot.hasStack() ? slot.getStack() : ItemStack.EMPTY;
	}
	
	public boolean canUse(PlayerEntity player){
		return true;
	}
}