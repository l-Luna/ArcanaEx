package arcana.client.particles;

import arcana.client.RenderHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.render.*;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

public class CubeParticle extends Particle{
	
	private final Sprite sprite;
	private final CubeParticleStyle style;
	private final float effectR, effectB, effectG;
	
	protected CubeParticle(ClientWorld world, double x, double y, double z, Sprite sprite, CubeParticleStyle style, float effectR, float effectG, float effectB){
		super(world, x, y, z);
		this.sprite = sprite;
		this.style = style;
		this.effectR = effectR;
		this.effectB = effectG;
		this.effectG = effectG;
		maxAge = style.maxLife();
	}
	
	public void buildGeometry(VertexConsumer vc, Camera camera, float tickDelta){
		// TODO: veiling
		RenderSystem.setShader(GameRenderer::getPositionColorTexLightmapProgram);
		//RenderSystem.getShader().getUniformOrDefault("TurbulenceColor").set(effectR, effectG, effectB);
		
		RenderSystem.enableBlend();
		RenderSystem.depthMask(true);
		RenderSystem.setShaderTexture(0, SpriteAtlasTexture.PARTICLE_ATLAS_TEXTURE);
		BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL);
		
		float age = (this.age + tickDelta) / (maxAge + 1);
		float s = 1.01f * style.easeScale(age), o = (s - 1) / 2;
		int alpha1 = (int)(style.easeAlpha(age) * 200);
		int c = ColorHelper.Argb.getArgb(alpha1, (int)(red * 255), (int)(green * 255), (int)(blue * 255));
		Vec3d d = new Vec3d(x, y, z).add(style.easeOffset(age, random));
		RenderHelper.colCuboid(buffer, new MatrixStack(), c, d.subtract(camera.getPos()).subtract(o, o, o), s, sprite, false);
		
		RenderHelper.drawBuffer(buffer);
		
		RenderSystem.setShader(GameRenderer::getParticleProgram);
	}
	
	public ParticleTextureSheet getType(){
		return ParticleTextureSheet.CUSTOM;
	}
	
	public static final class Factory implements ParticleFactory<CubeParticleEffect>{
		
		private final SpriteProvider sprite;
		private final float effectR, effectG, effectB;
		
		public Factory(SpriteProvider sprite, float effectR, float effectG, float effectB){
			this.sprite = sprite;
			this.effectR = effectR;
			this.effectG = effectG;
			this.effectB = effectB;
		}
		
		public @NotNull Particle createParticle(CubeParticleEffect parameters,
		                                        ClientWorld world,
		                                        double x,
		                                        double y,
		                                        double z,
		                                        double velocityX,
		                                        double velocityY,
		                                        double velocityZ){
			return new CubeParticle(world, x, y, z, sprite.getSprite(world.random), parameters.style(), effectR, effectG, effectB);
		}
	}
}