package arcana.items;

import arcana.api.Focus;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class FocusItem extends Item implements Focus{
	
	public FocusItem(Settings settings){
		super(settings);
	}
	
	public Text nameForTooltip(ItemStack focusStack){
		return Text.translatable(getTranslationKey(focusStack)).formatted(Formatting.AQUA);
	}
	
	@Environment(EnvType.CLIENT)
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context){
		tooltip.add(WandItem.costText(castCost(stack, null, MinecraftClient.getInstance().player)));
	}
}