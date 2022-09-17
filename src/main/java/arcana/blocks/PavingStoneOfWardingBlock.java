package arcana.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.EntityShapeContext;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

public class PavingStoneOfWardingBlock extends Block{
	
	protected static final VoxelShape normalCollisionShape = Block.createCuboidShape(0, 0, 0, 16, 14, 16);
	protected static final VoxelShape hostileCollisionShape = Block.createCuboidShape(0, 0, 0, 16, 32 + 17, 16);
	
	public PavingStoneOfWardingBlock(Settings settings){
		super(settings);
	}
	
	public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context){
		if(context instanceof EntityShapeContext esc && esc.getEntity() instanceof MobEntity mob)
			return mob instanceof Monster ? hostileCollisionShape : normalCollisionShape;
		else return super.getCollisionShape(state, world, pos, context);
	}
	
	public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type){
		return false;
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