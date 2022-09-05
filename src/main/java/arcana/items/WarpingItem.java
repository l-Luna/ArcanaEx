package arcana.items;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public interface WarpingItem{
	
	// bonus warping, based on NBT; displaying the tooltip is your responsibility
	int warping(ItemStack stack, PlayerEntity player);
}