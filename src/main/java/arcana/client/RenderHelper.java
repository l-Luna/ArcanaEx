package arcana.client;

import arcana.research.Icon;
import arcana.util.TintingVertexConsumerProvider;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

import static arcana.Arcana.arcId;

public class RenderHelper{
	
	// coloured version of DrawableHelper::drawTexture
	
	public static void drawTexture(DrawContext ctx, int x, int y, float z, float u, float v, int width, int height, float r, float g, float b){
		drawTexture(ctx, x, y, z, u, v, width, height, 256, 256, r, g, b, 1);
	}
	
	public static void drawTexture(DrawContext ctx, int x, int y, float z, float u, float v, int width, int height, int textureWidth, int textureHeight, float r, float g, float b, float a){
		drawTexture(ctx, x, x + width, y, y + height, z, width, height, u, v, textureWidth, textureHeight, r, g, b, a);
	}
	
	private static void drawTexture(DrawContext ctx, int x0, int x1, int y0, int y1, float z, int regionWidth, int regionHeight, float u, float v, int textureWidth, int textureHeight, float r, float g, float b, float a){
		drawTexturedQuad(
				ctx.getMatrices().peek().getPositionMatrix(),
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
	
	private static void drawTexturedQuad(Matrix4f matrix, int x0, int x1, int y0, int y1, float z, float u0, float u1, float v0, float v1, float r, float g, float b, float a){
		RenderSystem.enableBlend();
		RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
		BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
		bufferBuilder.vertex(matrix, (float)x0, (float)y1, z).color(r, g, b, a).texture(u0, v1);
		bufferBuilder.vertex(matrix, (float)x1, (float)y1, z).color(r, g, b, a).texture(u1, v1);
		bufferBuilder.vertex(matrix, (float)x1, (float)y0, z).color(r, g, b, a).texture(u1, v0);
		bufferBuilder.vertex(matrix, (float)x0, (float)y0, z).color(r, g, b, a).texture(u0, v0);
		BufferRenderer.draw(bufferBuilder.end());
		RenderSystem.disableBlend();
	}
	
	// stretchable box
	
	public static void drawStretchableBox(DrawContext ctx,
										  Identifier texture,
	                                      int x,
	                                      int y,
	                                      int u,
	                                      int v,
	                                      int width,
	                                      int height,
	                                      int corner,
	                                      int texSize){
		drawStretchableBox(ctx, texture, x, y, u, v, width, height, texSize, texSize, corner, corner, corner, corner);
	}
	
	public static void drawStretchableBox(DrawContext ctx,
	                                      Identifier texture,
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
		RenderSystem.setShader(GameRenderer::getPositionTexProgram);
		RenderSystem.enableBlend();
		
		// corners
		// top left
		ctx.drawTexture(texture, x, y, u, v, leftB, topB, 256, 256);
		// top right
		ctx.drawTexture(texture, x + width - rightB, y, texWidth - rightB, v, rightB, topB, 256, 256);
		// bottom left
		ctx.drawTexture(texture, x, y + height - bottomB, u, texHeight - bottomB, leftB, bottomB, 256, 256);
		// bottom right
		ctx.drawTexture(texture, x + width - rightB, y + height - bottomB, texWidth - rightB, texHeight - bottomB, rightB, bottomB, 256, 256);
		
		var fillWidth = width - leftB - rightB;
		var fillHeight = height - topB - bottomB;
		
		// top border
		ctx.drawTexture(texture, x + leftB, y, fillWidth, topB, u + leftB, v, texWidth - leftB - rightB, topB, 256, 256);
		// bottom border
		ctx.drawTexture(texture, x + leftB, y + height - bottomB, fillWidth, bottomB, u + leftB, texHeight - bottomB, texWidth - leftB - rightB, bottomB, 256, 256);
		// left border
		ctx.drawTexture(texture, x, y + topB, leftB, fillHeight, u, v + topB, leftB, texHeight - topB - bottomB, 256, 256);
		// right border
		ctx.drawTexture(texture, x + width - rightB, y + topB, rightB, fillHeight, texWidth - rightB, v + topB, rightB, texHeight - topB - bottomB, 256, 256);
		
		// TODO: fill middle?
		// unnecessary for arcanum, but
	}
	
	public static void renderIcon(DrawContext matrices, Icon icon, int x, int y, int zOffset){
		renderIcon(matrices, icon, x, y, zOffset, 1, 1);
	}
	
	public static void renderIcon(DrawContext matrices, Icon icon, int x, int y, int zOffset, float itemZoom, int frames){
		renderIcon(matrices, icon, x, y, zOffset, itemZoom, frames, 1, 1, 1, 1);
	}
	
	public static void renderIcon(DrawContext ctx, Icon icon, int x, int y, int zOffset, float itemZoom, int frames, float r, float g, float b, float a){
		if(icon.texture() != null){
			var tex = icon.texture();
			if(!tex.getPath().endsWith(".png"))
				tex = Identifier.of(tex.getNamespace(), tex.getPath() + ".png");
			if(!tex.getPath().startsWith("textures/"))
				tex = Identifier.of(tex.getNamespace(), "textures/" + tex.getPath());
			frames = Math.max(1, frames);
			int v = (int)((MinecraftClient.getInstance().world.getTime() / 2) % frames) * 16;
			ctx.drawTexture(tex, x, y, zOffset, 0, v, 16, 16, 16, 16 * frames);
		}else if(icon.stack() != null){
			Matrix4fStack matrices2 = RenderSystem.getModelViewStack();
			matrices2.pushMatrix();
			matrices2.scale(itemZoom, itemZoom, 1);
			ItemRenderer renderer = MinecraftClient.getInstance().getItemRenderer();
			ItemStack stack = icon.stack();
			renderGuiItemModel(renderer, stack, x, y, renderer.getModel(stack, null, null, 0), r, g, b, a);
			//renderer.renderGuiItemOverlay(MinecraftClient.getInstance().textRenderer, icon.stack(), x, y);
			matrices2.popMatrix();
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
			if(!(cons instanceof BufferBuilder bvc))
				throw new IllegalArgumentException("Can only render FX vertices directly to tesselator!");
			bvc.vertex(ms.peek().getPositionMatrix(), x, y, z);
			bvc.texture(u, v);
			bvc.color(colour);
			bvc.light(LightmapTextureManager.MAX_LIGHT_COORDINATE);
			// manually insert localUV... a bit messy, see BufferVertexConsumer#texture for reference
			// TODO: agony
			bvc.putFloat(0, localU);
			bvc.putFloat(4, localV);
		}else
			cons.vertex(ms.peek().getPositionMatrix(), x, y, z)
					.color(colour)
					.texture(u, v)
					.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
					.normal(nX, nY, nZ);
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
	
	// tiny numbers
	
	private static final Identifier TINY_NUMBERS = arcId("textures/gui/tiny_numbers.png");
	
	public static void drawTinyNumbers(DrawContext ctx, String str, int x, int y){
		drawTinyNumbers(ctx, str, x, y, 1, 1, 1, 1);
	}
	
	public static void drawTinyNumbers(DrawContext ctx, String str, int x, int y, float r, float g, float b, float a){
		RenderSystem.setShaderTexture(0, TINY_NUMBERS);
		for(int p = 0; p < 2; p++){
			int xx = x;
			int yy = y - 6;
			if(p == 0)
				RenderSystem.setShaderColor(r*0.25f, g*0.25f, b*0.25f, a);
			else{
				RenderSystem.setShaderColor(r, g, b, a);
				xx--;
				yy--;
			}
			for(int i = 0; i < str.length(); i++){
				char ch = str.charAt(i);
				int j = ch - '0';
				int u = (j % 5) * 3;
				int v = (j / 5) * 5;
				if(ch == '%'){
					u = 0;
					v = 10;
				}else if(ch == '.'){
					u = 3;
					v = 10;
				}
				drawTexture(ctx, xx, yy, p * 0.03f, u, v, 3, 5, 16, 16, 1, 1, 1, 1);
				xx += 4;
			}
		}
		RenderSystem.setShaderColor(1, 1, 1, 1);
	}
	
	// coloured version of ItemRenderer::renderGuiItemMode
	
	private static void renderGuiItemModel(ItemRenderer self, ItemStack stack, int x, int y, BakedModel model, float r, float g, float b, float a){
//		self.textureManager.getTexture(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE).setFilter(false, false);
		RenderSystem.setShaderTexture(0, SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
		RenderSystem.setShaderColor(r, g, b, a);
		Matrix4fStack matrices = RenderSystem.getModelViewStack();
		matrices.pushMatrix();
		matrices.translate(x, y, 100 + 0);
		matrices.translate(8, 8, 0);
		matrices.scale(1, -1, 1);
		matrices.scale(16, 16, 16);
		RenderSystem.applyModelViewMatrix();
		MatrixStack matrixStack2 = new MatrixStack();
		VertexConsumerProvider.Immediate immediate = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
		boolean unlit = !model.isSideLit();
		if(unlit)
			DiffuseLighting.disableGuiDepthLighting();
		
		self.renderItem(
				stack,
				ModelTransformationMode.GUI,
				false,
				matrixStack2,
				new TintingVertexConsumerProvider(immediate, r, g, b, a),
				LightmapTextureManager.MAX_LIGHT_COORDINATE,
				OverlayTexture.DEFAULT_UV,
				model
		);
		immediate.draw();
		RenderSystem.enableDepthTest();
		if(unlit)
			DiffuseLighting.enableGuiDepthLighting();
		
		matrices.popMatrix();
		RenderSystem.applyModelViewMatrix();
	}
}