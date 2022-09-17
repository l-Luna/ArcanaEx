package arcana.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

public class PavingStoneOfTravelBlock extends Block{
	
	protected static final VoxelShape collisionShape = Block.createCuboidShape(0, 0, 0, 16, 14, 16);
	
	public PavingStoneOfTravelBlock(Settings settings){
		super(settings);
	}
	
	public float getVelocityMultiplier(){
		return 1.25f;
	}
	
	public float getJumpVelocityMultiplier(){
		return 1.1f;
	}
	
	public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context){
		return collisionShape;
	}
	
	public VoxelShape getSidesShape(BlockState state, BlockView world, BlockPos pos){
		return VoxelShapes.fullCube();
	}
	
	public VoxelShape getCameraCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context){
		return VoxelShapes.fullCube();
	}
	
	public float getAmbientOcclusionLightLevel(BlockState state, BlockView world, BlockPos pos){
		return super.getAmbientOcclusionLightLevel(state, world, pos);
	}
}