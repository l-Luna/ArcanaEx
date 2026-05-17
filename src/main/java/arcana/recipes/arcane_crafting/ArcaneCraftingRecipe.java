package arcana.recipes.arcane_crafting;

import arcana.aspects.AspectMap;
import net.minecraft.recipe.CraftingRecipe;

public interface ArcaneCraftingRecipe extends CraftingRecipe{
	
	AspectMap aspects();
}