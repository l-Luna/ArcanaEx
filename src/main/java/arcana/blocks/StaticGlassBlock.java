package arcana.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.block.EntityShapeContext;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.TranslucentBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

public class StaticGlassBlock extends TranslucentBlock{
	
	public StaticGlassBlock(Settings settings){
		super(settings);
	}
	
	public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context){
		return context instanceof EntityShapeContext esc
			&& esc.getEntity() instanceof LivingEntity le
			&& le.isSneaking()
				? VoxelShapes.empty()
				: super.getCollisionShape(state, world, pos, context);
	}
}