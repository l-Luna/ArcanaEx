package arcana.items.trinkets;

import arcana.api.WarpingItem;
import dev.emi.trinkets.api.TrinketItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;

import java.util.List;

public class WarpingTrinketItem extends TrinketItem implements WarpingItem{
	
	public WarpingTrinketItem(Settings settings){
		super(settings);
	}
	
	public int warping(ItemStack stack, PlayerEntity player){
		return 2;
	}
	
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type){
		super.appendTooltip(stack, context, tooltip, type);
		tooltip.add(WarpingItem.warpingTooltip(2));
	}
}