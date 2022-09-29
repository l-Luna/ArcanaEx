package arcana.blocks;

import arcana.aspects.AspectIo;
import arcana.aspects.AspectStack;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class AlembicBlock extends Block implements AspectIo{
	
	protected static final VoxelShape shape = VoxelShapes.union(
			createCuboidShape(1, 1, 1, 15, 15, 15),
			createCuboidShape(0, 2, 0, 16, 4, 16),
			createCuboidShape(0, 12, 0, 16, 14, 16),
			createCuboidShape(4, 0, 4, 12, 2, 12),
			createCuboidShape(4, 14, 4, 12, 16, 12)
	).simplify();
	
	public AlembicBlock(Settings settings){
		super(settings);
	}
	
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context){
		return shape;
	}
	
	public boolean accept(AspectStack speck, World world, BlockPos pos, Direction from){
		return false;
	}
	
	public @Nullable AspectStack draw(int max, World world, BlockPos pos, Direction from){
		return null;
	}
}