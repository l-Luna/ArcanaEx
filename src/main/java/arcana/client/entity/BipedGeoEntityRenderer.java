package arcana.client.entity;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.util.RenderUtil;

import java.util.Optional;

public class BipedGeoEntityRenderer<T extends LivingEntity & GeoEntity> extends GeoEntityRenderer<T>{
	
	private static final String headBone = "head";
	private static final String bodyBone = "torso";
	private static final String rightArmBone = "right_arm";
	private static final String leftArmBone = "left_arm";
	private static final String rightLegBone = "right_leg";
	private static final String leftLegBone = "left_leg";
	
	private final BipedEntityModel<T> base;
	
	public BipedGeoEntityRenderer(EntityRendererFactory.Context renderManager, GeoModel<T> model){
		super(renderManager, model);
		base = new BipedEntityModel<>(renderManager.getPart(EntityModelLayers.PLAYER));
	}
	
	public void render(T entity, float yaw, float delta, MatrixStack matrices, VertexConsumerProvider vcp, int light){
		float bodyYaw = MathHelper.lerpAngleDegrees(delta, entity.prevBodyYaw, entity.bodyYaw);
		float headYaw = MathHelper.lerpAngleDegrees(delta, entity.prevHeadYaw, entity.headYaw);
		float headPitch = MathHelper.lerpAngleDegrees(delta, entity.prevPitch, entity.getPitch());
		
		// TODO: other arm poses
		base.rightArmPose = base.leftArmPose = BipedEntityModel.ArmPose.EMPTY;
		if(entity instanceof MobEntity me){
			if(me.isAttacking() && me.getMainHandStack().getItem() instanceof BowItem)
				base.leftArmPose = BipedEntityModel.ArmPose.BOW_AND_ARROW;
			else if(!me.getMainHandStack().isEmpty())
				base.leftArmPose = BipedEntityModel.ArmPose.ITEM;
		}
		base.handSwingProgress = entity.getHandSwingProgress(delta);
		
		float limbAngle = entity.limbAnimator.getPos(delta);
		float limbDistance = Math.min(1f, entity.limbAnimator.getSpeed());
		base.setAngles(entity,
				/* limb angle */ limbAngle,
				/* limb distance */ limbDistance,
				/* animation progress */ entity.age + delta,
				/* head yaw */ headYaw - bodyYaw,
				/* head pitch */ headPitch);
		
		copy(headBone, base.head);
		copy(bodyBone, base.body);
		copy(rightArmBone, base.rightArm);
		copy(leftArmBone, base.leftArm);
		copy(rightLegBone, base.rightLeg);
		copy(leftLegBone, base.leftLeg);
		
		boolean isLeft = true;
		ItemStack stack = entity.getStackInHand(isLeft ? Hand.MAIN_HAND : Hand.OFF_HAND);
		if(!stack.isEmpty()){
			matrices.push();
			
			Optional<GeoBone> bone = model.getBone(leftArmBone);
			RenderUtil.translateAndRotateMatrixForBone(matrices, bone.get());
			
			matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));
			matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
			matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180));
			matrices.translate((isLeft ? -1 : 1) / 16f, 0.125, -0.625);
			
			MinecraftClient.getInstance().getItemRenderer().renderItem(entity,
					stack,
					/*isLeft ? ModelTransformation.Mode.THIRD_PERSON_LEFT_HAND :*/ ModelTransformationMode.THIRD_PERSON_RIGHT_HAND,
					false,
					matrices,
					vcp,
					entity.getWorld(),
					light,
					LivingEntityRenderer.getOverlay(entity, 0),
					entity.getId());
			// avoids graphical issues
			vcp.getBuffer(RenderLayer.getEntityTranslucent(getTextureLocation(entity)));
			
			matrices.pop();
		}
		
		super.render(entity, yaw, delta, matrices, vcp, light);
	}
	
	private void copy(String boneName, ModelPart vbone){
		GeoBone toBone = model.getBone(boneName).get();
		// TODO: check for correctness
		//toBone.updatePivot(vbone.pivotX, vbone.pivotY, vbone.pivotZ);
		toBone.updateRotation(vbone.pitch, vbone.yaw, vbone.roll);
	}
}