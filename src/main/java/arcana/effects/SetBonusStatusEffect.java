package arcana.effects;

import arcana.items.ArcanaArmorItem;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.Optional;

public class SetBonusStatusEffect extends ArcanaStatusEffect{
	
	public SetBonusStatusEffect(){
		super(StatusEffectCategory.BENEFICIAL, 0);
	}
	
	public static void handleArmourSetBonus(PlayerEntity player){
		Identifier setBonus = null;
		int matched = 0;
		for(ItemStack item : player.getArmorItems())
			if(item.getItem() instanceof ArcanaArmorItem armor){
				Optional<Identifier> armorBonus = armor.material.setBonus();
				if(armorBonus.isPresent() && (setBonus == null || setBonus.equals(armorBonus.get()))){
					setBonus = armorBonus.get();
					matched++;
				}
			}
		if(matched >= 4){
			Registries.STATUS_EFFECT.getEntry(setBonus).ifPresent(effect -> player.addStatusEffect(new StatusEffectInstance(
					effect,
					10 /* ticks */,
					0 /* level */,
					true /* ambient */,
					false /* no particles */,
					true /* yes icon */
			)));
		}
	}
}