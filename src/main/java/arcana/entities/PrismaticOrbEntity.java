package arcana.entities;

import arcana.items.foci.PrismaticLightFocusItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

public class PrismaticOrbEntity extends ProjectileEntity{
	
	private static final TrackedData<Float> size = DataTracker.registerData(PrismaticOrbEntity.class, TrackedDataHandlerRegistry.FLOAT);
	private static final TrackedData<Boolean> shot = DataTracker.registerData(PrismaticOrbEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	
	public PrismaticOrbEntity(EntityType<? extends PrismaticOrbEntity> type, World world){
		super(type, world);
	}
	
	public void release(){
		setShot(true);
		Entity owner = getOwner();
		if(owner != null)
			setVelocity(owner, owner.getPitch(), owner.getYaw(), 0, 1.8f, 0.7f);
		else
			setVelocity(world.random.nextDouble(), world.random.nextDouble(), world.random.nextDouble(), 1f, 0f);
	}
	
	public void tick(){
		if(!hasShot()){
			Entity owner = getOwner();
			if(owner instanceof PlayerEntity player){
				setPosition(PrismaticLightFocusItem.hoverPosition(player));
				velocityDirty = true;
				float size = getSize();
				if(size < 1f)
					setSize(Math.min(1, size + 1 / 30f));
			}
		}
		
		if(!world.isClient){
			HitResult hitResult = ProjectileUtil.getCollision(this, this::canHit);
			if(hitResult.getType() != HitResult.Type.MISS)
				onCollision(hitResult);
		}
		
		setPosition(getPos().add(getVelocity()));
		
		super.tick();
	}
	
	protected void onCollision(HitResult hit){
		super.onCollision(hit);
		// particle burst
		((ServerWorld)world).spawnParticles(ParticleTypes.END_ROD,
				getPos().getX(),
				getPos().getY(),
				getPos().getZ(),
				30,
				0,
				0,
				0,
				0.3f);
		
		// deal damage to all nearby entities
		Entity owner = getOwner();
		float d = 1.5f;
		for(Entity entity : world.getOtherEntities(this, new Box(getPos().subtract(d, d, d), getPos().add(d, d, d))))
			if(entity instanceof LivingEntity target){
				boolean undeadTarget = target.getGroup() == EntityGroup.UNDEAD;
				target.damage(DamageSource.magic(this, owner), getDamage(undeadTarget));
				if(undeadTarget)
					target.setOnFireFor(4);
			}
		
		discard();
	}
	
	public float getDamage(boolean undeadTarget){
		// 3 base damage + 4 charged damage + 4 undead bonus damage
		return (int)(3 + (4 * getSize()) + (undeadTarget ? 4 : 0));
	}
	
	protected boolean canHit(Entity entity){
		return hasShot() && entity != getOwner() && super.canHit(entity);
	}
	
	protected void initDataTracker(){
		dataTracker.startTracking(size, 0f);
		dataTracker.startTracking(shot, false);
	}
	
	public float getSize(){
		return dataTracker.get(size);
	}
	
	public void setSize(float to){
		dataTracker.set(size, to);
	}
	
	public boolean hasShot(){
		return dataTracker.get(shot);
	}
	
	public void setShot(boolean to){
		dataTracker.set(shot, to);
	}
	
	protected void readCustomDataFromNbt(NbtCompound nbt){
		super.readCustomDataFromNbt(nbt);
		setSize(nbt.getFloat("size"));
		setShot(nbt.getBoolean("shot"));
	}
	
	protected void writeCustomDataToNbt(NbtCompound nbt){
		super.writeCustomDataToNbt(nbt);
		nbt.putFloat("size", getSize());
		nbt.putBoolean("shot", hasShot());
	}
	
	public Packet<?> createSpawnPacket(){
		return new EntitySpawnS2CPacket(this);
	}
	
	public boolean shouldRender(double distance){
		// our hitbox is pretty small, so use the same distance that shulker bullets do
		return distance < 16384;
	}
}