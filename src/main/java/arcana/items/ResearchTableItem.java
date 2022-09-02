package arcana.items;

import arcana.ArcanaRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;

import static arcana.blocks.ResearchTableBlock.left;

public class ResearchTableItem extends BlockItem{
	
	public ResearchTableItem(Settings settings){
		super(ArcanaRegistry.RESEARCH_TABLE, settings);
	}
	
	protected boolean place(ItemPlacementContext context, BlockState state){
		// only handle the research table
		var f = Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD;
		var pos = context.getBlockPos();
		var offset = context.getPlayerFacing().rotateYClockwise();
		
		// check if the right block is replaceable
		// not that context.canPlace() offsets by hit side, which is not what we want, so we use state.canReplace
		ItemPlacementContext rightCtx = ItemPlacementContext.offset(context, pos.offset(offset), offset);
		if(!context.getWorld().getBlockState(pos.offset(offset)).canReplace(rightCtx)){
			// if not, we check if we can shift the whole table to the left
			var opp = offset.getOpposite();
			pos = pos.offset(opp);
			context = ItemPlacementContext.offset(context, pos, opp);
			if(!context.getWorld().getBlockState(pos).canReplace(context))
				// if we have solid blocks on both sides, we can't place at all
				return false;
		}
		
		return context.getWorld().setBlockState(pos, state.with(left, true), f)
		    && context.getWorld().setBlockState(pos.offset(offset), state.with(left, false), f);
	}
}