package arcana.client;

import arcana.blocks.MysticMistBlockEntity;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;

public class MysticMistBlockEntityRenderer implements BlockEntityRenderer<MysticMistBlockEntity>{
	
	private static final Identifier CLOUDS = new Identifier("textures/environment/clouds.png");
	
	public void render(MysticMistBlockEntity entity,
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
				var epsilon = 0.00390625F;
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
				
				var epsilon2 = 9.765625E-4F;
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
	}
}