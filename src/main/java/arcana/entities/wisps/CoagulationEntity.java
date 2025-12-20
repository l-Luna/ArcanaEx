package arcana.entities.wisps;

import arcana.ArcanaRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Tameable;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.TrackTargetGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.UUID;

public class CoagulationEntity extends WispLikeEntity implements Tameable{
	
	private UUID ownerId;
	
	public CoagulationEntity(EntityType<? extends WispLikeEntity> entityType, World world){
		super(entityType, world);
	}
	
	public CoagulationEntity(World world, UUID ownerId){
		this(ArcanaRegistry.COAGULATION, world);
		this.ownerId = ownerId;
	}
	
	protected void initGoals(){
		super.initGoals();
		targetSelector.add(4, new DietAttackWithOwnerGoal<>(this));
	}
	
	public static DefaultAttributeContainer.Builder createDefaultAttributes(){
		return MobEntity.createMobAttributes()
				.add(EntityAttributes.GENERIC_MAX_HEALTH, 20)
				.add(EntityAttributes.GENERIC_FLYING_SPEED, 1)
				.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 1)
				.add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 4)
				.add(EntityAttributes.GENERIC_FOLLOW_RANGE, 60);
	}
	
	public void writeCustomDataToNbt(NbtCompound nbt){
		super.writeCustomDataToNbt(nbt);
		if(ownerId != null)
			nbt.putUuid("OwnerId", ownerId);
	}
	
	public void readCustomDataFromNbt(NbtCompound nbt){
		super.readCustomDataFromNbt(nbt);
		ownerId = nbt.containsUuid("OwnerId") ? nbt.getUuid("OwnerId") : null;
	}
	
	public Vec3d anchor(){
		Entity owner = getOwner();
		return owner != null ? owner.getPos() : getPos();
	}
	
	public @Nullable UUID getOwnerUuid(){
		return ownerId;
	}
	
	public @Nullable Entity getOwner(){
		return ownerId != null ? world.getPlayerByUuid(ownerId) : null;
	}
	
	public static class DietAttackWithOwnerGoal<TameableEntity extends MobEntity & Tameable> extends TrackTargetGoal{
		private final TameableEntity tameable;
		private LivingEntity attacking;
		private int lastAttackTime;
		
		public DietAttackWithOwnerGoal(TameableEntity tameable){
			super(tameable, false);
			this.tameable = tameable;
			this.setControls(EnumSet.of(Goal.Control.TARGET));
		}
		
		public boolean canStart(){
			Entity e = tameable.getOwner();
			if(!(e instanceof LivingEntity le))
				return false;
			else{
				attacking = le.getAttacking();
				int i = le.getLastAttackTime();
				return i != lastAttackTime && canTrack(attacking, TargetPredicate.DEFAULT);
			}
		}
		
		public void start(){
			mob.setTarget(attacking);
			Entity e = tameable.getOwner();
			if(e instanceof LivingEntity le)
				lastAttackTime = le.getLastAttackTime();
			
			super.start();
		}
	}
}