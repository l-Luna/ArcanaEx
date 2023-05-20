package arcana.items;

import arcana.aspects.Aspect;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public interface VisDiscountingItem{
	
	int percentOff(ItemStack stack, Aspect aspect, PlayerEntity player);
}
