package arcana.entities;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class CoagulationEntity extends WispLikeEntity{
	
	public CoagulationEntity(EntityType<? extends WispLikeEntity> entityType, World world){
		super(entityType, world);
	}
	
	public static DefaultAttributeContainer.Builder createDefaultAttributes(){
		return MobEntity.createMobAttributes()
				.add(EntityAttributes.GENERIC_MAX_HEALTH, 20)
				.add(EntityAttributes.GENERIC_FLYING_SPEED, 1)
				.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 1)
				.add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 4)
				.add(EntityAttributes.GENERIC_FOLLOW_RANGE, 60);
	}
	
	Vec3d anchor(){
		return getPos();
	}
}