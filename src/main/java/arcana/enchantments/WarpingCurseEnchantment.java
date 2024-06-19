package arcana.enchantments;

import arcana.items.RingItem;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;

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
	
	public boolean isAcceptableItem(ItemStack stack){
		return super.isAcceptableItem(stack) || stack.getItem() instanceof RingItem;
	}
}