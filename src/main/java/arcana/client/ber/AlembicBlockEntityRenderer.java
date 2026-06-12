package arcana.client.ber;

import arcana.aspects.AspectMap;
import arcana.blocks.be.AlembicBlockEntity;
import arcana.client.AspectRenderHelper;
import arcana.items.GogglesOfRevealingItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Vector3f;

public class AlembicBlockEntityRenderer implements BlockEntityRenderer<AlembicBlockEntity>{
	
	public void render(AlembicBlockEntity entity,
	                   float tickDelta,
	                   MatrixStack matrices,
	                   VertexConsumerProvider vertexConsumers,
	                   int light,
	                   int overlay){
		var player = MinecraftClient.getInstance().player;
		if(!GogglesOfRevealingItem.hasRevealing(player) || entity.stored == null)
			return;
		AspectRenderHelper.renderAspectsInWorld(matrices, player, AspectMap.fromAspectStack(entity.stored), entity.getPos(), new Vector3f(0, 1, -0.8f));
	}
}