package arcana.client.particles;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.particle.BlockDustParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.BlockStateParticleEffect;
import org.jetbrains.annotations.Nullable;

public class HungryNodeDiscParticle extends BlockDustParticle{
	
	protected HungryNodeDiscParticle(ClientWorld world,
	                                 double x,
	                                 double y,
	                                 double z,
	                                 double xSpeed,
	                                 double ySpeed,
	                                 double zSpeed,
	                                 BlockState state){
		super(world, x, y, z, xSpeed, ySpeed, zSpeed, state);
		collidesWithWorld = false;
		maxAge = 120;
		gravityStrength = 0;
		velocityX = velocityX * 0.01f + xSpeed;
		velocityY = velocityY * 0.01f + ySpeed;
		velocityZ = velocityZ * 0.01f + zSpeed;
	}
	
	public void tick(){
		prevPosX = x;
		prevPosY = y;
		prevPosZ = z;
		if(age++ >= maxAge)
			markDead();
		else{
			float time = age / 6f;
			// TODO: allow off-axis rings
			move(Math.cos(time) * velocityX, velocityY, Math.sin(time) * velocityZ);
		}
	}
	
	public static class Factory implements ParticleFactory<BlockStateParticleEffect>{
		
		@Nullable
		public Particle createParticle(BlockStateParticleEffect parameters,
		                               ClientWorld world,
		                               double x,
		                               double y,
		                               double z,
		                               double xSpeed,
		                               double ySpeed,
		                               double zSpeed){
			BlockState state = parameters.getBlockState();
			if(!state.isAir() && !state.isOf(Blocks.MOVING_PISTON))
				return new HungryNodeDiscParticle(world, x, y, z, xSpeed, ySpeed, zSpeed, state);
			return null;
		}
	}
}