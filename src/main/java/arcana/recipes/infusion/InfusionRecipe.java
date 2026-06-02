package arcana.recipes.infusion;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.world.World;

public interface InfusionRecipe extends Recipe<InfusionInventory>{
	
	BakedInfusionRecipe craftInfusion(InfusionInventory inventory);
	
	default boolean fits(int width, int height){
		return true;
	}
	
	default ItemStack craft(InfusionInventory inventory){
		return craftInfusion(inventory).result();
	}
	
	default boolean matches(InfusionInventory inventory, World world){
		return craftInfusion(inventory) != null;
	}
	
	default boolean isIgnoredInRecipeBook(){
		return true;
	}
}