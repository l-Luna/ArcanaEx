package arcana.client.particles;

import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;
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
	                               SpriteProvider spr){
		super(world, x, y, z);
		this.velocityX = vX;
		this.velocityY = vY;
		this.velocityZ = vZ;
		this.spr = spr;
		setSpriteForAge(spr);
	}
	
	public void tick(){
		super.tick();
		setSpriteForAge(spr);
	}
	
	public ParticleTextureSheet getType(){
		return ParticleTextureSheet.PARTICLE_SHEET_LIT;
	}
	
	public static class Factory implements ParticleFactory<SimpleParticleType>{
		
		private final SpriteProvider spr;
		
		private final float gravity;
		private final float drag;
		private boolean collidable = true;
		
		private int minLifetime = 30, maxLifetime = 30;
		private float minScale = 1, maxScale = 1;
		private float r = 1, g = 1, b = 1;
		private boolean randomiseAngle = false;
		
		public Factory(SpriteProvider spr, float gravity, float drag){
			this.spr = spr;
			this.gravity = gravity;
			this.drag = drag;
		}
		
		public Factory lifetime(int lifetime){
			minLifetime = maxLifetime = lifetime;
			return this;
		}
		
		public Factory lifetime(int min, int max){
			minLifetime = min;
			maxLifetime = max;
			return this;
		}
		
		public Factory scale(float scale){
			minScale = maxScale = scale;
			return this;
		}
		
		public Factory scale(float min, float max){
			minScale = min;
			maxScale = max;
			return this;
		}
		
		public Factory tint(float r, float g, float b){
			this.r = r;
			this.g = g;
			this.b = b;
			return this;
		}
		
		public Factory randomAngle(){
			randomiseAngle = true;
			return this;
		}
		
		public Factory collidable(boolean collidable){
			this.collidable = collidable;
			return this;
		}
		
		@Nullable
		public Particle createParticle(SimpleParticleType params, ClientWorld world, double x, double y, double z, double vX, double vY, double vZ){
			SimpleSpriteParticle particle = new SimpleSpriteParticle(world, x, y, z, vX, vY, vZ, spr);
			particle.gravityStrength = gravity;
			particle.velocityMultiplier = 1 - drag;
			particle.maxAge = minLifetime + (int)(world.random.nextFloat() * (maxLifetime - minLifetime));
			particle.scale(minScale + world.random.nextFloat() * (maxScale - minScale));
			particle.setColor(r, g, b);
			particle.collidesWithWorld = collidable;
			if(randomiseAngle)
				particle.prevAngle = particle.angle = 0.5f * MathHelper.PI * world.random.nextInt(4);
			return particle;
		}
	}
}