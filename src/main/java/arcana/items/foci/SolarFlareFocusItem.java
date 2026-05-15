package arcana.items.foci;

import arcana.aspects.AspectMap;
import arcana.items.FocusItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class SolarFlareFocusItem extends FocusItem{
	
	public SolarFlareFocusItem(Settings settings){
		super(settings);
	}
	
	public AspectMap deciCastCost(@Nullable ItemStack wand, ItemStack focus, PlayerEntity user){
		return new AspectMap();
	}
}