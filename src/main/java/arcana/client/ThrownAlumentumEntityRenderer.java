package arcana.client;

import arcana.entities.ThrownAlumentumEntity;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.util.Identifier;

public class ThrownAlumentumEntityRenderer extends EntityRenderer<ThrownAlumentumEntity>{
	
	protected ThrownAlumentumEntityRenderer(EntityRendererFactory.Context ctx){
		super(ctx);
	}
	
	public Identifier getTexture(ThrownAlumentumEntity entity){
		return null;
	}
}