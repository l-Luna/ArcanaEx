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
		return context.getWorld().setBlockState(context.getBlockPos(), state.with(left, true), f)
		    && context.getWorld().setBlockState(context.getBlockPos().offset(context.getPlayerFacing().rotateYClockwise()), state.with(left, false), f);
	}
}