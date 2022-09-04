package arcana.items;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class FocusItem extends Item{
	
	public FocusItem(Settings settings){
		super(settings);
	}
	
	public Text nameForTooltip(ItemStack focusStack){
		return Text.translatable(getTranslationKey(focusStack)).formatted(Formatting.AQUA);
	}
}