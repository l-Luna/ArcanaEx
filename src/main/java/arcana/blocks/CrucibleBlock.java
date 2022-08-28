package arcana.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

@SuppressWarnings("deprecation")
public class CrucibleBlock extends Block{
	
	protected static final VoxelShape INSIDE = createCuboidShape(2, 4, 2, 14, 15, 14);
	protected static final VoxelShape SHAPE = VoxelShapes.combineAndSimplify(
			createCuboidShape(0, 0, 0, 16, 15, 16),
			VoxelShapes.union(
					createCuboidShape(0, 0, 3, 16, 3, 13),
					createCuboidShape(3, 0, 0, 13, 3, 16),
					createCuboidShape(2, 0, 2, 14, 3, 14),
					INSIDE),
			BooleanBiFunction.ONLY_FIRST);
	
	public static final BooleanProperty FULL = BooleanProperty.of("full");
	
	public CrucibleBlock(Settings settings){
		super(settings);
		setDefaultState(stateManager.getDefaultState().with(FULL, false));
	}
	
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder){
		builder.add(FULL);
	}
	
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context){
		return SHAPE;
	}
	
	public VoxelShape getRaycastShape(BlockState state, BlockView world, BlockPos pos){
		return INSIDE;
	}
	
	public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type){
		return false;
	}
}