package arcana.effects;

import arcana.ArcanaTags;
import arcana.aspects.Aspect;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;

import java.util.ArrayList;

public class AspectPowerStatusEffect extends ArcanaStatusEffect{
	
	public AspectPowerStatusEffect(Aspect aspect){
		super(StatusEffectCategory.BENEFICIAL, aspect.colour());
	}
	
	public static void handleExclusivity(PlayerEntity player){
		// find the effect with the highest time remaining, and remove all others
		StatusEffect best = null;
		int bestTime = -1;
		for(StatusEffectInstance effectInst : player.getStatusEffects()){
			StatusEffect type = effectInst.getEffectType();
			if(ArcanaTags.isOf(type, ArcanaTags.ASPECT_CANDY_EFFECTS) && effectInst.getDuration() > bestTime){
				best = type;
				bestTime = effectInst.getDuration();
			}
		}
		
		for(StatusEffect effect : new ArrayList<>(player.getActiveStatusEffects().keySet()))
			if(ArcanaTags.isOf(effect, ArcanaTags.ASPECT_CANDY_EFFECTS) && effect != best)
				player.removeStatusEffect(effect);
	}
}