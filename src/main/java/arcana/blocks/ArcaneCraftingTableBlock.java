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
public class ArcaneCraftingTableBlock extends Block implements Waterloggable{
	
	public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
	
	public ArcaneCraftingTableBlock(Settings settings){
		super(settings);
		setDefaultState(stateManager.getDefaultState().with(WATERLOGGED, Boolean.FALSE));
	}
	
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder){
		builder.add(WATERLOGGED);
	}
	
	public FluidState getFluidState(BlockState state){
		return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
	}
	
	public BlockState getPlacementState(ItemPlacementContext ctx){
		FluidState fluidState = ctx.getWorld().getFluidState(ctx.getBlockPos());
		boolean bl = fluidState.getFluid() == Fluids.WATER;
		return super.getPlacementState(ctx).with(WATERLOGGED, bl);
	}
	
	public BlockState getStateForNeighborUpdate(BlockState state, Direction dir, BlockState neighbor, WorldAccess world, BlockPos pos, BlockPos neighborPos){
		if(state.get(WATERLOGGED))
			world.createAndScheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
		
		return super.getStateForNeighborUpdate(state, dir, neighbor, world, pos, neighborPos);
	}
}