package arcana.enchantments;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;

public class RunicShieldingEnchantment extends Enchantment{
	
	public RunicShieldingEnchantment(){
		super(Rarity.VERY_RARE, EnchantmentTarget.ARMOR, new EquipmentSlot[]{ EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET });
	}
	
	public boolean isAvailableForRandomSelection(){
		return false;
	}
	
	public boolean isAvailableForEnchantedBookOffer(){
		return false;
	}
	
	public boolean isTreasure(){
		return true;
	}
	
	// TODO: infusion enchantment probably gets its own mechanism for this
	public int getMaxLevel(){
		return 1;
	}
	
	public int getMinPower(int level){
		return level * 25;
	}
	
	public int getMaxPower(int level){
		return getMinPower(level) + 50;
	}
}