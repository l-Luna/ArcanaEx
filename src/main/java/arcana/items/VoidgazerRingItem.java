package arcana.items;

import arcana.ArcanaRegistry;
import arcana.api.WarpingItem;
import arcana.aspects.Aspect;
import arcana.components.Researcher;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class VoidgazerRingItem extends RingItem implements WarpingItem{
	
	public VoidgazerRingItem(Settings settings){
		super(settings, 0, 0);
	}
	
	public int warping(ItemStack stack, PlayerEntity player){
		return 2;
	}
	
	// up to 14%
	public int percentOff(ItemStack stack, @Nullable Aspect aspect, PlayerEntity player){
		int warp = Researcher.from(player).getEffectiveWarp();
		return (int)(Math.round(14 * (1 - Math.pow(2, -warp / 13f))));
	}
	
	@Environment(EnvType.CLIENT)
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context){
		int percentOff = percentOff(stack, null, MinecraftClient.getInstance().player);
		tooltip.add(Text.translatable("tooltip.arcana.wand.discount.all", percentOff).formatted(Formatting.DARK_PURPLE));
		tooltip.add(ArcanaRegistry.WARPING.getName(2));
	}
}