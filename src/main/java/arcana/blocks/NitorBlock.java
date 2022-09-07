package arcana.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

@SuppressWarnings("deprecation")
public class NitorBlock extends Block{
	
	protected static final VoxelShape shape = createCuboidShape(5, 5, 5, 11, 11, 11);
	
	public NitorBlock(Settings settings){
		super(settings);
	}
	
	public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random rng){
		// add a bunch of fire
		double x = pos.getX() + .5;
		double y = pos.getY() + .5;
		double z = pos.getZ() + .5;
		for(int i = 0; i < 3; i++){
			double vX = rng.nextGaussian() / 12;
			double vY = rng.nextGaussian() / 12;
			double vZ = rng.nextGaussian() / 12;
			world.addParticle(ParticleTypes.FLAME, x + vX, y + vY, z + vZ, vX / 16, vY / 16, vZ / 16);
		}
	}
	
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context){
		return shape;
	}
	
	public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context){
		return VoxelShapes.empty();
	}
}