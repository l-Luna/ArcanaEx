package arcana.client.ber;

import arcana.aspects.AspectMap;
import arcana.blocks.be.ThaumicHaloBlockEntity;
import arcana.client.AspectRenderer;
import arcana.items.GogglesOfRevealingItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3f;

public class ThaumicHaloBlockEntityRenderer implements BlockEntityRenderer<ThaumicHaloBlockEntity>{
	
	public void render(ThaumicHaloBlockEntity entity,
	                   float delta,
	                   MatrixStack matrices,
	                   VertexConsumerProvider consumers,
	                   int light,
	                   int overlay){
		var player = MinecraftClient.getInstance().player;
		if(!GogglesOfRevealingItem.hasRevealing(player) || entity.stored == null)
			return;
		AspectRenderer.renderAspectsInWorld(matrices, player, AspectMap.fromAspectStack(entity.stored), entity.getPos(), new Vec3f(0, 2, -0.8f));
	}
}