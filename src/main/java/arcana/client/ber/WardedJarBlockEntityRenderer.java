package arcana.client.ber;

import arcana.aspects.AspectStack;
import arcana.blocks.be.WardedJarBlockEntity;
import arcana.client.AspectRenderer;
import arcana.items.GogglesOfRevealingItem;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;

import static arcana.Arcana.arcId;

public class WardedJarBlockEntityRenderer implements BlockEntityRenderer<WardedJarBlockEntity>{
	
	public static final Identifier topTexture = arcId("block/jar_fluid/top");
	public static final Identifier sideTexture = arcId("block/jar_fluid/side");
	public static final Identifier bottomTexture = arcId("block/jar_fluid/bottom");
	
	public void render(WardedJarBlockEntity entity,
	                   float tickDelta,
	                   MatrixStack matrices,
	                   VertexConsumerProvider vcs,
	                   int light,
	                   int overlay){
		AspectStack stack = entity.getStored();
		if(stack != null){
			// render liquid contents
			var sprites = MinecraftClient.getInstance().getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);
			var top = sprites.apply(topTexture);
			var side = sprites.apply(sideTexture);
			var bottom = sprites.apply(bottomTexture);
			
			VertexConsumer cons = vcs.getBuffer(RenderLayer.getSolid());
			int colour = stack.type().colour() + 0xFF000000;
			float height = stack.amount() / 100f;
			
			matrices.push();
			matrices.translate(.25, .075, .25);
			matrices.scale(.5f, .6f * height, .5f);
			
			// top
			vertex(cons, matrices, colour, 0, 1, 1, top.getMinU(), top.getMaxV(), light);
			vertex(cons, matrices, colour, 1, 1, 1, top.getMaxU(), top.getMaxV(), light);
			vertex(cons, matrices, colour, 1, 1, 0, top.getMaxU(), top.getMinV(), light);
			vertex(cons, matrices, colour, 0, 1, 0, top.getMinU(), top.getMinV(), light);
			
			// bottom
			vertex(cons, matrices, colour, 0, 0, 0, bottom.getMinU(), bottom.getMinV(), light);
			vertex(cons, matrices, colour, 1, 0, 0, bottom.getMaxU(), bottom.getMinV(), light);
			vertex(cons, matrices, colour, 1, 0, 1, bottom.getMaxU(), bottom.getMaxV(), light);
			vertex(cons, matrices, colour, 0, 0, 1, bottom.getMinU(), bottom.getMaxV(), light);
			
			var sideMinV = side.getMaxV() - height * (side.getMaxV() - side.getMinV());
			
			// east (+X) face
			vertex(cons, matrices, colour, 1, 1, 0, side.getMinU(), sideMinV, light);
			vertex(cons, matrices, colour, 1, 1, 1, side.getMaxU(), sideMinV, light);
			vertex(cons, matrices, colour, 1, 0, 1, side.getMaxU(), side.getMaxV(), light);
			vertex(cons, matrices, colour, 1, 0, 0, side.getMinU(), side.getMaxV(), light);
			
			// west (-X) face
			vertex(cons, matrices, colour, 0, 1, 0, side.getMinU(), sideMinV, light);
			vertex(cons, matrices, colour, 0, 0, 0, side.getMinU(), side.getMaxV(), light);
			vertex(cons, matrices, colour, 0, 0, 1, side.getMaxU(), side.getMaxV(), light);
			vertex(cons, matrices, colour, 0, 1, 1, side.getMaxU(), sideMinV, light);
			
			// north (-Z) face
			vertex(cons, matrices, colour, 1, 0, 0, side.getMaxU(), side.getMaxV(), light);
			vertex(cons, matrices, colour, 0, 0, 0, side.getMinU(), side.getMaxV(), light);
			vertex(cons, matrices, colour, 0, 1, 0, side.getMinU(), sideMinV, light);
			vertex(cons, matrices, colour, 1, 1, 0, side.getMaxU(), sideMinV, light);
			
			// south (+Z) face
			vertex(cons, matrices, colour, 0, 0, 1, side.getMinU(), side.getMaxV(), light);
			vertex(cons, matrices, colour, 1, 0, 1, side.getMaxU(), side.getMaxV(), light);
			vertex(cons, matrices, colour, 1, 1, 1, side.getMaxU(), sideMinV, light);
			vertex(cons, matrices, colour, 0, 1, 1, side.getMinU(), sideMinV, light);
			
			matrices.pop();
			
			// render pop-out aspect
			var player = MinecraftClient.getInstance().player;
			if(!GogglesOfRevealingItem.hasRevealing(player))
				return;
			
			// 99% chance this should not be copy-pasted everywhere
			matrices.push();
			
			matrices.translate(0.5, 0.7, 0.5);
			matrices.multiply(Quaternion.fromEulerXyzDegrees(new Vec3f(0, -MinecraftClient.getInstance().cameraEntity.getYaw(), 0)));
			matrices.translate(0.5, 0, -0.5);
			
			var pos = entity.getPos();
			double sqrDist = player.squaredDistanceTo(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5);
			if(sqrDist > 8 * 8){
				matrices.pop();
				return;
			}
			var alpha = (float)(1 - Math.sqrt(sqrDist) / 10);
			var intAlpha = (int)(Math.max(0, alpha * 255)) << 24;
			
			var scale = 48f;
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
	}
	
	private void vertex(VertexConsumer cons, MatrixStack ms, int colour, float x, float y, float z, float u, float v, int light){
		cons.vertex(ms.peek().getPositionMatrix(), x, y, z)
				.color(colour)
				.texture(u, v)
				.light(light)
				.normal(1, 0, 0)
				.next();
	}
}