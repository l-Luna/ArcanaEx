package arcana.blocks.deco;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

public class CrystalPillarBlock extends Block{
	
	private static final VoxelShape SHAPE = Block.createCuboidShape(7, 0, 7, 9, 16, 9);
	
	public CrystalPillarBlock(Settings settings){
		super(settings);
	}
	
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context){
		return SHAPE;
	}
}