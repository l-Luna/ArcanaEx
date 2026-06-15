package arcana.client.entity;

import arcana.client.ArcanaClient;
import arcana.client.RenderHelper;
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

public class PrismaticOrbEntityRenderer extends EntityRenderer<PrismaticOrbEntity>{
	
	public PrismaticOrbEntityRenderer(EntityRendererFactory.Context ctx){
		super(ctx);
	}
	
	public Identifier getTexture(PrismaticOrbEntity entity){
		return null;
	}
	
	public void render(PrismaticOrbEntity entity, float yaw, float dt, MatrixStack ms, VertexConsumerProvider vcs, int light){
		super.render(entity, yaw, dt, ms, vcs, light);
		
		var atlas = MinecraftClient.getInstance().getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.enableDepthTest();
		RenderSystem.setShader(GameRenderer::getPositionColorTexLightmapProgram);
		RenderSystem.setShaderColor(1, 1, 1, 1);
		RenderSystem.setShaderTexture(0, SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);
		BufferBuilder vc = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL);
		
		ms.push();
		ms.translate(-0.0625, -0.0625, -0.0625);
		float time = entity.age + dt;
		Sprite whiteSprite = atlas.apply(ArcanaClient.WHITE_TEX);
		// 6 orbs following paths that look like rotating around the diagonal of a sphere,
		// with either dimension's frequency scaled, and the object's size scaled
		for(int xf = 1; xf < 4; xf++)
			for(int yf = 0; yf < 4; yf++){
				float eSize = entity.getSize();
				float cDist = 0.03f + 0.12f * eSize;
				float cSize = 0.05f + 0.13f * eSize;
				RenderHelper.colCuboid(vc,
						ms,
						ColorHelper.Argb.getArgb(255, (int)(255f * (xf / 4f + 0.25f)), (int)(255f * (yf / 4f + 0.25f)), 255),
						MathUtil.facingToVec(
								(float)(Math.sin(time * xf / 7f) * Math.PI),
								(float)(Math.cos(time * yf / 7f) * Math.PI)).multiply(cDist),
						cSize,
						whiteSprite,
						false);
			}
		ms.pop();
		
		RenderSystem.setShaderColor(1, 1, 1, 1);
		BufferRenderer.draw(vc.end());
		RenderSystem.disableBlend();
	}
}