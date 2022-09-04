package arcana.items;

import arcana.aspects.AspectMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FocusItem extends Item{
	
	public FocusItem(Settings settings){
		super(settings);
	}
	
	public Text nameForTooltip(ItemStack focusStack){
		return Text.translatable(getTranslationKey(focusStack)).formatted(Formatting.AQUA);
	}
	
	// TODO: split into Focus interface, like Cap/Core?
	public AspectMap castCost(@Nullable ItemStack wand, ItemStack focus, PlayerEntity user){
		return new AspectMap();
	}
	
	public ActionResult castOnBlock(ItemUsageContext ctx){
		return ActionResult.PASS;
	}
	
	public ActionResult castOnEntity(ItemStack wand, ItemStack focus, PlayerEntity user, LivingEntity target, Hand hand){
		return ActionResult.PASS;
	}
	
	@Environment(EnvType.CLIENT)
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context){
		tooltip.add(WandItem.costText(castCost(stack, null, MinecraftClient.getInstance().player)));
	}
}