package arcana.effects;

import arcana.ArcanaRegistry;
import arcana.ArcanaTags;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class PressureStatusEffect extends ArcanaStatusEffect{
	
	public PressureStatusEffect(){
		super(StatusEffectCategory.HARMFUL, 0x1B042E);
	}
	
	public static boolean suppresses(LivingEntity entity, StatusEffect effect){
		return effect.isBeneficial()
				&& entity != null
				&& entity.hasStatusEffect(ArcanaRegistry.PRESSURE.entry())
				&& !ArcanaTags.isOf(effect, ArcanaTags.BYPASSES_PRESSURE);
	}
}