package arcana.api;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public interface ContextCraftedItem{
	
	void onCraft(ItemStack stack, Inventory context, World world, PlayerEntity player);
}