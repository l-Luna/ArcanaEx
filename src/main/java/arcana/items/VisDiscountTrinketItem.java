package arcana.items;

import arcana.api.VisDiscountingItem;
import arcana.aspects.Aspect;
import dev.emi.trinkets.api.TrinketItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class VisDiscountTrinketItem extends TrinketItem implements VisDiscountingItem{
	
	private final int percentOff;
	
	public VisDiscountTrinketItem(Settings settings, int percentOff){
		super(settings);
		this.percentOff = percentOff;
	}
	
	public int percentOff(ItemStack stack, @Nullable Aspect aspect, PlayerEntity player){
		return percentOff;
	}
}