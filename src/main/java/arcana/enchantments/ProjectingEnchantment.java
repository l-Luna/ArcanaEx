package arcana.enchantments;

import arcana.items.RingItem;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;

public class ProjectingEnchantment extends Enchantment{
	
	public ProjectingEnchantment(){
		super(Rarity.VERY_RARE, EnchantmentTarget.ARMOR_HEAD, new EquipmentSlot[]{});
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
	
	public boolean isAcceptableItem(ItemStack stack){
		return stack.getItem() instanceof RingItem;
	}
	
	public int getMaxLevel(){
		return 3;
	}
	
	public int getMinPower(int level){
		return level * 25;
	}
	
	public int getMaxPower(int level){
		return getMinPower(level) + 50;
	}
}