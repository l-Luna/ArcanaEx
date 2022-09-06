package arcana.client;

import arcana.aspects.AspectMap;
import arcana.aspects.AspectStack;
import arcana.blocks.CrucibleBlockEntity;
import arcana.items.GogglesOfRevealingItem;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;

import java.util.List;

public class CrucibleBlockEntityRenderer implements BlockEntityRenderer<CrucibleBlockEntity>{
	
	public void render(CrucibleBlockEntity entity,
	                   float delta,
	                   MatrixStack matrices,
	                   VertexConsumerProvider consumers,
	                   int light,
	                   int overlay){
		var player = MinecraftClient.getInstance().player;
		boolean hasGoggles = player.getEquippedStack(EquipmentSlot.HEAD).getItem() instanceof GogglesOfRevealingItem;
		
		if(!hasGoggles)
			return;
		
		// display aspects above the crucible
		matrices.push();
		matrices.translate(0.5, 2, 0.5);
		matrices.multiply(Quaternion.fromEulerXyzDegrees(new Vec3f(0, -MinecraftClient.getInstance().cameraEntity.getYaw(), 0)));
		AspectMap aspects = entity.getAspects();
		List<AspectStack> stacks = aspects.asStacks();
		
		var pos = entity.getPos();
		double sqrDist = player.squaredDistanceTo(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5);
		if(sqrDist > 8 * 8){
			matrices.pop();
			return;
		}
		var alpha = (float)(1 - Math.sqrt(sqrDist) / 10);
		var intAlpha = (int)(Math.max(0, alpha * 255)) << 24;
		
		for(int i = 0, size = stacks.size(); i < size; i++){
			AspectStack stack = stacks.get(i);
			matrices.push();
			matrices.translate(size / 2d - i, 0, 0);
			var scale = 24f;
			matrices.translate((16 / scale - 1) / 2f, 0, 0);
			matrices.scale(1 / scale, 1 / scale, -1 / scale);
			matrices.multiply(Quaternion.fromEulerXyz(0, 0, (float)Math.PI));
			RenderSystem.enableDepthTest();
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			AspectRenderer.renderAspect(stack.type(), matrices, 0, 0, 0, 1, 1, 1, alpha);
			AspectRenderer.renderAspectStackOverlay(stack.amount(), matrices, MinecraftClient.getInstance().textRenderer, 0, 0, 0, 0xFFFFFF | intAlpha);
			matrices.pop();
		}
		matrices.pop();
	}
}