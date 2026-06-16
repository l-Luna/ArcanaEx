package arcana.recipes.infusion;

import arcana.aspects.AspectMap;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.input.RecipeInput;

import java.util.List;

public record InfusionInput(ItemStack centre, List<ItemStack> outerStacks, AspectMap aspects) implements RecipeInput{
	
	public ItemStack getStackInSlot(int slot){
		if(slot == 0)
			return centre;
		else
			return outerStacks.get(slot - 1);
	}
	
	public int getSize(){
		return 1 + outerStacks.size();
	}
}