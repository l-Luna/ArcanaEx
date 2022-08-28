package arcana.recipes;

import arcana.aspects.AspectMap;
import net.minecraft.recipe.CraftingRecipe;

public interface ArcaneCraftingRecipe extends CraftingRecipe{
	
	AspectMap aspects();
}