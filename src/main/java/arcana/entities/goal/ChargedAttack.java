package arcana.entities.goal;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;

public interface ChargedAttack<E extends HostileEntity>{

	default boolean canUse(E entity){
		return true;
	}
	
	void begin(E entity);
	
	boolean hasFinishedCharging(E entity, int chargedTicks);
	
	default void finishCharging(E entity){}
	
	void shootAt(E entity, LivingEntity target, float time);
	
	default void cancel(E entity){
		if(entity.isUsingItem())
			entity.clearActiveItem();
	}
}