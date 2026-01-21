package arcana.entities.crimson;

import arcana.ArcanaRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.World;

public class CrimsonKnightEntity extends CrimsonEntity{
	
	public CrimsonKnightEntity(EntityType<? extends HostileEntity> entityType, World world){
		super(entityType, world);
	}
	
	// setup
	
	protected void initEquipment(Random random, LocalDifficulty localDifficulty){
		super.initEquipment(random, localDifficulty);
		equipStack(EquipmentSlot.MAINHAND, ArcanaRegistry.CRIMSON_BLADE.getDefaultStack());
	}
	
	protected void initGoals(){
		super.initGoals();
		goalSelector.add(4, new MeleeAttackGoal(this, 1.4, false));
	}
	
	// attributes
	
	public static DefaultAttributeContainer.Builder createKnightAttributes(){
		return CrimsonEntity.createCrimsonAttributes().add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.2);
	}
}