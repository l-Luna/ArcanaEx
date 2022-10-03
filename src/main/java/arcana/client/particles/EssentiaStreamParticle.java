package arcana.client.particles;

import arcana.aspects.Aspect;
import arcana.blocks.InfusionMatrixBlockEntity;
import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColorHelper.Argb;
import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;

public class EssentiaStreamParticle extends SpriteBillboardParticle{
	
	private final Vec3f origin;
	private final Matrix3f rotation;
	
	private final SpriteProvider spr;
	
	protected EssentiaStreamParticle(ClientWorld world,
	                                 double x,
	                                 double y,
	                                 double z,
	                                 double angleX,
	                                 double angleY,
	                                 SpriteProvider spr,
	                                 Aspect aspect){
		super(world, x, y, z, 0, 0, 0);
		gravityStrength = 0;
		collidesWithWorld = false;
		origin = new Vec3f((float)x, (float)y, (float)z);
		rotation = new Matrix3f(Quaternion.fromEulerYxz((float)angleX, (float)angleY, 0));
		maxAge = 80;
		setSpriteForAge(spr);
		var newPos = posWhen(age);
		setPos(newPos.getX(), newPos.getY(), newPos.getZ());
		// avoid initial stuttering
		prevPosX = newPos.getX();
		prevPosY = newPos.getY();
		prevPosZ = newPos.getZ();
		setColor(Argb.getRed(aspect.colour()) / 255f, Argb.getGreen(aspect.colour()) / 255f, Argb.getBlue(aspect.colour()) / 255f);
		this.spr = spr;
	}
	
	public void tick(){
		velocityX = velocityY = velocityZ = 0;
		super.tick();
		var newPos = posWhen(age);
		setPos(newPos.getX(), newPos.getY(), newPos.getZ());
		setSpriteForAge(spr);
		// ...end when we reach a matrix...
		Vec3f toC = new Vec3f(0, age / 12f, 0);
		toC.transform(rotation);
		toC.add(origin);
		if(world.getBlockEntity(new BlockPos(toC.getX(), toC.getY() + 1, toC.getZ())) instanceof InfusionMatrixBlockEntity
				// is the central line roughly in the middle
				&& Math.abs(toC.getX() - Math.floor(toC.getX()) - .5) < .1
				&& Math.abs(toC.getZ() - Math.floor(toC.getZ()) - .5) < .1)
			markDead();
	}
	
	public ParticleTextureSheet getType(){
		return ParticleTextureSheet.PARTICLE_SHEET_LIT;
	}
	
	private Vec3f posWhen(int t){
		final float r = .15f;
		int rand = System.identityHashCode(this) % 16; // chosen by fair memory alloc
		Vec3f to = new Vec3f((float)(r * Math.sin(t / 4f + rand)), t / 12f, (float)(r * Math.cos(t / 4f + rand)));
		to.transform(rotation);
		to.add(origin);
		return to;
	}
	
	public static class Factory implements ParticleFactory<AspectParticleEffect>{
		
		private final SpriteProvider sprite;
		
		public Factory(SpriteProvider sprite){
			this.sprite = sprite;
		}
		
		public Particle createParticle(AspectParticleEffect parameters,
		                               ClientWorld world,
		                               double x,
		                               double y,
		                               double z,
		                               double dx,
		                               double dy,
		                               double dz){
			return new EssentiaStreamParticle(world, x, y, z, dx, dy, sprite, parameters.getAspect());
		}
	}
}