package arcana.blocks;

import arcana.aspects.AspectIo;
import arcana.aspects.AspectStack;
import arcana.blocks.be.WardedJarBlockEntity;
import arcana.blocks.tubes.EssentiaTubeBlock;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public class WardedJarBlock extends BlockWithEntity implements AspectIo{
	
	public static final BooleanProperty connected = BooleanProperty.of("connected");
	public static final VoxelShape shape = createCuboidShape(3, 0, 3, 13, 14, 13);
	
	public WardedJarBlock(Settings settings){
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
	
	public AspectStack accept(AspectStack speck, World world, BlockPos pos, Direction from){
		return world.getBlockEntity(pos) instanceof WardedJarBlockEntity self ? self.accept(speck, world, pos, from) : null;
	}
	
	public @Nullable AspectStack draw(int max, World world, BlockPos pos, Direction from){
		return world.getBlockEntity(pos) instanceof WardedJarBlockEntity self ? self.draw(max, world, pos, from) : null;
	}
	
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state){
		return new WardedJarBlockEntity(pos, state);
	}
	
	public BlockRenderType getRenderType(BlockState state){
		return BlockRenderType.MODEL;
	}
}