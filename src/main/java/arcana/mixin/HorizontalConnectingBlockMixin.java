package arcana.mixin;

import arcana.ArcanaTags;
import arcana.components.CaArrow;
import net.minecraft.block.BlockState;
import net.minecraft.block.EntityShapeContext;
import net.minecraft.block.HorizontalConnectingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HorizontalConnectingBlock.class)
public class HorizontalConnectingBlockMixin{
	
	@Inject(method = "getCollisionShape", at = @At("HEAD"), cancellable = true)
	void getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context, CallbackInfoReturnable<VoxelShape> cir){
		if(context instanceof EntityShapeContext esc
				&& esc.getEntity() instanceof ArrowEntity arrow
				&& CaArrow.isProjected(arrow)
				&& state.isIn(ArcanaTags.PROJECTED_ARROW_IGNORES))
			cir.setReturnValue(VoxelShapes.empty());
	}
}