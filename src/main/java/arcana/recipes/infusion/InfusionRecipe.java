package arcana.recipes.infusion;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;

public interface InfusionRecipe extends Recipe<InfusionInput>{
	
	BakedInfusionRecipe craftInfusion(InfusionInput inventory);
	
	default boolean fits(int width, int height){
		return true;
	}
	
	default ItemStack craft(InfusionInput inventory, RegistryWrapper.WrapperLookup lookup){
		return craftInfusion(inventory).result();
	}
	
	default boolean matches(InfusionInput inventory, World world){
		return craftInfusion(inventory) != null;
	}
	
	default boolean isIgnoredInRecipeBook(){
		return true;
	}
}