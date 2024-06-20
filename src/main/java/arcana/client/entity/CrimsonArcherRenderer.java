package arcana.client.entity;

import arcana.entities.crimson.CrimsonArcherEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;

public class CrimsonArcherRenderer extends BipedGeoEntityRenderer<CrimsonArcherEntity>{
	
	public CrimsonArcherRenderer(EntityRendererFactory.Context renderManager){
		super(renderManager, new CrimsonArcherModel());
	}
}