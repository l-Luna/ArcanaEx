package arcana.blocks.deco;

import com.unascribed.lib39.weld.api.BigBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

public class StoneVaseBlock extends BigBlock{
	
	public static final IntProperty Y = IntProperty.of("y", 0, 1);
	public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
	
	protected static final VoxelShape TOP_SHAPE = Block.createCuboidShape(1, 0, 1, 15, 9, 15);
	protected static final VoxelShape BOTTOM_SHAPE = Block.createCuboidShape(1, 0, 1, 15, 16, 15);
	
	public StoneVaseBlock(Settings settings){
		super(null, Y, null, settings);
	}
	
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder){
		super.appendProperties(builder);
		builder.add(Y, WATERLOGGED);
	}
	
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context){
		if(state.get(Y) == 1)
			return TOP_SHAPE;
		return BOTTOM_SHAPE;
	}
	
	public FluidState getFluidState(BlockState state){
		return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
	}
	
	public BlockState getPlacementState(ItemPlacementContext ctx){
		FluidState fluidState = ctx.getWorld().getFluidState(ctx.getBlockPos());
		return super.getPlacementState(ctx).with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER);
	}
}