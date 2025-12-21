package arcana.entities.wisps;

import arcana.ArcanaRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.world.World;

public class TaintedWispEntity extends WispEntity{
	
	public TaintedWispEntity(EntityType<? extends TaintedWispEntity> entityType, World world){
		super(entityType, world);
	}
	
	public TaintedWispEntity(World world){
		this(ArcanaRegistry.TAINTED_WISP, world);
	}
	
	public static DefaultAttributeContainer.Builder createDefaultAttributes(){
		return MobEntity.createMobAttributes()
				.add(EntityAttributes.GENERIC_MAX_HEALTH, 20)
				.add(EntityAttributes.GENERIC_FLYING_SPEED, 1)
				.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 1)
				.add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 4)
				.add(EntityAttributes.GENERIC_FOLLOW_RANGE, 60);
	}
	
	protected void initGoals(){
		super.initGoals();
		targetSelector.add(5, new ActiveTargetGoal<>(this, PureWispEntity.class, false, false));
	}
	
	public boolean shouldAngerAt(LivingEntity entity){
		return true;
	}
}