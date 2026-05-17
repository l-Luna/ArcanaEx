package arcana.recipes.infusion;

import arcana.aspects.AspectMap;
import net.minecraft.item.ItemStack;

import java.util.List;

/**
 * Final costs of an infusion recipe, after dynamic recipes are resolved.
 */
public record BakedInfusionRecipe(ItemStack result, List<ItemStack> outerStacks, AspectMap aspects, int instability){

}