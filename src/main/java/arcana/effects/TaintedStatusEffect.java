package arcana.effects;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectCategory;

public class TaintedStatusEffect extends ArcanaStatusEffect{
	
	public TaintedStatusEffect(){
		super(StatusEffectCategory.HARMFUL, 0x8D4590);
	}
	
	public boolean canApplyUpdateEffect(int duration, int amplifier){
		int tickDelay = 30 >> amplifier;
		if(tickDelay > 0)
			return duration % tickDelay == 0;
		return true;
	}
	
	public void applyUpdateEffect(LivingEntity entity, int amplifier){
		// TODO: taint damage source
		entity.damage(DamageSource.LIGHTNING_BOLT, 1);
	}
}