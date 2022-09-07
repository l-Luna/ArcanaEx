package arcana.integration.emi;

import arcana.screens.ArcaneCraftingScreenHandler;
import dev.emi.emi.api.EmiRecipeHandler;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import net.minecraft.screen.slot.Slot;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class EmiArcaneCraftingRecipeHandler implements EmiRecipeHandler<ArcaneCraftingScreenHandler>{
	
	public List<Slot> getInputSources(ArcaneCraftingScreenHandler handler){
		List<Slot> slots = new ArrayList<>();
		
		for(int i = 1; i < 10; i++)
			slots.add(handler.getSlot(i));
		
		int invStart = 1 + 9 + 1;
		for(int i = invStart; i < invStart + 36; ++i)
			slots.add(handler.getSlot(i));
		
		return slots;
	}
	
	public List<Slot> getCraftingSlots(ArcaneCraftingScreenHandler handler){
		List<Slot> slots = new ArrayList<>();
		
		for(int i = 1; i < 10; i++)
			slots.add(handler.getSlot(i));
		
		return slots;
	}
	
	public @Nullable Slot getOutputSlot(ArcaneCraftingScreenHandler handler){
		return handler.getSlot(0);
	}
	
	public boolean supportsRecipe(EmiRecipe recipe){
		return (recipe.getCategory() == ArcanaEmiPlugin.ARCANE_CRAFTING || recipe.getCategory() == VanillaEmiRecipeCategories.CRAFTING)
				&& recipe.supportsRecipeTree();
	}
}