package arcana.effects;

import arcana.ArcanaRegistry;
import arcana.ArcanaTags;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.entry.RegistryEntry;

public class PressureStatusEffect extends ArcanaStatusEffect{
	
	public PressureStatusEffect(){
		super(StatusEffectCategory.HARMFUL, 0x1B042E);
	}
	
	public static boolean suppresses(LivingEntity entity, RegistryEntry<StatusEffect> effect){
		return effect.hasKeyAndValue()
				&& effect.value().isBeneficial()
				&& entity != null
				&& entity.hasStatusEffect(ArcanaRegistry.PRESSURE.entry())
				&& !effect.isIn(ArcanaTags.BYPASSES_PRESSURE);
	}
}