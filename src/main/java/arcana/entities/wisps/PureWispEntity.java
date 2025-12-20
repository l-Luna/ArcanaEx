package arcana.entities.wisps;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.world.World;

public class PureWispEntity extends WispEntity{
	
	public PureWispEntity(EntityType<? extends WispEntity> entityType, World world){
		super(entityType, world);
	}
	
	public static DefaultAttributeContainer.Builder createDefaultAttributes(){
		return MobEntity.createMobAttributes()
				.add(EntityAttributes.GENERIC_MAX_HEALTH, 22)
				.add(EntityAttributes.GENERIC_FLYING_SPEED, 1.2)
				.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 1.2)
				.add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 3)
				.add(EntityAttributes.GENERIC_FOLLOW_RANGE, 60);
	}
	
	protected void initGoals(){
		super.initGoals();
		targetSelector.add(5, new ActiveTargetGoal<>(this, PureWispEntity.class, false, false));
	}
}