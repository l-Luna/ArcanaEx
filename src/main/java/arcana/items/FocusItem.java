package arcana.items;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;

public class FocusItem extends Item{
	
	public FocusItem(Settings settings){
		super(settings);
	}
	
	public Text nameForTooltip(ItemStack focusStack){
		return Text.translatable(getTranslationKey(focusStack)).formatted(Formatting.AQUA);
	}
	
	// TODO: split into Focus interface, like Cap/Core?
	public ActionResult castOnBlock(ItemUsageContext ctx){
		return ActionResult.PASS;
	}
	
	public ActionResult castOnEntity(ItemStack wand, ItemStack focus, PlayerEntity user, LivingEntity target, Hand hand){
		return ActionResult.PASS;
	}
}