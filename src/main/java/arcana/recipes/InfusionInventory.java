package arcana.recipes;

import arcana.aspects.AspectMap;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;

import java.util.List;

public class InfusionInventory extends SimpleInventory{
	
	List<ItemStack> outerStacks;
	ItemStack centre;
	AspectMap aspects;
}