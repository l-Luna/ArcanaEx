package arcana.mixin;

import arcana.aura.WardedChunk;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(Explosion.class)
public class ExplosionMixin{

	@WrapOperation(
			method = "collectBlocksAndDamageEntities",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/explosion/ExplosionBehavior;getBlastResistance(Lnet/minecraft/world/explosion/Explosion;Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/fluid/FluidState;)Ljava/util/Optional;"
			)
	)
	Optional<Float> applyWarding(ExplosionBehavior instance, Explosion explosion, BlockView bv, BlockPos pos, BlockState blockState, FluidState fluidState, Operation<Optional<Float>> original){
		if(bv instanceof World w && WardedChunk.isWarded(w, pos))
			return Optional.of(3600000f);
		return original.call(instance, explosion, bv, pos, blockState, fluidState);
	}
}