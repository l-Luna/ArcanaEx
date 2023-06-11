package arcana.client;

import arcana.blocks.MysticMistBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Quaternion;
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
		double time = entity.getWorld().getTime() + tickDelta;
		var diff = -(long)(time / 64);
		float offset = (float)((time / 64) % 1);
		LocalRandom rng = new LocalRandom(entity.hashCode());
		PerlinNoiseSampler p = new PerlinNoiseSampler(rng);
		
		var atlas = MinecraftClient.getInstance().getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);
		whiteSprite = atlas.apply(ArcanaClient.miscWhite);
		VertexConsumer vc = vcp.getBuffer(RenderLayer.getTranslucent());
		
		final int lim = 32;
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
		
		VertexConsumer rainbuf = vcp.getBuffer(RenderLayer.getCutout());
		Sprite rainSprite = atlas.apply(SNOW);
		
		float texMinU = rainSprite.getMinU();
		float texMaxU = MathHelper.lerp(1, rainSprite.getMinU(), rainSprite.getMaxU());
		float texMinV = rainSprite.getMinV();
		float texMaxV = MathHelper.lerp(1, rainSprite.getMinV(), rainSprite.getMaxV());
		
		final float sqrt2 = MathHelper.SQUARE_ROOT_OF_TWO;
		final float fallrate = 256;
		
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
							rainbuf.vertex(mat, endY, 0, 0)
									.color(0xFFFFFFFF)
									.texture(texMinU, maxV)
									.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
									.normal(1, 0, 0)
									.next();
							rainbuf.vertex(mat, endY, -1 / 4f, 0)
									.color(0xFFFFFFFF)
									.texture(texMaxU, maxV)
									.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
									.normal(1, 0, 0)
									.next();
							rainbuf.vertex(mat, startY, -1 / 4f, 0)
									.color(0xFFFFFFFF)
									.texture(texMaxU, minV)
									.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
									.normal(1, 0, 0)
									.next();
							rainbuf.vertex(mat, startY, 0, 0)
									.color(0xFFFFFFFF)
									.texture(texMinU, minV)
									.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
									.normal(1, 0, 0)
									.next();
							
							// and back
							rainbuf.vertex(mat, startY, 0, 0)
									.color(0xFFFFFFFF)
									.texture(texMinU, minV)
									.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
									.normal(1, 0, 0)
									.next();
							rainbuf.vertex(mat, startY, -1 / 4f, 0)
									.color(0xFFFFFFFF)
									.texture(texMaxU, minV)
									.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
									.normal(1, 0, 0)
									.next();
							rainbuf.vertex(mat, endY, -1 / 4f, 0)
									.color(0xFFFFFFFF)
									.texture(texMaxU, maxV)
									.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
									.normal(1, 0, 0)
									.next();
							rainbuf.vertex(mat, endY, 0, 0)
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