package arcana.mixin;

import arcana.ArcanaRegistry;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractBlock.AbstractBlockState.class)
public abstract class AbstractBlockStateMixin{
	
	@Shadow
	protected abstract BlockState asBlockState();
	
	@ModifyExpressionValue(method = "getModelOffset", at = @At(value = "NEW", target = "(DDD)Lnet/minecraft/util/math/Vec3d;"))
	Vec3d modifyModelOffsetY(Vec3d original, BlockView world, BlockPos pos){
		BlockState state = asBlockState();
		if(state.isIn(BlockTags.SMALL_FLOWERS) && world.getBlockState(pos.down()).isOf(ArcanaRegistry.STONE_VASE))
			return new Vec3d(original.x, original.y - 0.5, original.z);
		return original;
	}
}