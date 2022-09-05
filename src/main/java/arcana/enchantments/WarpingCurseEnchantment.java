package arcana.enchantments;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;

public class WarpingCurseEnchantment extends Enchantment{
	
	public WarpingCurseEnchantment(Rarity weight, EquipmentSlot[] slotTypes){
		super(weight, EnchantmentTarget.VANISHABLE, slotTypes);
	}
	
	public int getMinPower(int level){
		return 25;
	}
	
	public int getMaxPower(int level){
		return 50;
	}
	
	public int getMaxLevel(){
		return 3;
	}
	
	public boolean isTreasure(){
		return true;
	}
	
	public boolean isCursed(){
		return true;
	}
}