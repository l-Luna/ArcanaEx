package arcana.entities;

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
		goalSelector.add(10, new FloatAroundGoal(this));
		targetSelector.add(2, new RevengeGoal(this));
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
			return entity.navigation.isIdle() && entity.random.nextInt(toGoalTicks(7)) == 0;
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
	
	protected static class ChargeAttackGoal extends Goal{
		
		public boolean canStart(){
			return false;
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
					entity.setVelocity(entity.getVelocity().add(diff.multiply(this.speed * 0.05 / diff.length())));
			}
		}
	}
}