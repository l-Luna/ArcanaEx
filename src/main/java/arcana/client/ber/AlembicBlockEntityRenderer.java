package arcana.client.ber;

import arcana.aspects.AspectMap;
import arcana.blocks.be.AlembicBlockEntity;
import arcana.client.AspectRenderHelper;
import arcana.items.GogglesOfRevealingItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3f;

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
		AspectRenderHelper.renderAspectsInWorld(matrices, player, AspectMap.fromAspectStack(entity.stored), entity.getPos(), new Vec3f(0, 1, -0.8f));
		
		/*matrices.push();
		matrices.translate(0.5, 1, 0.5);
		matrices.multiply(Quaternion.fromEulerXyzDegrees(new Vec3f(0, -MinecraftClient.getInstance().cameraEntity.getYaw(), 0)));
		matrices.translate(0.5, 0, -0.8);
		
		var pos = entity.getPos();
		double sqrDist = player.squaredDistanceTo(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5);
		if(sqrDist > 8 * 8){
			matrices.pop();
			return;
		}
		var alpha = (float)(1 - Math.sqrt(sqrDist) / 10);
		var intAlpha = (int)(Math.max(0, alpha * 255)) << 24;
		
		var scale = 24f;
		matrices.translate((16 / scale - 1) / 2f, 0, 0);
		matrices.scale(1 / scale, 1 / scale, -1 / scale);
		matrices.multiply(Quaternion.fromEulerXyz(0, 0, (float)Math.PI));
		RenderSystem.enableDepthTest();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		AspectRenderer.renderAspect(entity.stored.type(), matrices, 0, 0, 0, 1, 1, 1, alpha);
		AspectRenderer.renderAspectStackOverlay(entity.stored.amount(), matrices, MinecraftClient.getInstance().textRenderer, 0, 0, 0, 0xFFFFFF | intAlpha);
		
		matrices.pop();*/
	}
}