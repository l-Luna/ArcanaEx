package arcana.entities;

import arcana.ArcanaRegistry;
import arcana.ArcanaTags;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.EntityDamageSource;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class WispEntity extends WispLikeEntity implements Angerable{
	
	// where the spawning node is
	private BlockPos anchor;
	
	// for angerable
	private int angerTime;
	private @Nullable UUID angryAt;
	
	public WispEntity(EntityType<? extends WispEntity> entityType, World world){
		super(entityType, world);
	}
	
	public WispEntity(World world){
		this(ArcanaRegistry.WISP, world);
	}
	
	protected void initGoals(){
		super.initGoals();
		targetSelector.add(4, new ActiveTargetGoal<>(this, PlayerEntity.class, 10, true, false, this::shouldAngerAt));
	}
	
	public static DefaultAttributeContainer.Builder createDefaultAttributes(){
		return MobEntity.createMobAttributes()
				.add(EntityAttributes.GENERIC_MAX_HEALTH, 20)
				.add(EntityAttributes.GENERIC_FLYING_SPEED, 1)
				.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 1)
				.add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 4)
				.add(EntityAttributes.GENERIC_FOLLOW_RANGE, 60);
	}
	
	public void setAnchorPos(BlockPos anchor){
		this.anchor = anchor;
	}
	
	Vec3d anchor(){
		return anchor != null ? Vec3d.ofCenter(anchor) : getPos();
	}
	
	public void tick(){
		super.tick();
		if(!world.isClient)
			tickAngerLogic((ServerWorld)world, false);
	}
	
	public boolean handleAttack(Entity attacker){
		return !(attacker instanceof LivingEntity le) || !le.getMainHandStack().isIn(ArcanaTags.WISP_WEAPONS);
	}
	
	public boolean canHit(){
		// false by default here, but actually handled in GameRendererMixin
		return false;
	}
	
	public boolean isInvulnerableTo(DamageSource damageSource){
		if(damageSource.isOutOfWorld())
			return false;
		return !(damageSource instanceof EntityDamageSource eds)
				|| handleAttack(eds.getAttacker())
				|| eds.isExplosive()
				|| eds.isFire()
				|| eds.isThorns()
				|| eds.isMagic();
	}
	
	// start items with no velocity
	public @Nullable ItemEntity dropStack(ItemStack stack, float yOffset){
		if(stack.isEmpty())
			return null;
		else if(world.isClient)
			return null;
		else{
			ItemEntity entity = new ItemEntity(world, getX(), getY() + yOffset, getZ(), stack, 0, 0, 0);
			entity.setToDefaultPickupDelay();
			world.spawnEntity(entity);
			return entity;
		}
	}
	
	public void writeCustomDataToNbt(NbtCompound nbt){
		super.writeCustomDataToNbt(nbt);
		writeAngerToNbt(nbt);
	}
	
	public void readCustomDataFromNbt(NbtCompound nbt){
		super.readCustomDataFromNbt(nbt);
		readAngerFromNbt(world, nbt);
	}
	
	// for angerable
	
	public int getAngerTime(){
		return angerTime;
	}
	
	public void setAngerTime(int angerTime){
		this.angerTime = angerTime;
	}
	
	public @Nullable UUID getAngryAt(){
		return angryAt;
	}
	
	public void setAngryAt(@Nullable UUID angryAt){
		this.angryAt = angryAt;
	}
	
	public void chooseRandomAngerTime(){
		setAngerTime(world.random.nextBetween(20 * 25, 20 * 40));
	}
}