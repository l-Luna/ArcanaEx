package arcana.client.ber;

import arcana.aspects.AspectStack;
import arcana.aspects.Aspects;
import arcana.blocks.be.MysticMistBlockEntity;
import arcana.client.ArcanaClient;
import arcana.client.AspectRenderer;
import arcana.items.GogglesOfRevealingItem;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.math.noise.PerlinNoiseSampler;
import net.minecraft.util.math.random.LocalRandom;

public class MysticMistBlockEntityRenderer implements BlockEntityRenderer<MysticMistBlockEntity>{
	
	public static final Identifier RAIN = new Identifier("environment/rain");
	public static final Identifier SNOW = new Identifier("environment/snow");
	
	// could pass it as a parameter but eh, too lazy
	private static Sprite whiteSprite = null;
	
	public void render(MysticMistBlockEntity entity,
	                   float tickDelta,
	                   MatrixStack matrices,
	                   VertexConsumerProvider vcp,
	                   int light,
	                   int overlay){
		AspectStack stack = entity.stored;
		if(stack == null)
			return;
		
		// 101% chance this should not be copy-pasted everywhere
		var player = MinecraftClient.getInstance().player;
		aspects:
		if(GogglesOfRevealingItem.hasRevealing(player)){
			matrices.push();
			
			matrices.translate(0.5, 1.8, 0.5);
			matrices.multiply(Quaternion.fromEulerXyzDegrees(new Vec3f(0, -MinecraftClient.getInstance().cameraEntity.getYaw(), 0)));
			matrices.translate(0.5, 0, 0);
			
			var pos = entity.getPos();
			double sqrDist = player.squaredDistanceTo(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5);
			if(sqrDist > 8 * 8){
				matrices.pop();
				break aspects;
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
			AspectRenderer.renderAspect(stack.type(), matrices, 0, 0, 0, 1, 1, 1, alpha);
			AspectRenderer.renderAspectStackOverlay(stack.amount(), matrices, MinecraftClient.getInstance().textRenderer, 0, 0, 0, 0xFFFFFF | intAlpha);
			
			matrices.pop();
		}
		// end
		
		// heat effect
		if(stack.type().equals(Aspects.FIRE)){
			// TODO
			return;
		}
		// aurora effect
		if(stack.type().equals(Aspects.AURA)){
			// TODO
			return;
		}
		
		// water, energy, ice
		boolean isRain = stack.type().equals(Aspects.WATER);
		boolean isThunder = stack.type().equals(Aspects.ENERGY);
		
		final int lim = MysticMistBlockEntity.radius * 2;
		
		matrices.push();
		matrices.translate(-lim/2f, entity.vspace - 1.5, -lim/2f);
		
		double time = entity.getWorld().getTime() + tickDelta;
		var diff = -(long)(time / 64);
		float offset = (float)((time / 64) % 1);
		LocalRandom rng = new LocalRandom(entity.hashCode());
		PerlinNoiseSampler p = new PerlinNoiseSampler(rng);
		
		var atlas = MinecraftClient.getInstance().getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);
		whiteSprite = atlas.apply(ArcanaClient.miscWhite);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.enableDepthTest();
		RenderSystem.setShader(GameRenderer::getPositionColorTexLightmapShader);
		RenderSystem.setShaderColor(1, 1, 1, 1);
		if(isThunder)
			RenderSystem.setShaderColor(0.8f, 0.8f, 0.8f, 1);
		RenderSystem.setShaderTexture(0, SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);
		BufferBuilder vc = Tessellator.getInstance().getBuffer();
		vc.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL);
		
		for(int x = 0; x < lim; x++)
			for(int z = 0; z < lim; z++){
				if(p.sample((x + diff) * .3, 0, (z + diff) * .3) >= 0.13){
					float opacity = 1;
					if(x == 0 || z == 0)
						opacity = offset;
					else if(x == lim - 1 || z == lim - 1)
						opacity = 1 - offset;
					
					// clouds
					colCuboid(vc, matrices, ((int)(opacity * 255) << 24) + 0x00EEEEFE, x + offset, 1.2f, z + offset, 1, 0.35f, 1, light);
				}
			}
		
