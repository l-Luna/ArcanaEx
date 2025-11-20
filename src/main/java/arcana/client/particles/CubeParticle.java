package arcana.client.particles;

import arcana.client.RenderHelper;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

public class CubeParticle extends Particle{
	
	private final Sprite sprite;
	private final CubeParticleStyle style;
	
	protected CubeParticle(ClientWorld world, double x, double y, double z, Sprite sprite, CubeParticleStyle style){
		super(world, x, y, z);
		this.sprite = sprite;
		this.style = style;
		maxAge = style.maxLife();
	}
	
	public void buildGeometry(VertexConsumer vc, Camera camera, float tickDelta){
		float age = (this.age + tickDelta) / (maxAge + 1);
		float s = 1.01f * style.easeScale(age), o = (s - 1) / 2;
		int alpha1 = (int)(style.easeAlpha(age) * 200);
		int c = ColorHelper.Argb.getArgb(alpha1, (int)(red * 255), (int)(green * 255), (int)(blue * 255));
		RenderHelper.colCuboid(vc, new MatrixStack(), c, new Vec3d(x, y, z).subtract(camera.getPos()).subtract(o, o, o), s, sprite, true);
	}
	
	public ParticleTextureSheet getType(){
		return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
	}
	
	public static final class Factory implements ParticleFactory<CubeParticleEffect>{
		
		private final SpriteProvider sprite;
		
		public Factory(SpriteProvider sprite){
			this.sprite = sprite;
		}
		
		public @NotNull Particle createParticle(CubeParticleEffect parameters,
		                                        ClientWorld world,
		                                        double x,
		                                        double y,
		                                        double z,
		                                        double velocityX,
		                                        double velocityY,
		                                        double velocityZ){
			return new CubeParticle(world, x, y, z, sprite.getSprite(world.random), parameters.style());
		}
	}
}