package arcana.entities;

import arcana.ArcanaRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.EnumSet;

public abstract class WispLikeEntity extends PathAwareEntity{
	
	protected WispLikeEntity(EntityType<? extends WispLikeEntity> entityType, World world){
		super(entityType, world);
		moveControl = new WispMoveControl(this);
	}
	
	//
	
	abstract Vec3d anchor();
	
	//
	
	protected void initGoals(){
		goalSelector.add(0, new SwimGoal(this));
		goalSelector.add(4, new StrafeTargetGoal(this));
		goalSelector.add(5, new ChargeTargetGoal(this));
		goalSelector.add(10, new FloatAroundGoal(this));
		targetSelector.add(2, new RevengeGoal(this));
	}
	
	public void tick(){
		super.tick();
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
	
	protected static class FloatAroundGoal extends Goal{
		private final WispLikeEntity entity;
		
		public FloatAroundGoal(WispLikeEntity entity){
			this.entity = entity;
			setControls(EnumSet.of(Goal.Control.MOVE));
		}
		
		public boolean canStart(){
			return entity.navigation.isIdle() && entity.random.nextInt(toGoalTicks(7)) == 0 && entity.getTarget() == null;
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
				if(entity.world.isAir(targetPos)){
					BlockHitResult cast = entity.world.raycast(new RaycastContext(entity.getPos(), Vec3d.ofCenter(targetPos), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, entity));
					if(cast.getType() == HitResult.Type.MISS)
						entity.moveControl.moveTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 0.6);
				}
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
					&& entity.squaredDistanceTo(target) <= 5*5;
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
			if(target == null) return;
			
			// if the wisp is in the correct ring, eventually stop strafing
			Vec3d diff = target.getPos().subtract(entity.getPos());
			Vec3d hDir = diff.multiply(1, 0, 1).normalize();
			boolean satisfied = diff.lengthSquared() > 5 * 5
					&& diff.lengthSquared() < 6 * 6
					&& Math.abs(diff.y) < 0.5f;
			if(satisfied && entity.random.nextInt(toGoalTicks(20)) == 0)
				stop();
			
			Vec3d vel = entity.getVelocity();
			
			// voted #1 jank 2025
			// try fix Y position
			vel = vel.add(0, diff.y * 0.3f, 0);
			// try fix horizontal distance
			vel = vel.add(hDir.negate().multiply((diff.length() - 5.5f) * 0.3f));
			// add a rightwards drift
			vel = vel.add(hDir.rotateY(MathHelper.PI/2).multiply(0.1f));
			
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
					&& entity.squaredDistanceTo(target) > 5*5;
		}
		
		public boolean shouldContinue(){
			Entity target = entity.getTarget();
			return entity.moveControl.isMoving()
					&& target != null
					&& target.isAlive();
		}
		
		public void start(){
			Entity target = entity.getTarget();
			if(target == null) return;
			
			Vec3d dir = target.getPos().subtract(entity.getPos()).normalize();
			Vec3d targetPos = target.getPos().add(dir.multiply(5));
			entity.moveControl.moveTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 2.3f);
		}
		
		public boolean shouldRunEveryTick(){
			return true;
		}
		
		public void tick(){
			Entity target = entity.getTarget();
			if(target == null) return;
			
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