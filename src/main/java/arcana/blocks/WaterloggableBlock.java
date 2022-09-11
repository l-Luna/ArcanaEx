package arcana.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Waterloggable;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;

@SuppressWarnings("deprecation")
public class WaterloggableBlock extends Block implements Waterloggable{
	
	public static final BooleanProperty waterlogged = Properties.WATERLOGGED;
	
	public WaterloggableBlock(Settings settings){
		super(settings);
		setDefaultState(getStateManager().getDefaultState().with(waterlogged, false));
	}
	
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder){
		builder.add(waterlogged);
	}
	
	public FluidState getFluidState(BlockState state){
		return state.get(waterlogged) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
	}
	
	public BlockState getPlacementState(ItemPlacementContext ctx){
		return super.getPlacementState(ctx).with(waterlogged, ctx.getWorld().getFluidState(ctx.getBlockPos()).getFluid() == Fluids.WATER);
	}
	
	public BlockState getStateForNeighborUpdate(BlockState state, Direction dir, BlockState neighbor, WorldAccess world, BlockPos pos, BlockPos neighborPos){
		if(state.get(waterlogged))
			world.createAndScheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
		
		return super.getStateForNeighborUpdate(state, dir, neighbor, world, pos, neighborPos);
	}
}