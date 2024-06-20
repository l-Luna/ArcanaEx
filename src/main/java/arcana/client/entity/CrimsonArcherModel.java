package arcana.client.entity;

import arcana.entities.crimson.CrimsonArcherEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.model.AnimatedTickingGeoModel;

import static arcana.Arcana.arcId;

public class CrimsonArcherModel extends AnimatedTickingGeoModel<CrimsonArcherEntity>{
	
	private static final Identifier model = arcId("geo/crimson_archer.geo.json"),
			texture = arcId("textures/entity/crimson_archer.png"),
			anim = arcId("animations/crimson_archer.animation.json");
	
	public Identifier getModelResource(CrimsonArcherEntity object){
		return model;
	}
	
	public Identifier getTextureResource(CrimsonArcherEntity object){
		return texture;
	}
	
	public Identifier getAnimationResource(CrimsonArcherEntity animatable){
		return anim;
	}
	
	public void setCustomAnimations(CrimsonArcherEntity animatable, int instanceId, AnimationEvent animationEvent){
		super.setCustomAnimations(animatable, instanceId, animationEvent);
		/*IBone head = getAnimationProcessor().getBone("head");
		
		if(head != null){
			EntityModelData extraData = (EntityModelData)animationEvent.getExtraDataOfType(EntityModelData.class).get(0);
			head.setRotationX(extraData.headPitch * MathHelper.RADIANS_PER_DEGREE);
			head.setRotationY(extraData.netHeadYaw * MathHelper.RADIANS_PER_DEGREE);
		}*/
	}
}