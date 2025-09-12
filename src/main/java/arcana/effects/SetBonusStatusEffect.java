package arcana.effects;

import arcana.items.ArcanaArmourMaterials;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ItemStack;

public class SetBonusStatusEffect extends ArcanaStatusEffect{
	
	public SetBonusStatusEffect(){
		super(StatusEffectCategory.BENEFICIAL, 0);
	}
	
	public static void handleArmourSetBonus(PlayerEntity player){
		ArmorMaterial setBonusMaterial = null;
		int matched = 0;
		for(ItemStack item : player.getArmorItems())
			if(item.getItem() instanceof ArmorItem armor)
				if(setBonusMaterial == null || setBonusMaterial == armor.getMaterial()){
					setBonusMaterial = armor.getMaterial();
					matched++;
				}
		if(matched >= 4 && setBonusMaterial instanceof ArcanaArmourMaterials aam){
			StatusEffect effect = aam.getSetBonusEffect();
			if(effect != null)
				player.addStatusEffect(new StatusEffectInstance(
						effect,
						10 /* ticks */,
						0 /* level */,
						true /* ambient */,
						false /* no particles */,
						true /* yes icon */
				));
		}
	}
}