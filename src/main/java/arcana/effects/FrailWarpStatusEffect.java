package arcana.effects;

import arcana.cca_components.RunicShielding;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.player.PlayerEntity;

public class FrailWarpStatusEffect extends ArcanaStatusEffect{
	
	public FrailWarpStatusEffect(){
		super(StatusEffectCategory.HARMFUL, 0xBC0826);
	}
	
	public boolean canApplyUpdateEffect(int duration, int amplifier){
		return true;
	}
	
	public boolean applyUpdateEffect(LivingEntity entity, int amplifier){
		if(entity instanceof PlayerEntity player
				&& !player.isDead()
				&& !player.getAbilities().invulnerable
				&& RunicShielding.from(player).getHalfPoints() < 2)
			player.damage(entity.getDamageSources().wither(), (player.getHealth() + player.getAbsorptionAmount()) * 2);
		return true;
	}
}