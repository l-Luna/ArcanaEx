package arcana.api;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;

public interface DynamicMaxLevelEnchantment{
	
	int getMaxLevel(ItemStack stack);
	
	static int getMaxLevel(Enchantment enchantment, ItemStack stack){
		return enchantment instanceof DynamicMaxLevelEnchantment dmle ? dmle.getMaxLevel(stack) : enchantment.getMaxLevel();
	}
}