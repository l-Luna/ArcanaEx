package arcana.entities.crimson;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.world.World;

public class CrimsonProtectorEntity extends CrimsonEntity{
	
	public CrimsonProtectorEntity(EntityType<? extends HostileEntity> entityType, World world){
		super(entityType, world);
	}
	
	// attributes
	
	public static DefaultAttributeContainer.Builder createProtectorAttributes(){
		return CrimsonEntity.createCrimsonAttributes().add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.2);
	}
}