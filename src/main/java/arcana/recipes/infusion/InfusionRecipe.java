package arcana.recipes.infusion;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;

public interface InfusionRecipe extends Recipe<InfusionInventory>{
	
	BakedInfusionRecipe craftInfusion(InfusionInventory inventory);
	
	default ItemStack craft(InfusionInventory inventory){
		return craftInfusion(inventory).result();
	}
}