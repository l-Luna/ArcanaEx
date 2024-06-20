package arcana.client.entity;

import net.minecraft.util.Identifier;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class PlainGeoModel<T extends IAnimatable> extends AnimatedGeoModel<T>{
	
	private final Identifier model, texture, animation;
	
	public PlainGeoModel(Identifier model, Identifier texture, Identifier animation){
		this.model = model;
		this.texture = texture;
		this.animation = animation;
	}
	
	public Identifier getModelResource(T object){
		return model;
	}
	
	public Identifier getTextureResource(T object){
		return texture;
	}
	
	public Identifier getAnimationResource(T animatable){
		return animation;
	}
}