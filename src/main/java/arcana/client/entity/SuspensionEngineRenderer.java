package arcana.client.entity;

import arcana.entities.SuspensionEngineEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import software.bernie.geckolib3.renderers.geo.GeoProjectilesRenderer;

public class SuspensionEngineRenderer extends GeoProjectilesRenderer<SuspensionEngineEntity>{
	
	public SuspensionEngineRenderer(EntityRendererFactory.Context renderManager){
		super(renderManager, new SuspensionEngineModel());
	}
}