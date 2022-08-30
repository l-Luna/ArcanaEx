package arcana.client;

import arcana.research.Icon;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;

public class RenderHelper{
	
	// coloured version of DrawableHelper::drawTexture
	
	public static void drawTexture(MatrixStack matrices, int x, int y, int z, float u, float v, int width, int height, float r, float g, float b){
		drawTexture(matrices, x, y, z, u, v, width, height, 256, 256, r, g, b, 1);
	}
	
	public static void drawTexture(MatrixStack matrices, int x, int y, int z, float u, float v, int width, int height, int textureWidth, int textureHeight, float r, float g, float b, float a){
		drawTexture(matrices, x, x + width, y, y + height, z, width, height, u, v, textureWidth, textureHeight, r, g, b, a);
	}
	
	private static void drawTexture(MatrixStack matrices, int x0, int x1, int y0, int y1, int z, int regionWidth, int regionHeight, float u, float v, int textureWidth, int textureHeight, float r, float g, float b, float a){
		drawTexturedQuad(
				matrices.peek().getPositionMatrix(),
				x0,
				x1,
				y0,
				y1,
				z,
				(u + 0.0F) / (float)textureWidth,
				(u + (float)regionWidth) / (float)textureWidth,
				(v + 0.0F) / (float)textureHeight,
				(v + (float)regionHeight) / (float)textureHeight,
				r,
				g,
				b,
				a
		);
	}
	
	private static void drawTexturedQuad(Matrix4f matrix, int x0, int x1, int y0, int y1, int z, float u0, float u1, float v0, float v1, float r, float g, float b, float a){
		RenderSystem.enableBlend();
		RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
		BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
		bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE);
		bufferBuilder.vertex(matrix, (float)x0, (float)y1, (float)z).color(r, g, b, a).texture(u0, v1).next();
		bufferBuilder.vertex(matrix, (float)x1, (float)y1, (float)z).color(r, g, b, a).texture(u1, v1).next();
		bufferBuilder.vertex(matrix, (float)x1, (float)y0, (float)z).color(r, g, b, a).texture(u1, v0).next();
		bufferBuilder.vertex(matrix, (float)x0, (float)y0, (float)z).color(r, g, b, a).texture(u0, v0).next();
		BufferRenderer.drawWithShader(bufferBuilder.end());
		RenderSystem.disableBlend();
	}
	
	// stretchable box
	
	public static void drawStretchableBox(MatrixStack matrices,
	                                      int x,
	                                      int y,
	                                      int u,
	                                      int v,
	                                      int width,
	                                      int height,
	                                      int corner,
										  int texSize){
		drawStretchableBox(matrices, x, y, u, v, width, height, texSize, texSize, corner, corner, corner, corner);
	}
	
	public static void drawStretchableBox(MatrixStack matrices,
	                                      int x,
	                                      int y,
	                                      int u,
	                                      int v,
	                                      int width,
	                                      int height,
	                                      int texWidth,
	                                      int texHeight,
	                                      int topB,
	                                      int bottomB,
	                                      int leftB,
	                                      int rightB){
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.enableBlend();
		
		// corners
		// top left
		DrawableHelper.drawTexture(matrices, x, y, u, v, leftB, topB, 256, 256);
		// top right
		DrawableHelper.drawTexture(matrices, x + width - rightB, y, texWidth - rightB, v, rightB, topB, 256, 256);
		// bottom left
		DrawableHelper.drawTexture(matrices, x, y + height - bottomB, u, texHeight - bottomB, leftB, bottomB, 256, 256);
		// bottom right
		DrawableHelper.drawTexture(matrices, x + width - rightB, y + height - bottomB, texWidth - rightB, texHeight - bottomB, rightB, bottomB, 256, 256);
		
		var fillWidth = width - leftB - rightB;
		var fillHeight = height - topB - bottomB;
		
		// top border
		DrawableHelper.drawTexture(matrices, x + leftB, y, fillWidth, topB, u + leftB, v, texWidth - leftB - rightB, topB, 256, 256);
		// bottom border
		DrawableHelper.drawTexture(matrices, x + leftB, y + height - bottomB, fillWidth, bottomB, u + leftB, texHeight - bottomB, texWidth - leftB - rightB, bottomB, 256, 256);
		// left border
		DrawableHelper.drawTexture(matrices, x, y + topB, leftB, fillHeight, u, v + topB, leftB, texHeight - topB - bottomB, 256, 256);
		// right border
		DrawableHelper.drawTexture(matrices, x + width - rightB, y + topB, rightB, fillHeight, texWidth - rightB, v + topB, rightB, texHeight - topB - bottomB, 256, 256);
		
		// TODO: fill middle?
		// unnecessary for arcanum, but
	}
	
	public static void renderIcon(MatrixStack matrices, Icon icon, int x, int y, int zOffset){
		renderIcon(matrices, icon, x, y, zOffset, 1);
	}
	
	public static void renderIcon(MatrixStack matrices, Icon icon, int x, int y, int zOffset, float itemZoom){
		if(icon.texture() != null){
			var tex = icon.texture();
			if(!tex.getPath().endsWith(".png"))
				tex = new Identifier(tex.getNamespace(), tex.getPath() + ".png");
			if(!tex.getPath().startsWith("textures/"))
				tex = new Identifier(tex.getNamespace(), "textures/" + tex.getPath());
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderTexture(0, tex);
			
			DrawableHelper.drawTexture(matrices, x, y, zOffset, 0, 0, 16, 16, 16, 16);
		}else if(icon.stack() != null){
			var matrices2 = RenderSystem.getModelViewStack();
			matrices2.push();
			matrices2.scale(itemZoom, itemZoom, 1);
			MinecraftClient.getInstance().getItemRenderer().renderGuiItemIcon(icon.stack(), x, y);
			matrices2.pop();
			RenderSystem.applyModelViewMatrix();
		}
	}
}