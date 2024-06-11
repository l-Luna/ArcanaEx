package arcana.client.entity;

import arcana.entities.SuspensionEngineEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.model.AnimatedGeoModel;

import static arcana.Arcana.arcId;

public class SuspensionEngineModel extends AnimatedGeoModel<SuspensionEngineEntity>{
	
	private static final Identifier model = arcId("geo/suspension_engine.geo.json"),
									texture = arcId("textures/entity/suspension_engine.png"),
									floatAnim = arcId("animations/suspension_engine.animation.json");
	
	public Identifier getModelResource(SuspensionEngineEntity object){
		return model;
	}
	
	public Identifier getTextureResource(SuspensionEngineEntity object){
		return texture;
	}
	
	public Identifier getAnimationResource(SuspensionEngineEntity animatable){
		return floatAnim;
	}
}