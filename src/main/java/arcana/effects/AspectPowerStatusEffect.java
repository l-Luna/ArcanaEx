package arcana.effects;

import arcana.ArcanaRegistry;
import arcana.aspects.Aspect;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;

public class AspectPowerStatusEffect extends ArcanaStatusEffect{
	
	private final Aspect aspect;
	
	public AspectPowerStatusEffect(Aspect aspect){
		super(StatusEffectCategory.BENEFICIAL, aspect.colour());
		this.aspect = aspect;
	}
	
	public static void handleExclusivity(PlayerEntity player){
		// find the effect with the highest time remaining, and remove all others
		StatusEffect best = null;
		int bestTime = -1;
		for(StatusEffect effect : ArcanaRegistry.ASPECT_EFFECTS){
			StatusEffectInstance effectInst = player.getStatusEffect(effect);
			if(effectInst != null && effectInst.getDuration() > bestTime){
				best = effect;
				bestTime = effectInst.getDuration();
			}
		}
		
		for(StatusEffect effect : ArcanaRegistry.ASPECT_EFFECTS)
			if(effect != best)
				player.removeStatusEffect(effect);
	}
}