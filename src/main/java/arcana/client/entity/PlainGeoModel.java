package arcana.client.entity;

import net.minecraft.util.Identifier;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.model.GeoModel;

public class PlainGeoModel<T extends GeoEntity> extends GeoModel<T>{
	
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