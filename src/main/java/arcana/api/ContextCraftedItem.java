package arcana.api;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

public interface ContextCraftedItem{
	
	void onCraft(ItemStack stack, Inventory context);
}