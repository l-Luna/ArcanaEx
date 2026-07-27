package arcana.entities;

import arcana.ArcanaDamageSources;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

public class PrismaticOrbEntity extends MagicOrbEntity{
	
	private static final TrackedData<Boolean> BURNING = DataTracker.registerData(PrismaticOrbEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	
	public PrismaticOrbEntity(EntityType<? extends PrismaticOrbEntity> type, World world){
		super(type, world);
	}
	
	public void tick(){
		if(getWorld().isClient && isBurning() && getWorld().random.nextFloat() < 0.2f)
			getWorld().addParticle(ParticleTypes.DRIPPING_LAVA,
					getPos().x, getPos().y, getPos().z,
					getVelocity().x, getVelocity().y, getVelocity().z);
		super.tick();
	}
	
	public void burst(){
		// particle burst
		ServerWorld sw = (ServerWorld)getWorld();
		sw.spawnParticles(ParticleTypes.END_ROD,
				getPos().getX(),
				getPos().getY(),
				getPos().getZ(),
				30, 0, 0, 0, 0.08f);
		if(isBurning())
			sw.spawnParticles(ParticleTypes.FLAME,
					getPos().getX(),
					getPos().getY(),
					getPos().getZ(),
					7, 0, 0, 0, 0.2f);
		super.burst();
	}
	
	protected void damageEntity(LivingEntity target, Entity owner){
		boolean undeadTarget = target.getType().isIn(EntityTypeTags.UNDEAD);
		// 3 base damage + 4 charged damage + 4 undead bonus damage
		target.damage(ArcanaDamageSources.prismaticLight(getEntityWorld(), this), 3 + 4 * getSize() + (undeadTarget ? 4 : 0));
		if(undeadTarget)
			target.setOnFireFor(4);
		if(isBurning())
			target.setOnFireFor(8);
	}
	
	protected void initDataTracker(DataTracker.Builder builder){
		super.initDataTracker(builder);
		builder.add(BURNING, false);
	}
	
	public boolean isBurning(){
		return dataTracker.get(BURNING);
	}
	
	public void setBurning(boolean to){
		dataTracker.set(BURNING, to);
	}
	
	protected void readCustomDataFromNbt(NbtCompound nbt){
		super.readCustomDataFromNbt(nbt);
		setBurning(nbt.getBoolean("burning"));
	}
	
	protected void writeCustomDataToNbt(NbtCompound nbt){
		super.writeCustomDataToNbt(nbt);
		nbt.putBoolean("burning", isBurning());
	}
}