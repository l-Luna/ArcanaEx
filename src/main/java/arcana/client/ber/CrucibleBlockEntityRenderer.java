package arcana.client.ber;

import arcana.blocks.be.CrucibleBlockEntity;
import arcana.client.AspectRenderHelper;
import arcana.items.GogglesOfRevealingItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Vector3f;

public class CrucibleBlockEntityRenderer implements BlockEntityRenderer<CrucibleBlockEntity>{
	
	public void render(CrucibleBlockEntity entity,
	                   float delta,
	                   MatrixStack matrices,
	                   VertexConsumerProvider consumers,
	                   int light,
	                   int overlay){
		var player = MinecraftClient.getInstance().player;
		if(!GogglesOfRevealingItem.hasRevealing(player))
			return;
		AspectRenderHelper.renderAspectsInWorld(matrices, player, entity.getAspects(), entity.getPos(), new Vector3f(0, 1.8f, 0));
	}
}