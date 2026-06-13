package arcana.entities;

import arcana.ArcanaRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.DustColorTransitionParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.world.World;
import org.joml.Vector3f;

public class ThrownAlumentumEntity extends Entity{
	
	public ThrownAlumentumEntity(EntityType<?> type, World world){
		super(type, world);
	}
	
	public static ThrownAlumentumEntity create(World w){
		return new ThrownAlumentumEntity(ArcanaRegistry.THROWN_ALUMENTUM, w);
	}
	
	protected void initDataTracker(DataTracker.Builder builder){}
	
	public void tick(){
		super.tick();
		
		if(getWorld().isClient && age > 2){
			// Add particles
			var rng = getWorld().random;
			getWorld().addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, getX(), getY(), getZ(), rng.nextGaussian() / 16, 0.1, rng.nextGaussian() / 16);
			getWorld().addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, getX(), getY(), getZ(), rng.nextGaussian() / 12, 0.1, rng.nextGaussian() / 12);
			getWorld().addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, getX(), getY(), getZ(), rng.nextGaussian() / 9, 0.1, rng.nextGaussian() / 9);
			for(int i = 0; i < 3; i++){
				getWorld().addParticle(new DustColorTransitionParticleEffect(new Vector3f(1, 1, 1), new Vector3f(.5f, 1, .5f), 3),
						getX() + rng.nextGaussian() / 6, getY() + rng.nextGaussian() / 6, getZ() + rng.nextGaussian() / 6, rng.nextGaussian() * 2, 1, rng.nextGaussian() * 2);
			}
		}
		
		// Gravity
		if(!hasNoGravity())
			setVelocity(getVelocity().add(0, -.04d, 0));
		move(MovementType.SELF, getVelocity());
		setVelocity(getVelocity().multiply(.98d));
		
		if(!getWorld().isClient && (horizontalCollision || verticalCollision)){
			// Explode when touching something
			explode();
			discard();
		}
	}
	
	private void explode() {
		getWorld().createExplosion(this, getDamageSources().explosion(null), null, getX(), getBodyY(.0625), getZ(), 7, false, World.ExplosionSourceType.TNT);
	}
	
	protected void readCustomDataFromNbt(NbtCompound nbt){}
	
	protected void writeCustomDataToNbt(NbtCompound nbt){}
}