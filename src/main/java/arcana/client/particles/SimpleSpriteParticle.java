package arcana.client.particles;

import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

public class SimpleSpriteParticle extends SpriteBillboardParticle{
	
	private final SpriteProvider spr;
	
	protected SimpleSpriteParticle(ClientWorld world,
	                               double x,
	                               double y,
	                               double z,
	                               double vX,
	                               double vY,
	                               double vZ,
	                               SpriteProvider spr,
	                               float gravity,
	                               float drag,
	                               int maxAge,
	                               float scale,
	                               boolean randomiseAngle){
		super(world, x, y, z);
		this.velocityX = vX;
		this.velocityY = vY;
		this.velocityZ = vZ;
		this.spr = spr;
		this.gravityStrength = gravity;
		this.velocityMultiplier = 1 - drag;
		this.maxAge = maxAge;
		this.scale *= scale; // keep randomness from BillboardParticle
		if(randomiseAngle)
			this.prevAngle = this.angle = 0.5f * MathHelper.PI * world.random.nextInt(4);
		setSpriteForAge(spr);
	}
	
	public void tick(){
		super.tick();
		setSpriteForAge(spr);
	}
	
	public ParticleTextureSheet getType(){
		return ParticleTextureSheet.PARTICLE_SHEET_LIT;
	}
	
	public static class Factory implements ParticleFactory<DefaultParticleType>{
		
		private final SpriteProvider spr;
		
		private final float gravity;
		private final float drag;
		private final int maxAge;
		private final float scale;
		private final boolean randomiseAngle;
		
		public Factory(SpriteProvider spr, float gravity, float drag, int maxAge, float scale, boolean randomiseAngle){
			this.spr = spr;
			this.gravity = gravity;
			this.drag = drag;
			this.maxAge = maxAge;
			this.scale = scale;
			this.randomiseAngle = randomiseAngle;
		}
		
		@Nullable
		public Particle createParticle(DefaultParticleType params, ClientWorld world, double x, double y, double z, double vX, double vY, double vZ){
			return new SimpleSpriteParticle(world, x, y, z, vX, vY, vZ, spr, gravity, drag, maxAge, scale, randomiseAngle);
		}
	}
}