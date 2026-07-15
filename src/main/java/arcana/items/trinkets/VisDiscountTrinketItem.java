package arcana.items.trinkets;

import arcana.api.VisDiscountingItem;
import arcana.aspects.Aspect;
import dev.emi.trinkets.api.TrinketItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class VisDiscountTrinketItem extends TrinketItem implements VisDiscountingItem{
	
	private final int percentOff;
	
	public VisDiscountTrinketItem(Settings settings, int percentOff){
		super(settings);
		this.percentOff = percentOff;
	}
	
	public int percentOff(ItemStack stack, @Nullable Aspect aspect, PlayerEntity player){
		return percentOff;
	}
	
	@Environment(EnvType.CLIENT)
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type){
		super.appendTooltip(stack, context, tooltip, type);
		tooltip.add(Text.translatable("tooltip.arcana.wand.discount.all", percentOff));
	}
}