package arcana.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.PlantBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

public class SizedPlantBlock extends PlantBlock{
	
	private final VoxelShape shape;
	
	public SizedPlantBlock(Settings settings, float width, float height){
		super(settings);
		shape = Block.createCuboidShape(8 - (width / 2), 0, 8 - (width / 2), 8 + (width / 2), height, 8 + (width / 2));
	}
	
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context){
		Vec3d o = state.getModelOffset(world, pos);
		return shape.offset(o.x, o.y, o.z);
	}
}