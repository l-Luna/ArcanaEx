package arcana.items;

import arcana.api.Focus;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public abstract class FocusItem extends Item implements Focus{
	
	public FocusItem(Settings settings){
		super(settings);
	}
	
	public Text nameForTooltip(ItemStack focusStack){
		return Text.translatable(getTranslationKey(focusStack)).formatted(Formatting.AQUA);
	}
	
	@Environment(EnvType.CLIENT)
	public void appendTooltip(ItemStack stack, TooltipContext ctx, List<Text> tooltip, TooltipType type){
		tooltip.add(WandItem.costText(castCost(stack, null, MinecraftClient.getInstance().player)));
	}
}