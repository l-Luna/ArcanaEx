package arcana.api;

import arcana.aspects.Aspect;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface VisDiscountingItem{
	
	int percentOff(ItemStack stack, @Nullable Aspect aspect, PlayerEntity player);
}
