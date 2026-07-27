package arcana.entities;

import arcana.util.MathUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public abstract class MagicOrbEntity extends ProjectileEntity{
	
	private static final TrackedData<Float> SIZE = DataTracker.registerData(MagicOrbEntity.class, TrackedDataHandlerRegistry.FLOAT);
	private static final TrackedData<Boolean> SHOT = DataTracker.registerData(MagicOrbEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	
	public MagicOrbEntity(EntityType<? extends ProjectileEntity> entityType, World world){
		super(entityType, world);
	}
	
	public void release(@Nullable Entity target){
		setShot(true);
		Entity owner = getOwner();
		if(target != null)
			setVelocity(target.getPos().subtract(getPos()).normalize().multiply(1.8f));
		else if(owner != null)
			setVelocity(owner, owner.getPitch(), owner.getYaw(), 0, 1.8f, 0.7f);
		else
			setVelocity(getWorld().random.nextDouble(), getWorld().random.nextDouble(), getWorld().random.nextDouble(), 1f, 0f);
	}
	
	public void tick(){
		Entity ownerEntity = getOwner();
		if(!hasShot()){
			if(ownerEntity instanceof LivingEntity owner){
				setPosition(MathUtil.hoverPosition(owner));
				velocityDirty = true;
				float size = getSize();
				if(size < 1f)
					setSize(Math.min(1, size + 1 / 30f));
			}
		}
		
		if(!getWorld().isClient){
			if(ownerEntity == null || !ownerEntity.isAlive())
				burst();
			
			HitResult hitResult = ProjectileUtil.getCollision(this, this::canHit);
			if(hitResult.getType() != HitResult.Type.MISS)
				onCollision(hitResult);
		}
		
		setPosition(getPos().add(getVelocity()));
		
		super.tick();
	}
	
	protected void onCollision(HitResult hit){
		burst();
	}
	
	public void burst(){
		Entity owner = getOwner();
		float rad = radius();
		for(Entity entity : getWorld().getOtherEntities(this, new Box(getPos().subtract(rad, rad, rad), getPos().add(rad, rad, rad))))
			if(entity instanceof LivingEntity target && target != owner)
				damageEntity(target, owner);
		
		discard();
	}
	
	protected abstract void damageEntity(LivingEntity target, Entity owner);
	
	protected float radius(){
		return 1.5f;
	}
	
	protected boolean canHit(Entity entity){
		return hasShot() && entity != getOwner() && super.canHit(entity);
	}
	
	protected void initDataTracker(DataTracker.Builder builder){
		builder.add(SIZE, 0f);
		builder.add(SHOT, false);
	}
	
	public float getSize(){
		return dataTracker.get(SIZE);
	}
	
	public void setSize(float to){
		dataTracker.set(SIZE, to);
	}
	
	public boolean hasShot(){
		return dataTracker.get(SHOT);
	}
	
	public void setShot(boolean to){
		dataTracker.set(SHOT, to);
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
	
	public boolean shouldRender(double distance){
		// our hitbox is pretty small, so use the same distance that shulker bullets do
		return distance < 16384;
	}
}