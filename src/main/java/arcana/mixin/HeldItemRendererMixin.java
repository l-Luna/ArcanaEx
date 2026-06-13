package arcana.mixin;

import arcana.api.AnimatedSwingItem;
import arcana.api.AnimatedUseItem;
import arcana.legacy_components.Caster;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public class HeldItemRendererMixin{
	
	@Inject(method = "renderFirstPersonItem", slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getUseAction()Lnet/minecraft/util/UseAction;")), at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/HeldItemRenderer;applyEquipOffset(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/util/Arm;F)V", ordinal = 0, shift = At.Shift.AFTER))
	void renderFirstPersonItem_use(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci){
		Arm arm = hand == Hand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
		if(item.getItem() instanceof AnimatedUseItem pi)
			pi.applyUsingAnimation(matrices, player, item, tickDelta, hand, arm);
		else{
			Caster caster = Caster.from(player);
			if(caster.isDraining()){
				matrices.translate(0, -0.1, 0);
				matrices.multiply(Vec3f.POSITIVE_X.getRadialQuaternion(-0.8f));
			}else if(caster.isContinuousCasting()){
				// cross-reference with crossbow rendering
				int offset = arm == Arm.RIGHT ? 1 : -1;
				matrices.translate(offset * -0.565f, 0, 0);
				matrices.multiply(Vec3f.POSITIVE_X.getRadialQuaternion(-0.9f));
			}
		}
	}
	
	@WrapOperation(method = "renderFirstPersonItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/HeldItemRenderer;applySwingOffset(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/util/Arm;F)V"))
	void renderFirstPersonItem_attack(HeldItemRenderer instance,
	                                  MatrixStack matrices,
	                                  Arm arm,
	                                  float swingProgress,
	                                  Operation<Void> original,
	                                  // from renderFirstPersonItem
	                                  AbstractClientPlayerEntity player,
	                                  float tickDelta,
	                                  float pitch,
	                                  Hand hand,
	                                  float _swingProgress,
	                                  ItemStack item,
	                                  float equipProgress,
	                                  MatrixStack _matrices,
	                                  VertexConsumerProvider vertexConsumers,
	                                  int light){
		if(item.getItem() instanceof AnimatedSwingItem aai){
			if(aai.applySwingAnimation(matrices, player, item, tickDelta, swingProgress, equipProgress, hand, arm))
				return;
		}
		original.call(instance, matrices, arm, swingProgress);
	}
}