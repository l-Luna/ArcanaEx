package arcana.client.entity;

import arcana.client.ArcanaClient;
import arcana.entities.PrismaticOrbEntity;
import arcana.util.MathUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class PrismaticOrbEntityRenderer extends EntityRenderer<PrismaticOrbEntity>{
	
	private static Sprite whiteSprite = null;
	
	public PrismaticOrbEntityRenderer(EntityRendererFactory.Context ctx){
		super(ctx);
	}
	
	public Identifier getTexture(PrismaticOrbEntity entity){
		return null;
	}
	
	public void render(PrismaticOrbEntity entity, float yaw, float dt, MatrixStack ms, VertexConsumerProvider vcs, int light){
		super.render(entity, yaw, dt, ms, vcs, light);
		
		var atlas = MinecraftClient.getInstance().getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);
		whiteSprite = atlas.apply(ArcanaClient.miscWhite);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.enableDepthTest();
		RenderSystem.setShader(GameRenderer::getPositionColorTexLightmapShader);
		RenderSystem.setShaderColor(1, 1, 1, 1);
		RenderSystem.setShaderTexture(0, SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);
		BufferBuilder vc = Tessellator.getInstance().getBuffer();
		vc.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL);
		
		ms.push();
		ms.translate(-0.0625, -0.0625, -0.0625);
		float time = entity.age + dt;
		// 6 orbs following paths that look like rotating around the diagonal of a sphere,
		// with either dimension's frequency scaled, and the object's size scaled
		for(int xf = 1; xf < 4; xf++)
			for(int yf = 0; yf < 4; yf++)
				colCuboid(vc, ms, ColorHelper.Argb.getArgb(255, (int)(255f * (xf / 4f + 0.25f)), (int)(255f * (yf / 4f + 0.25f)), 255), MathUtil.facingToVec((float)(Math.sin(time * xf / 7f) * Math.PI), (float)(Math.cos(time * yf / 7f) * Math.PI)).multiply(0.1f), 0.1f);
		ms.pop();
		
		RenderSystem.setShaderColor(1, 1, 1, 1);
		BufferRenderer.drawWithShader(vc.end());
		RenderSystem.disableBlend();
	}
	
	private void colVertex(VertexConsumer cons, MatrixStack ms, int colour, float x, float y, float z, int nX, int nY, int nZ){
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
	
	private void colCuboid(VertexConsumer cons, MatrixStack ms, int colour, Vec3d pos, float size){
		ms.push();
		ms.translate(pos.x, pos.y, pos.z);
		ms.scale(size, size, size);
		
		// top
		int darker = ColorHelper.Argb.mixColor(colour, 0xFFCCCCCC);
		colVertex(cons, ms, darker, 0, 1, 1, 0, 1, 0);
		colVertex(cons, ms, darker, 1, 1, 1, 0, 1, 0);
		colVertex(cons, ms, darker, 1, 1, 0, 0, 1, 0);
		colVertex(cons, ms, darker, 0, 1, 0, 0, 1, 0);
		
		// bottom
		colVertex(cons, ms, darker, 0, 0, 0, 0, -1, 0);
		colVertex(cons, ms, darker, 1, 0, 0, 0, -1, 0);
		colVertex(cons, ms, darker, 1, 0, 1, 0, -1, 0);
		colVertex(cons, ms, darker, 0, 0, 1, 0, -1, 0);
		
		// east (+X) face
		colVertex(cons, ms, colour, 1, 1, 0, 1, 0, 0);
		colVertex(cons, ms, colour, 1, 1, 1, 1, 0, 0);
		colVertex(cons, ms, colour, 1, 0, 1, 1, 0, 0);
		colVertex(cons, ms, colour, 1, 0, 0, 1, 0, 0);
		
		// west (-X) face
		colVertex(cons, ms, colour, 0, 1, 0, -1, 0, 0);
		colVertex(cons, ms, colour, 0, 0, 0, -1, 0, 0);
		colVertex(cons, ms, colour, 0, 0, 1, -1, 0, 0);
		colVertex(cons, ms, colour, 0, 1, 1, -1, 0, 0);
		
		// north (-Z) face
		colVertex(cons, ms, colour, 1, 0, 0, 0, 0, -1);
		colVertex(cons, ms, colour, 0, 0, 0, 0, 0, -1);
		colVertex(cons, ms, colour, 0, 1, 0, 0, 0, -1);
		colVertex(cons, ms, colour, 1, 1, 0, 0, 0, -1);
		
		// south (+Z) face
		colVertex(cons, ms, colour, 0, 0, 1, 0, 0, 1);
		colVertex(cons, ms, colour, 1, 0, 1, 0, 0, 1);
		colVertex(cons, ms, colour, 1, 1, 1, 0, 0, 1);
		colVertex(cons, ms, colour, 0, 1, 1, 0, 0, 1);
		
		ms.pop();
	}
}