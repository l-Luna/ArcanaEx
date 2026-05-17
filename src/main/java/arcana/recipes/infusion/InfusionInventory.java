package arcana.recipes.infusion;

import arcana.aspects.AspectMap;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;

import java.util.List;

public class InfusionInventory extends SimpleInventory{
	
	public final List<ItemStack> outerStacks;
	public final ItemStack centre;
	public final AspectMap aspects;
	
	public InfusionInventory(ItemStack centre, List<ItemStack> outerStacks, AspectMap aspects){
		this.centre = centre;
		this.outerStacks = outerStacks;
		this.aspects = aspects;
	}
}