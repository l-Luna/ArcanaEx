package arcana.client.entity;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3f;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;
import software.bernie.geckolib3.util.EModelRenderCycle;
import software.bernie.geckolib3.util.GeoUtils;
import software.bernie.geckolib3.util.RenderUtils;

public class BipedGeoEntityRenderer<T extends LivingEntity & IAnimatable> extends GeoEntityRenderer<T>{
	
	private static final String headBone = "head";
	private static final String bodyBone = "torso";
	private static final String rightArmBone = "right_arm";
	private static final String leftArmBone = "left_arm";
	private static final String rightLegBone = "right_leg";
	private static final String leftLegBone = "left_leg";
	
	private final BipedEntityModel<T> base;
	
	public BipedGeoEntityRenderer(EntityRendererFactory.Context renderManager, AnimatedGeoModel<T> model){
		super(renderManager, model);
		base = new BipedEntityModel<>(renderManager.getPart(EntityModelLayers.PLAYER));
	}
	
	public void render(GeoModel model,
	                   T animatable,
	                   float delta,
	                   RenderLayer type,
	                   MatrixStack matrices,
	                   VertexConsumerProvider vcp,
	                   VertexConsumer vc,
	                   int packedLight,
	                   int packedOverlay,
	                   float red,
	                   float green,
	                   float blue,
	                   float alpha){
		setCurrentRTB(vcp);
		
		float bodyYaw = MathHelper.lerpAngleDegrees(delta, animatable.prevBodyYaw, animatable.bodyYaw);
		float headYaw = MathHelper.lerpAngleDegrees(delta, animatable.prevHeadYaw, animatable.headYaw);
		float headPitch = MathHelper.lerpAngleDegrees(delta, animatable.prevPitch, animatable.getPitch());
		
		// TODO: other arm poses
		base.rightArmPose = base.leftArmPose = BipedEntityModel.ArmPose.EMPTY;
		if(animatable instanceof MobEntity me){
			if(me.isAttacking() && me.getMainHandStack().getItem() instanceof BowItem)
				base.leftArmPose = BipedEntityModel.ArmPose.BOW_AND_ARROW;
			else if(!me.getMainHandStack().isEmpty())
				base.leftArmPose = BipedEntityModel.ArmPose.ITEM;
		}
		base.handSwingProgress = animatable.getHandSwingProgress(delta);
		
		base.setAngles(animatable,
				/* limb angle */ animatable.limbAngle - animatable.limbDistance * (1 - delta),
				/* limb distance */ Math.min(MathHelper.lerp(delta, animatable.lastLimbDistance, animatable.limbDistance), 1),
				/* animation progress */ animatable.age + delta,
				/* head yaw */ headYaw - bodyYaw,
				/* head pitch */ headPitch);
		
		copy(headBone, base.head);
		copy(bodyBone, base.body);
		copy(rightArmBone, base.rightArm);
		copy(leftArmBone, base.leftArm);
		copy(rightLegBone, base.rightLeg);
		copy(leftLegBone, base.leftLeg);
		
		boolean isLeft = true;
		if(getCurrentModelRenderCycle() == EModelRenderCycle.INITIAL){
			ItemStack stack = animatable.getStackInHand(isLeft ? Hand.MAIN_HAND : Hand.OFF_HAND);
			if(!stack.isEmpty()){
				matrices.push();
				
				IBone bone = modelProvider.getBone(leftArmBone);
				RenderUtils.translateAndRotateMatrixForBone(matrices, (GeoBone)bone);
				
				matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(90));
				matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(180));
				matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(180));
				matrices.translate((isLeft ? -1 : 1) / 16f, 0.125, -0.625);
				
				MinecraftClient.getInstance().getItemRenderer().renderItem(animatable,
						stack,
						/*isLeft ? ModelTransformation.Mode.THIRD_PERSON_LEFT_HAND :*/ ModelTransformation.Mode.THIRD_PERSON_RIGHT_HAND,
						false,
						matrices,
						getCurrentRTB(),
						animatable.world,
						packedLight,
						LivingEntityRenderer.getOverlay(animatable, 0),
						animatable.getId());
				// avoids graphical issues
				getCurrentRTB().getBuffer(RenderLayer.getEntityTranslucent(getTextureLocation(animatable)));
				
				matrices.pop();
			}
		}
		
		super.render(model, animatable, delta, type, matrices, vcp, vc, packedLight, packedOverlay, red, green, blue, alpha);
	}
	
	private void copy(String boneName, ModelPart vbone){
		GeoUtils.copyRotations(vbone, modelProvider.getBone(boneName));
	}
}