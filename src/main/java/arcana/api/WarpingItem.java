package arcana.api;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public interface WarpingItem{
	
	// bonus warping, based on NBT; displaying the tooltip is your responsibility
	int warping(ItemStack stack, PlayerEntity player);
	
	@Environment(EnvType.CLIENT)
	static Text warpingTooltip(int amount){
		return Text.translatable("tooltip.arcana.warping", Text.translatable("enchantment.level." + amount)).formatted(Formatting.DARK_RED);
	}
}