package arcana.entities.crimson;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.world.World;

public class CrimsonHeavyKnightEntity extends CrimsonEntity{
	
	public CrimsonHeavyKnightEntity(EntityType<? extends HostileEntity> entityType, World world){
		super(entityType, world);
	}
	
	// attributes
	
	public static DefaultAttributeContainer.Builder createHeavyKnightAttributes(){
		return CrimsonEntity.createCrimsonAttributes().add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.1);
	}
}