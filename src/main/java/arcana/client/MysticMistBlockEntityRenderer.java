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
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.noise.PerlinNoiseSampler;
import net.minecraft.util.math.random.LocalRandom;

public class MysticMistBlockEntityRenderer implements BlockEntityRenderer<MysticMistBlockEntity>{
	
//	private static final Identifier CLOUDS = new Identifier("textures/environment/clouds.png");
//	private static final Identifier WHITE = new Identifier("textures/misc/white.png");
	
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
		
		whiteSprite = MinecraftClient.getInstance().getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE).apply(ArcanaClient.miscWhite);
		VertexConsumer vc = vcp.getBuffer(RenderLayer.getTranslucent());
		
		final int lim = 32;
		for(int x = 0; x < lim; x++)
			for(int z = 0; z < lim; z++){
				if(p.sample((x + diff) * .3, 0, (z + diff) * .3) >= 0.18){
					float opacity = 1;
					if(x == 0 || z == 0)
						opacity = offset;
					else if(x == lim - 1 || z == lim - 1)
						opacity = 1 - offset;
					
					colCuboid(vc, matrices, ((int)(opacity * 255) << 24) + 0x00FFFFFF, x + offset, 1.2f, z + offset, 1, 0.35f, 1, light);
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
	
	/*public void render(MysticMistBlockEntity entity,
	                   float tickDelta,
	                   MatrixStack matrices,
	                   VertexConsumerProvider vcp,
	                   int light,
	                   int overlay){
		//var u = vcp.getBuffer(RenderLayer.getSolid());
		
		float time = (entity.getWorld().getTime() % 256) + tickDelta;
		
		RenderSystem.setShader(GameRenderer::getPositionColorTexLightmapShader);
		matrices.push();
		matrices.translate(0, 4, 0);
		matrices.scale(0.3f, 0.3f, 0.3f);
		matrices.scale(1, 0.1f, 1);
		
		RenderSystem.setShaderColor(1, 1, 1, 1);
		RenderSystem.setShaderTexture(0, CLOUDS);
		RenderSystem.disableCull();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.enableDepthTest();
		RenderSystem.depthMask(true);
		
		BufferBuilder u = Tessellator.getInstance().getBuffer();
		u.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL);
		renderClouds(u, matrices, new Vec3d(1, 1, 1), light, time);
		
		BufferRenderer.drawWithShader(u.end());
		RenderSystem.setShaderColor(1, 1, 1, 1);
		RenderSystem.enableCull();
		RenderSystem.disableBlend();
		
		matrices.pop();
	}
	
	private void renderClouds(VertexConsumer builder, MatrixStack stack, Vec3d color, int light, float time){
		float red = (float)color.x;
		float green = (float)color.y;
		float blue = (float)color.z;
		float red2 = red * 0.9F;
		float green2 = green * 0.9F;
		float blue2 = blue * 0.9F;
		float red3 = red * 0.7F;
		float green3 = green * 0.7F;
		float blue3 = blue * 0.7F;
		float red4 = red * 0.8F;
		float green4 = green * 0.8F;
		float blue4 = blue * 0.8F;
		Matrix4f mat = stack.peek().getPositionMatrix();
		for(int i = -3; i <= 4; ++i){
			for(int j = -3; j <= 4; ++j){
				float xOff = (float)(i * 8);
				float zOff = (float)(j * 8);
				float epsilon = 1 / 256f;
				builder.vertex(mat, xOff, 0, zOff + 8)
						.color(red3, green3, blue3, 0.8F)
						.texture(xOff * epsilon, (zOff + 8) * epsilon)
						.light(light)
						.normal(0, -1, 0)
						.next();
				builder.vertex(mat, xOff + 8, 0, zOff + 8)
						.color(red3, green3, blue3, 0.8F)
						.texture((xOff + 8) * epsilon, (zOff + 8) * epsilon)
						.light(light)
						.normal(0, -1, 0)
						.next();
				builder.vertex(mat, xOff + 8, 0, zOff)
						.color(red3, green3, blue3, 0.8F)
						.texture((xOff + 8) * epsilon, (zOff) * epsilon)
						.light(light)
						.normal(0, -1, 0)
						.next();
				builder.vertex(mat, xOff, 0, zOff)
						.color(red3, green3, blue3, 0.8F)
						.texture(xOff * epsilon, zOff * epsilon)
						.light(light)
						.normal(0, -1, 0)
						.next();
				
				float epsilon2 = 1 / 1024f;
				builder.vertex(mat, xOff, 4 - epsilon2, zOff + 8)
						.color(red, green, blue, 0.8F)
						.texture((xOff) * epsilon, (zOff + 8) * epsilon)
						.light(light)
						.normal(0, 1, 0)
						.next();
				builder.vertex(mat, xOff + 8, 4 - epsilon2, zOff + 8)
						.color(red, green, blue, 0.8F)
						.texture((xOff + 8) * epsilon, (zOff + 8) * epsilon)
						.light(light)
						.normal(0, 1, 0)
						.next();
				builder.vertex(mat, xOff + 8, 4 - epsilon2, zOff)
						.color(red, green, blue, 0.8F)
						.texture((xOff + 8) * epsilon, (zOff) * epsilon)
						.light(light)
						.normal(0, 1, 0)
						.next();
				builder.vertex(mat, xOff, 4 - epsilon2, zOff)
						.color(red, green, blue, 0.8F)
						.texture((xOff) * epsilon, (zOff) * epsilon)
						.light(light)
						.normal(0, 1, 0)
						.next();
				
				for(int ag = 0; ag < 8; ++ag){
					builder.vertex(mat, xOff + (float)ag, 0, zOff + 8)
							.color(red2, green2, blue2, 0.8F)
							.texture((xOff + (float)ag + 0.5F) * epsilon, (zOff + 8) * epsilon)
							.light(light)
							.normal(-1, 0, 0)
							.next();
					builder.vertex(mat, xOff + (float)ag, 4, zOff + 8)
							.color(red2, green2, blue2, 0.8F)
							.texture((xOff + (float)ag + 0.5F) * epsilon, (zOff + 8) * epsilon)
							.light(light)
							.normal(-1, 0, 0)
							.next();
					builder.vertex(mat, xOff + (float)ag, 4, zOff)
							.color(red2, green2, blue2, 0.8F)
							.texture((xOff + (float)ag + 0.5F) * epsilon, (zOff) * epsilon)
							.light(light)
							.normal(-1, 0, 0)
							.next();
					builder.vertex(mat, xOff + (float)ag, 0, zOff)
							.color(red2, green2, blue2, 0.8F)
							.texture((xOff + (float)ag + 0.5F) * epsilon, (zOff) * epsilon)
							.light(light)
							.normal(-1, 0, 0)
							.next();
				}
				
				for(int ag = 0; ag < 8; ++ag){
					builder.vertex(mat, xOff + (float)ag + 1 - epsilon2, 0, zOff + 8)
							.color(red2, green2, blue2, 0.8F)
							.texture((xOff + (float)ag + 0.5F) * epsilon, (zOff + 8) * epsilon)
							.light(light)
							.normal(1, 0, 0)
							.next();
					builder.vertex(mat, xOff + (float)ag + 1 - epsilon2, 4, zOff + 8)
							.color(red2, green2, blue2, 0.8F)
							.texture((xOff + (float)ag + 0.5F) * epsilon, (zOff + 8) * epsilon)
							.light(light)
							.normal(1, 0, 0)
							.next();
					builder.vertex(mat, xOff + (float)ag + 1 - epsilon2, 4, zOff)
							.color(red2, green2, blue2, 0.8F)
							.texture((xOff + (float)ag + 0.5F) * epsilon, (zOff) * epsilon)
							.light(light)
							.normal(1, 0, 0)
							.next();
					builder.vertex(mat, xOff + (float)ag + 1 - epsilon2, 0, zOff)
							.color(red2, green2, blue2, 0.8F)
							.texture((xOff + (float)ag + 0.5F) * epsilon, (zOff) * epsilon)
							.light(light)
							.normal(1, 0, 0)
							.next();
				}
				
				for(int ag = 0; ag < 8; ++ag){
					builder.vertex(mat, xOff, 4, zOff + (float)ag)
							.color(red4, green4, blue4, 0.8F)
							.texture((xOff) * epsilon, (zOff + (float)ag + 0.5F) * epsilon)
							.light(light)
							.normal(0, 0, -1)
							.next();
					builder.vertex(mat, xOff + 8, 4, zOff + (float)ag)
							.color(red4, green4, blue4, 0.8F)
							.texture((xOff + 8) * epsilon, (zOff + (float)ag + 0.5F) * epsilon)
							.light(light)
							.normal(0, 0, -1)
							.next();
					builder.vertex(mat, xOff + 8, 0, zOff + (float)ag)
							.color(red4, green4, blue4, 0.8F)
							.texture((xOff + 8) * epsilon, (zOff + (float)ag + 0.5F) * epsilon)
							.light(light)
							.normal(0, 0, -1)
							.next();
					builder.vertex(mat, xOff, 0, zOff + (float)ag)
							.color(red4, green4, blue4, 0.8F)
							.texture((xOff) * epsilon, (zOff + (float)ag + 0.5F) * epsilon)
							.light(light)
							.normal(0, 0, -1)
							.next();
				}
				
				for(int ag = 0; ag < 8; ++ag){
					builder.vertex(mat, xOff, 4, zOff + (float)ag + 1 - epsilon2)
							.color(red4, green4, blue4, 0.8F)
							.texture((xOff) * epsilon, (zOff + (float)ag + 0.5F) * epsilon)
							.light(light)
							.normal(0, 0, 1)
							.next();
					builder.vertex(mat, xOff + 8, 4, zOff + (float)ag + 1 - epsilon2)
							.color(red4, green4, blue4, 0.8F)
							.texture((xOff + 8) * epsilon, (zOff + (float)ag + 0.5F) * epsilon)
							.light(light)
							.normal(0, 0, 1)
							.next();
					builder.vertex(mat, xOff + 8, 0, zOff + (float)ag + 1 - epsilon2)
							.color(red4, green4, blue4, 0.8F)
							.texture((xOff + 8) * epsilon, (zOff + (float)ag + 0.5F) * epsilon)
							.light(light)
							.normal(0, 0, 1)
							.next();
					builder.vertex(mat, xOff, 0, zOff + (float)ag + 1 - epsilon2)
							.color(red4, green4, blue4, 0.8F)
							.texture((xOff) * epsilon, (zOff + (float)ag + 0.5F) * epsilon)
							.light(light)
							.normal(0, 0, 1)
							.next();
				}
			}
		}
	}*/
}