		RenderSystem.setShaderColor(1, 1, 1, 1);
		Sprite fallSprite = atlas.apply(isRain || isThunder ? RAIN : SNOW);
		
		float texMinU = fallSprite.getMinU();
		float texMaxU = MathHelper.lerp(1, fallSprite.getMinU(), fallSprite.getMaxU());
		float texMinV = fallSprite.getMinV();
		float texMaxV = MathHelper.lerp(1, fallSprite.getMinV(), fallSprite.getMaxV());
		
		final float sqrt2 = MathHelper.SQUARE_ROOT_OF_TWO;
		final float fallrate = isRain ? 32 : isThunder ? 16 : 256;
		
		for(int x = 0; x < lim; x++)
			for(int z = 0; z < lim; z++){
				if(p.sample((x + diff) * .3, 0, (z + diff) * .3) >= 0.13){
					// need two parts to animate it on an atlas
					for(int part = 0; part < 2; part++){
						float lOffset = (float)(((time / fallrate) % 1) + Math.abs(p.sample((x + diff), 0, (z + diff)))) % 1;
						var itv = MathHelper.lerp(1 - lOffset, texMinV, texMaxV);
						float startY = (part == 0) ? 0 : lOffset,
						      endY   = (part == 0) ? lOffset : 1;
						float minV = (part == 0) ? itv : texMinV,
						      maxV = (part == 0) ? texMaxV : itv;
						// X-shape
						for(int direction = 0; direction < 2; direction++){
							matrices.push();
							matrices.translate(x + offset, 1.2f, z + offset);
							matrices.translate(1, 0, 0);
							if(direction == 1)
								matrices.translate(0, 0, 1);
							matrices.scale(4 * sqrt2, 4 * sqrt2, 4 * sqrt2);
							matrices.multiply(Quaternion.fromEulerXyz(0, MathHelper.HALF_PI / 2f, 0));
							matrices.multiply(Quaternion.fromEulerXyz(0, 0, 3 * MathHelper.HALF_PI));
							
							if(direction == 1)
								matrices.multiply(Quaternion.fromEulerXyz(MathHelper.HALF_PI, 0, 0));
							
							// and rain
							var mat = matrices.peek().getPositionMatrix();
							
							// forward
							vc.vertex(mat, endY, 0, 0)
									.color(0xFFFFFFFF)
									.texture(texMinU, maxV)
									.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
									.normal(1, 0, 0)
									.next();
							vc.vertex(mat, endY, -1 / 4f, 0)
									.color(0xFFFFFFFF)
									.texture(texMaxU, maxV)
									.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
									.normal(1, 0, 0)
									.next();
							vc.vertex(mat, startY, -1 / 4f, 0)
									.color(0xFFFFFFFF)
									.texture(texMaxU, minV)
									.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
									.normal(1, 0, 0)
									.next();
							vc.vertex(mat, startY, 0, 0)
									.color(0xFFFFFFFF)
									.texture(texMinU, minV)
									.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
									.normal(1, 0, 0)
									.next();
							
							// and back
							vc.vertex(mat, startY, 0, 0)
									.color(0xFFFFFFFF)
									.texture(texMinU, minV)
									.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
									.normal(1, 0, 0)
									.next();
							vc.vertex(mat, startY, -1 / 4f, 0)
									.color(0xFFFFFFFF)
									.texture(texMaxU, minV)
									.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
									.normal(1, 0, 0)
									.next();
							vc.vertex(mat, endY, -1 / 4f, 0)
									.color(0xFFFFFFFF)
									.texture(texMaxU, maxV)
									.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
									.normal(1, 0, 0)
									.next();
							vc.vertex(mat, endY, 0, 0)
									.color(0xFFFFFFFF)
									.texture(texMinU, maxV)
									.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
									.normal(1, 0, 0)
									.next();
							
							matrices.pop();
						}
					}
				}
			}
		matrices.pop();
		BufferRenderer.drawWithShader(vc.end());
		RenderSystem.disableBlend();
	}
	
	private void colVertex(VertexConsumer cons, MatrixStack ms, int colour, float x, float y, float z, int light, int nX, int nY, int nZ){
		// normals + positions -> UVs
		// lerp(..., abs(nY * x + nZ * y + nX * z)), lerp(..., abs(nZ * x + nX * y + nY * z))
		cons.vertex(ms.peek().getPositionMatrix(), x, y, z)
				.color(colour)
				.texture(
						MathHelper.lerp(Math.abs(nY * x + nZ * y + nX * z), whiteSprite.getMinU(), whiteSprite.getMaxU()),
						MathHelper.lerp(Math.abs(nZ * x + nX * y + nY * z), whiteSprite.getMinV(), whiteSprite.getMaxV())
				)
				.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
				.normal(nX, nY, nZ)
				.next();
	}
	
	private void colCuboid(VertexConsumer cons, MatrixStack ms, int colour, float x, float y, float z, float xSize, float ySize, float zSize, int light){
		ms.push();
		ms.translate(x, y, z);
		ms.scale(xSize, ySize, zSize);
		
		// top
		int darker = ColorHelper.Argb.mixColor(colour, 0xFFCCCCCC);
		colVertex(cons, ms, darker, 0, 1, 1, light, 0, 1, 0);
		colVertex(cons, ms, darker, 1, 1, 1, light, 0, 1, 0);
		colVertex(cons, ms, darker, 1, 1, 0, light, 0, 1, 0);
		colVertex(cons, ms, darker, 0, 1, 0, light, 0, 1, 0);
		
		// bottom
		colVertex(cons, ms, darker, 0, 0, 0, light, 0, -1, 0);
		colVertex(cons, ms, darker, 1, 0, 0, light, 0, -1, 0);
		colVertex(cons, ms, darker, 1, 0, 1, light, 0, -1, 0);
		colVertex(cons, ms, darker, 0, 0, 1, light, 0, -1, 0);
		
		// east (+X) face
		colVertex(cons, ms, colour, 1, 1, 0, light, 1, 0, 0);
		colVertex(cons, ms, colour, 1, 1, 1, light, 1, 0, 0);
		colVertex(cons, ms, colour, 1, 0, 1, light, 1, 0, 0);
		colVertex(cons, ms, colour, 1, 0, 0, light, 1, 0, 0);
		
		// west (-X) face
		colVertex(cons, ms, colour, 0, 1, 0, light, -1, 0, 0);
		colVertex(cons, ms, colour, 0, 0, 0, light, -1, 0, 0);
		colVertex(cons, ms, colour, 0, 0, 1, light, -1, 0, 0);
		colVertex(cons, ms, colour, 0, 1, 1, light, -1, 0, 0);
		
		// north (-Z) face
		colVertex(cons, ms, colour, 1, 0, 0, light, 0, 0, -1);
		colVertex(cons, ms, colour, 0, 0, 0, light, 0, 0, -1);
		colVertex(cons, ms, colour, 0, 1, 0, light, 0, 0, -1);
		colVertex(cons, ms, colour, 1, 1, 0, light, 0, 0, -1);
		
		// south (+Z) face
		colVertex(cons, ms, colour, 0, 0, 1, light, 0, 0, 1);
		colVertex(cons, ms, colour, 1, 0, 1, light, 0, 0, 1);
		colVertex(cons, ms, colour, 1, 1, 1, light, 0, 0, 1);
		colVertex(cons, ms, colour, 0, 1, 1, light, 0, 0, 1);
		
		ms.pop();
	}
	
	public boolean rendersOutsideBoundingBox(MysticMistBlockEntity blockEntity){
		return true;
	}
}