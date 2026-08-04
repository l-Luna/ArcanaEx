package arcana.client.entity;

import arcana.client.RenderHelper;
import arcana.entities.MagicOrbEntity;
import arcana.util.MathUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.util.List;

import static arcana.Arcana.arcId;

public class MagicOrbEntityRenderer<E extends MagicOrbEntity> extends EntityRenderer<E>{
	
	private final List<Integer> colours;
	
	public MagicOrbEntityRenderer(EntityRendererFactory.Context ctx, List<Integer> colours){
		super(ctx);
		this.colours = colours;
	}
	
	public Identifier getTexture(E entity){
		return null;
	}
	
	public void render(E entity, float yaw, float dt, MatrixStack ms, VertexConsumerProvider vcs, int light){
		super.render(entity, yaw, dt, ms, vcs, light);
		
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.enableDepthTest();
		RenderSystem.setShader(GameRenderer::getPositionColorTexLightmapProgram);
		RenderSystem.setShaderColor(1, 1, 1, 1);
		BufferBuilder vc = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL);
		
		ms.push();
		ms.translate(-0.0625, -0.0625, -0.0625);
		if(!entity.hasShot() && entity.getOwner() instanceof LivingEntity owner){
			// the entity will try to catch up to the owner, but it only does this every tick
			// make up the difference
			Vec3d diff = MathUtil.hoverPosition(owner, dt).subtract(MathUtil.hoverPosition(owner));
			ms.translate(diff.x, diff.y, diff.z);
		}
		
		float time = entity.age + dt;
		RenderSystem.setShaderTexture(0, arcId("textures/misc/white.png"));
		// 6 orbs following paths that look like rotating around the diagonal of a sphere,
		// with either dimension's frequency scaled, and the object's size scaled
		float eSize = entity.getSize();
		for(int xf = 1; xf < 4; xf++)
			for(int yf = 0; yf < 4; yf++){
				float cDist = 0.03f + 0.12f * eSize;
				float cSize = 0.05f + 0.13f * eSize;
				RenderHelper.colCuboid(vc,
						ms,
						colours.get(((xf - 1) + yf * 3) % colours.size()) | 0xFF000000,
						MathUtil.facingToVec(
								(float)(Math.sin(time * xf / 7f) * Math.PI),
								(float)(Math.cos(time * yf / 7f) * Math.PI)).multiply(cDist),
						cSize, cSize, cSize,
						0, 0, 1, 1,
						false);
			}
		ms.pop();
		
		RenderSystem.setShaderColor(1, 1, 1, 1);
		RenderHelper.drawBuffer(vc);
		RenderSystem.disableBlend();
	}
}