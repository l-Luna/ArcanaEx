package arcana.client.entity;

import arcana.entities.crimson.CrimsonEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;

import static arcana.Arcana.arcId;

public class CrimsonEntityRenderer<T extends CrimsonEntity> extends BipedGeoEntityRenderer<T>{
	
	public CrimsonEntityRenderer(EntityRendererFactory.Context renderManager, String name){
		super(renderManager, new PlainGeoModel<>(
				arcId("geo/crimson_" + name + ".geo.json"),
				arcId("textures/entity/crimson_" + name + ".png"),
				arcId("animations/crimson_" + name + ".animation.json")
		));
	}
}