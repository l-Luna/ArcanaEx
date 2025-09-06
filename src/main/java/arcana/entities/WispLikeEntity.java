package arcana.entities;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.EnumSet;

public abstract class WispLikeEntity extends PathAwareEntity{
	
	protected WispLikeEntity(EntityType<? extends WispLikeEntity> entityType, World world){
		super(entityType, world);
	}
	
	//
	
	abstract Vec3d anchor();
	
	//
	
	protected void initGoals(){
		goalSelector.add(10, new FloatAroundGoal(this));
	}
	
	public boolean hasNoGravity(){
		return true;
	}
	
	protected static class FloatAroundGoal extends Goal{
		private final WispLikeEntity entity;
		
		public FloatAroundGoal(WispLikeEntity entity){
			this.entity = entity;
			setControls(EnumSet.of(Goal.Control.MOVE));
		}
		
		public boolean canStart(){
			return entity.navigation.isIdle();
		}
		
		public boolean shouldContinue(){
			return entity.navigation.isFollowingPath();
		}
		
		public void start(){
			Vec3d point = NoPenaltyTargeting.findFrom(entity, 10, 10, entity.anchor());
			if(point != null)
				entity.navigation.startMovingAlong(entity.navigation.findPathTo(new BlockPos(point), 0), 2.1f);
		}
	}
	
	protected static class ChargeAttackGoal extends Goal{
		
		public boolean canStart(){
			return false;
		}
	}
}