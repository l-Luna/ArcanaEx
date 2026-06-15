package arcana.mixin;

import arcana.duck.ArcanaLivingEntity;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin{
	
	@WrapWithCondition(method = "setupTransforms",
	                   at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;multiply(Lorg/joml/Quaternionf;)V", ordinal = 0),
	                   slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/LivingEntityRenderer;getLyingAngle(Lnet/minecraft/entity/LivingEntity;)F")))
	boolean shouldShowDefaultDeathAnim(MatrixStack instance,
	                                   Quaternionf quaternion,
	                                   LivingEntity entity,
	                                   MatrixStack matrices,
	                                   float animationProgress,
	                                   float bodyYaw,
	                                   float tickDelta){
		return !(entity instanceof ArcanaLivingEntity ale && ale.arcana$diedToPutrefaction());
	}
	
	@Inject(method = "setupTransforms",
	        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;multiply(Lorg/joml/Quaternionf;)V", ordinal = 0),
	        slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/LivingEntityRenderer;getLyingAngle(Lnet/minecraft/entity/LivingEntity;)F")))
	void showPutrefactionDeathAnim(LivingEntity entity, MatrixStack matrices, float animationProgress, float bodyYaw, float tickDelta, float scale, CallbackInfo ci){
		if(entity instanceof ArcanaLivingEntity ale && ale.arcana$diedToPutrefaction()){
			float f = (entity.deathTime + tickDelta - 1) / 20f * 1.6f;
			f = Math.min(MathHelper.sqrt(f), 0.9f);
			matrices.scale(1, 1 - f, 1);
		}
	}
}