package arcana.enchantments;

import arcana.ArcanaTags;
import arcana.api.DynamicMaxLevelEnchantment;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;

public class ProjectingEnchantment extends Enchantment implements DynamicMaxLevelEnchantment{
	
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
		return stack.isIn(ArcanaTags.PROJECTING_LEVEL_1);
	}
	
	public int getMaxLevel(){
		return 3;
	}
	
	public int getMaxLevel(ItemStack stack){
		return maxLevelFor(stack);
	}
	
	public int getMinPower(int level){
		return level * 25;
	}
	
	public int getMaxPower(int level){
		return getMinPower(level) + 50;
	}
	
	public static int maxLevelFor(ItemStack stack){
		return stack.isIn(ArcanaTags.PROJECTING_LEVEL_3) ? 3 :
				stack.isIn(ArcanaTags.PROJECTING_LEVEL_2) ? 2 :
				stack.isIn(ArcanaTags.PROJECTING_LEVEL_1) ? 1 :
				0;
	}
}