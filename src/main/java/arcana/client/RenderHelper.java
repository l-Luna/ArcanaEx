package arcana.client;

import arcana.research.Icon;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.*;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;

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
		renderIcon(matrices, icon, x, y, zOffset, 1, 1);
	}
	
	public static void renderIcon(MatrixStack matrices, Icon icon, int x, int y, int zOffset, float itemZoom, int frames){
		if(icon.texture() != null){
			var tex = icon.texture();
			if(!tex.getPath().endsWith(".png"))
				tex = new Identifier(tex.getNamespace(), tex.getPath() + ".png");
			if(!tex.getPath().startsWith("textures/"))
				tex = new Identifier(tex.getNamespace(), "textures/" + tex.getPath());
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderTexture(0, tex);
			frames = Math.max(1, frames);
			int v = (int)((MinecraftClient.getInstance().world.getTime() / 2) % frames) * 16;
			DrawableHelper.drawTexture(matrices, x, y, zOffset, 0, v, 16, 16, 16, 16 * frames);
		}else if(icon.stack() != null){
			var matrices2 = RenderSystem.getModelViewStack();
			matrices2.push();
			matrices2.scale(itemZoom, itemZoom, 1);
			var renderer = MinecraftClient.getInstance().getItemRenderer();
			renderer.renderGuiItemIcon(icon.stack(), x, y);
			renderer.renderGuiItemOverlay(MinecraftClient.getInstance().textRenderer, icon.stack(), x, y);
			matrices2.pop();
			RenderSystem.applyModelViewMatrix();
		}
	}
	
	//
	
	private static void colVertex(VertexConsumer cons, MatrixStack ms, int colour, float x, float y, float z, int nX, int nY, int nZ, Sprite sprite, boolean isFx){
		// use face normals and cube positions to pick UVs (note |nX| + |nY| + |nZ| = 1)
		// on side faces (|nX| + |nZ| = 1), use the other coordinate to decide U, and Y for V
		// otherwise use X for U and Z for V
		float localU = Math.abs(nX * z + nZ * x + nY * x), localV = Math.abs(nX * y + nZ * y + nY * z);
		float u = MathHelper.lerp(localU, sprite.getMinU(), sprite.getMaxU()), v = MathHelper.lerp(localV, sprite.getMinV(), sprite.getMaxV());
		if(isFx){
			if(!(cons instanceof BufferVertexConsumer bvc))
				throw new IllegalArgumentException("Can only render FX vertices directly to tesselator!");
			bvc.vertex(ms.peek().getPositionMatrix(), x, y, z);
			bvc.texture(u, v);
			bvc.color(colour);
			bvc.light(LightmapTextureManager.MAX_LIGHT_COORDINATE);
			// manually insert localUV... a bit messy, see BufferVertexConsumer#texture for reference
			bvc.putFloat(0, localU);
			bvc.putFloat(4, localV);
			bvc.nextElement();
			bvc.next();
		}else
			cons.vertex(ms.peek().getPositionMatrix(), x, y, z)
					.color(colour)
					.texture(u, v)
					.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
					.normal(nX, nY, nZ)
					.next();
	}
	
	public static void colCuboid(VertexConsumer cons, MatrixStack ms, int colour, Vec3d pos, float size, Sprite sprite, boolean isFx){
		colCuboid(cons, ms, colour, pos, size, size, size, sprite, isFx);
	}
	
	public static void colCuboid(VertexConsumer cons, MatrixStack ms, int colour, Vec3d pos, float xSize, float ySize, float zSize, Sprite sprite, boolean isFx){
		ms.push();
		ms.translate(pos.x, pos.y, pos.z);
		ms.scale(xSize, ySize, zSize);
		
		// top
		int darker = ColorHelper.Argb.mixColor(colour, 0xFFCCCCCC);
		colVertex(cons, ms, darker, 0, 1, 1, 0, 1, 0, sprite, isFx);
		colVertex(cons, ms, darker, 1, 1, 1, 0, 1, 0, sprite, isFx);
		colVertex(cons, ms, darker, 1, 1, 0, 0, 1, 0, sprite, isFx);
		colVertex(cons, ms, darker, 0, 1, 0, 0, 1, 0, sprite, isFx);
		
		// bottom
		colVertex(cons, ms, darker, 0, 0, 0, 0, -1, 0, sprite, isFx);
		colVertex(cons, ms, darker, 1, 0, 0, 0, -1, 0, sprite, isFx);
		colVertex(cons, ms, darker, 1, 0, 1, 0, -1, 0, sprite, isFx);
		colVertex(cons, ms, darker, 0, 0, 1, 0, -1, 0, sprite, isFx);
		
		// east (+X) face
		colVertex(cons, ms, colour, 1, 1, 0, 1, 0, 0, sprite, isFx);
		colVertex(cons, ms, colour, 1, 1, 1, 1, 0, 0, sprite, isFx);
		colVertex(cons, ms, colour, 1, 0, 1, 1, 0, 0, sprite, isFx);
		colVertex(cons, ms, colour, 1, 0, 0, 1, 0, 0, sprite, isFx);
		
		// west (-X) face
		colVertex(cons, ms, colour, 0, 1, 0, -1, 0, 0, sprite, isFx);
		colVertex(cons, ms, colour, 0, 0, 0, -1, 0, 0, sprite, isFx);
		colVertex(cons, ms, colour, 0, 0, 1, -1, 0, 0, sprite, isFx);
		colVertex(cons, ms, colour, 0, 1, 1, -1, 0, 0, sprite, isFx);
		
		// north (-Z) face
		colVertex(cons, ms, colour, 1, 0, 0, 0, 0, -1, sprite, isFx);
		colVertex(cons, ms, colour, 0, 0, 0, 0, 0, -1, sprite, isFx);
		colVertex(cons, ms, colour, 0, 1, 0, 0, 0, -1, sprite, isFx);
		colVertex(cons, ms, colour, 1, 1, 0, 0, 0, -1, sprite, isFx);
		
		// south (+Z) face
		colVertex(cons, ms, colour, 0, 0, 1, 0, 0, 1, sprite, isFx);
		colVertex(cons, ms, colour, 1, 0, 1, 0, 0, 1, sprite, isFx);
		colVertex(cons, ms, colour, 1, 1, 1, 0, 0, 1, sprite, isFx);
		colVertex(cons, ms, colour, 0, 1, 1, 0, 0, 1, sprite, isFx);
		
		ms.pop();
	}
}