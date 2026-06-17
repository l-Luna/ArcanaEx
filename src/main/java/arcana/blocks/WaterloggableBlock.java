package arcana.blocks;

import com.mojang.serialization.MapCodec;
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
	
	private static final MapCodec<WaterloggableBlock> CODEC = createCodec(WaterloggableBlock::new);
	
	public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
	
	public WaterloggableBlock(Settings settings){
		super(settings);
		setDefaultState(getStateManager().getDefaultState().with(WATERLOGGED, false));
	}
	
	protected MapCodec<? extends Block> getCodec(){
		return CODEC;
	}
	
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder){
		builder.add(WATERLOGGED);
	}
	
	public FluidState getFluidState(BlockState state){
		return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
	}
	
	public BlockState getPlacementState(ItemPlacementContext ctx){
		return super.getPlacementState(ctx).with(WATERLOGGED, ctx.getWorld().getFluidState(ctx.getBlockPos()).getFluid() == Fluids.WATER);
	}
	
	public BlockState getStateForNeighborUpdate(BlockState state, Direction dir, BlockState neighbor, WorldAccess world, BlockPos pos, BlockPos neighborPos){
		if(state.get(WATERLOGGED))
			world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
		
		return super.getStateForNeighborUpdate(state, dir, neighbor, world, pos, neighborPos);
	}
}