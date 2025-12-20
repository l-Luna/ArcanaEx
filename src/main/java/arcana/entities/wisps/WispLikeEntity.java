package arcana.entities.wisps;

import arcana.ArcanaRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.EnumSet;

public abstract class WispLikeEntity extends PathAwareEntity{
	
	// for rendering angry wisps
	private static final TrackedData<Boolean> ANGRY = DataTracker.registerData(WispLikeEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	
	protected WispLikeEntity(EntityType<? extends WispLikeEntity> entityType, World world){
		super(entityType, world);
		moveControl = new WispMoveControl(this);
	}
	
	//
	
	public abstract Vec3d anchor();
	
	//
	
	protected void initGoals(){
		goalSelector.add(0, new SwimGoal(this));
		goalSelector.add(4, new StrafeTargetGoal(this));
		goalSelector.add(5, new ChargeTargetGoal(this));
		goalSelector.add(10, new FloatAroundGoal(this));
		targetSelector.add(2, new RevengeGoal(this));
	}
	
	protected void initDataTracker(){
		super.initDataTracker();
		dataTracker.startTracking(ANGRY, false);
	}
	
	public void setAngry(boolean angry){
		dataTracker.set(ANGRY, angry);
	}
	
	public boolean angry(){
		return dataTracker.get(ANGRY);
	}
	
	public void tick(){
		noClip = true;
		super.tick();
		noClip = false;
		if(world.isClient)
			world.addParticle(ArcanaRegistry.LIGHTNING,
					getX() + random.nextGaussian() * 0.1f,
					getY() + (getHeight() / 2f) + random.nextGaussian() * 0.1f,
					getZ() + random.nextGaussian() * 0.1f,
					-getVelocity().x * 0.2f + random.nextGaussian() * 0.03f,
					-getVelocity().y * 0.2f + random.nextGaussian() * 0.03f,
					-getVelocity().z * 0.2f + random.nextGaussian() * 0.03f);
	}
	
	public boolean hasNoGravity(){
		return true;
	}
	
	protected EntityNavigation createNavigation(World world){
		BirdNavigation nav = new BirdNavigation(this, world);
		nav.setCanPathThroughDoors(false);
		nav.setCanSwim(false);
		nav.setCanEnterOpenDoors(false);
		return nav;
	}
	
	public void move(MovementType movementType, Vec3d movement){
		super.move(movementType, movement);
		checkBlockCollision();
	}
	
	public void setTarget(LivingEntity target){
		super.setTarget(target);
		setAngry(target != null);
	}
	
	protected static class FloatAroundGoal extends Goal{
		private final WispLikeEntity entity;
		
		public FloatAroundGoal(WispLikeEntity entity){
			this.entity = entity;
			setControls(EnumSet.of(Goal.Control.MOVE));
		}
		
		public boolean canStart(){
			return entity.navigation.isIdle()
					&& entity.random.nextInt(toGoalTicks(7)) == 0
					&& entity.getTarget() == null;
		}
		
		public boolean shouldContinue(){
			return entity.navigation.isFollowingPath();
		}
		
		public void start(){
			Vec3d anchor = entity.anchor();
			if(anchor == null)
				anchor = entity.getPos();
			
			for(int i = 0; i < 3; i++){
				var rng = entity.random;
				Vec3d target = anchor.add(rng.nextBetween(-10, 10), rng.nextBetween(-4, 8), rng.nextBetween(-10, 10));
				BlockPos targetPos = new BlockPos(target);
				if(entity.world.isAir(targetPos))
					entity.moveControl.moveTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 0.6);
			}
		}
	}
	
	protected static class StrafeTargetGoal extends Goal{
		private final WispLikeEntity entity;
		
		public StrafeTargetGoal(WispLikeEntity entity){
			this.entity = entity;
			setControls(EnumSet.of(Goal.Control.MOVE));
		}
		
		public boolean canStart(){
			Entity target = entity.getTarget();
			return target != null
					&& target.isAlive()
					&& !entity.moveControl.isMoving()
					&& entity.random.nextInt(toGoalTicks(7)) == 0
					&& (entity.squaredDistanceTo(target) <= 5 * 5 || Math.abs(entity.getY() - target.getY()) >= 1.5f);
		}
		
		public boolean shouldContinue(){
			Entity target = entity.getTarget();
			return entity.moveControl.isMoving()
					&& target != null
					&& target.isAlive();
		}
		
		public boolean shouldRunEveryTick(){
			return true;
		}
		
		public void tick(){
			Entity target = entity.getTarget();
			if(target == null)
				return;
			
			// if the wisp is in the correct ring, eventually stop strafing
			Vec3d diff = target.getPos().subtract(entity.getPos());
			Vec3d hDir = diff.multiply(1, 0, 1).normalize();
			boolean satisfied = diff.lengthSquared() > 5 * 5
					&& Math.abs(diff.y) < 0.7f;
			if(satisfied && entity.random.nextInt(toGoalTicks(20)) == 0)
				stop();
			
			Vec3d vel = entity.getVelocity();
			vel = vel.add(0, MathHelper.clamp(diff.y, -1, 1) * 0.1f, 0);
			vel = vel.add(hDir.negate().multiply(0.2f));
			entity.setVelocity(vel);
		}
	}
	
	protected static class ChargeTargetGoal extends Goal{
		private final WispLikeEntity entity;
		
		public ChargeTargetGoal(WispLikeEntity entity){
			this.entity = entity;
			setControls(EnumSet.of(Goal.Control.MOVE));
		}
		
		public boolean canStart(){
			Entity target = entity.getTarget();
			return target != null
					&& target.isAlive()
					&& !entity.moveControl.isMoving()
					&& entity.random.nextInt(toGoalTicks(7)) == 0
					&& entity.squaredDistanceTo(target) > 5 * 5
					&& Math.abs(entity.getY() - target.getY()) < 1.5f;
		}
		
		public boolean shouldContinue(){
			Entity target = entity.getTarget();
			return entity.moveControl.isMoving()
					&& target != null
					&& target.isAlive();
		}
		
		public void start(){
			Entity target = entity.getTarget();
			if(target == null)
				return;
			
			Vec3d dir = target.getPos().subtract(entity.getPos()).normalize();
			Vec3d targetPos = target.getPos().add(dir.multiply(5));
			entity.moveControl.moveTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 2.6f);
			entity.setVelocity(entity.getVelocity().multiply(1, 0, 1));
		}
		
		public boolean shouldRunEveryTick(){
			return true;
		}
		
		public void tick(){
			Entity target = entity.getTarget();
			if(target == null)
				return;
			
			if(entity.getBoundingBox().intersects(target.getBoundingBox()))
				entity.tryAttack(target);
		}
	}
	
	protected static class WispMoveControl extends MoveControl{
		
		public WispMoveControl(WispLikeEntity owner){
			super(owner);
		}
		
		public void tick(){
			if(this.state == MoveControl.State.MOVE_TO){
				Vec3d diff = new Vec3d(targetX - entity.getX(), targetY - entity.getY(), targetZ - entity.getZ());
				if(diff.length() < entity.getBoundingBox().getAverageSideLength()){
					state = MoveControl.State.WAIT;
					entity.setVelocity(entity.getVelocity().multiply(0.5));
				}else
					entity.setVelocity(entity.getVelocity().add(diff.multiply(speed * 0.05 / diff.length())));
				
				if(entity.horizontalCollision || entity.verticalCollision)
					state = State.WAIT;
			}
		}
	}
}