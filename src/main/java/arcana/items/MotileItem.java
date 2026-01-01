package arcana.items;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class MotileItem extends Item{
	
	public MotileItem(Settings settings){
		super(settings);
	}
	
	// TODO: fancier visual
	public boolean hasGlint(ItemStack stack){
		return true;
	}
}