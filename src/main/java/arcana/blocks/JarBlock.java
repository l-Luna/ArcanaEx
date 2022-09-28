package arcana.blocks;

import arcana.aspects.AspectSpeck;
import arcana.aspects.SpeckIo;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public class JarBlock extends Block implements SpeckIo{
	
	public static final BooleanProperty connected = BooleanProperty.of("connected");
	public static final VoxelShape shape = createCuboidShape(3, 0, 3, 13, 14, 13);
	
	public JarBlock(Settings settings){
		super(settings);
		setDefaultState(getStateManager().getDefaultState().with(connected, false));
	}
	
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder){
		super.appendProperties(builder);
		builder.add(connected);
	}
	
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context){
		return shape;
	}
	
	public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighbor, WorldAccess world, BlockPos pos, BlockPos neighborPos){
		if(direction == Direction.UP)
			if(neighbor.getBlock() instanceof EssentiaTubeBlock)
				return state.with(connected, true);
			else
				return state.with(connected, false);
		else
			return state;
	}
	
	public boolean accept(AspectSpeck speck){
		return false;
	}
	
	public @Nullable AspectSpeck draw(int max){
		return null;
	}
}