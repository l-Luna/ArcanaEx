package arcana.entities;

import arcana.ArcanaRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.particle.DustColorTransitionParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;

public class ThrownAlumentumEntity extends Entity{
	
	public ThrownAlumentumEntity(EntityType<?> type, World world){
		super(type, world);
	}
	
	public ThrownAlumentumEntity(World world){
		this(ArcanaRegistry.THROWN_ALUMENTUM, world);
	}
	
	public void tick(){
		super.tick();
		
		if(world.isClient && age > 2){
			// Add particles
			var rng = world.random;
			world.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, getX(), getY(), getZ(), rng.nextGaussian() / 16, 0.1, rng.nextGaussian() / 16);
			world.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, getX(), getY(), getZ(), rng.nextGaussian() / 12, 0.1, rng.nextGaussian() / 12);
			world.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, getX(), getY(), getZ(), rng.nextGaussian() / 9, 0.1, rng.nextGaussian() / 9);
			for(int i = 0; i < 3; i++){
				world.addParticle(new DustColorTransitionParticleEffect(new Vec3f(1, 1, 1), new Vec3f(.5f, 1, .5f), 3),
						getX() + rng.nextGaussian() / 6, getY() + rng.nextGaussian() / 6, getZ() + rng.nextGaussian() / 6, rng.nextGaussian() * 2, 1, rng.nextGaussian() * 2);
			}
		}
		
		// Gravity
		if(!hasNoGravity())
			setVelocity(getVelocity().add(0, -.04d, 0));
		move(MovementType.SELF, getVelocity());
		setVelocity(getVelocity().multiply(.98d));
		
		if(horizontalCollision || verticalCollision){
			// Explode when touching something
			explode();
			discard();
		}
	}
	
	private void explode() {
		world.createExplosion(this, DamageSource.explosion((LivingEntity)null), null, getX(), getBodyY(.0625), getZ(), 7, false, Explosion.DestructionType.DESTROY);
	}
	
	protected void initDataTracker(){}
	
	protected void readCustomDataFromNbt(NbtCompound nbt){}
	
	protected void writeCustomDataToNbt(NbtCompound nbt){}
	
	public Packet<?> createSpawnPacket(){
		return new EntitySpawnS2CPacket(this);
	}
}