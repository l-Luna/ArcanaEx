package arcana.effects;

import arcana.ArcanaTags;
import arcana.aspects.Aspect;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.ArrayList;
import java.util.Objects;

public class AspectPowerStatusEffect extends ArcanaStatusEffect{
	
	public AspectPowerStatusEffect(Aspect aspect){
		super(StatusEffectCategory.BENEFICIAL, aspect.colour());
	}
	
	public static void handleExclusivity(PlayerEntity player){
		// find the effect with the highest time remaining, and remove all others
		RegistryEntry<StatusEffect> best = null;
		int bestTime = -1;
		for(StatusEffectInstance effectInst : player.getStatusEffects()){
			RegistryEntry<StatusEffect> type = effectInst.getEffectType();
			if(type.isIn(ArcanaTags.ASPECT_CANDY_EFFECTS) && effectInst.getDuration() > bestTime){
				best = type;
				bestTime = effectInst.getDuration();
			}
		}
		
		for(RegistryEntry<StatusEffect> effect : new ArrayList<>(player.getActiveStatusEffects().keySet()))
			if(effect.isIn(ArcanaTags.ASPECT_CANDY_EFFECTS) && !Objects.equals(effect, best))
				player.removeStatusEffect(effect);
	}
}