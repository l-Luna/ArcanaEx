package arcana.client.particles;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.particle.BlockDustParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

public class HungryNodeBlockParticle extends BlockDustParticle{
	
	public HungryNodeBlockParticle(ClientWorld world,
	                               double x,
	                               double y,
	                               double z,
	                               double xSpeed,
	                               double ySpeed,
	                               double zSpeed,
	                               BlockState state){
		super(world, x, y, z, xSpeed, ySpeed, zSpeed, state);
		collidesWithWorld = false;
		gravityStrength = 0;
		maxAge = 20;
		velocityX = velocityX * 0.01f + xSpeed;
		velocityY = velocityY * 0.01f + ySpeed;
		velocityZ = velocityZ * 0.01f + zSpeed;
		Random rand = world.random;
		this.x += (rand.nextFloat() - rand.nextFloat()) * .05f;
		this.y += (rand.nextFloat() - rand.nextFloat()) * .05f;
		this.z += (rand.nextFloat() - rand.nextFloat()) * .05f;
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
				return new HungryNodeBlockParticle(world, x, y, z, xSpeed, ySpeed, zSpeed, state);
			return null;
		}
	}
}