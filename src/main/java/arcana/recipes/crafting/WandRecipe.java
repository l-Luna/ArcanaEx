package arcana.recipes.crafting;

import arcana.api.Cap;
import arcana.api.Core;
import arcana.items.WandItem;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;

public class WandRecipe extends SpecialCraftingRecipe{
	
	public static final RecipeSerializer<WandRecipe> SERIALIZER = new SpecialRecipeSerializer<>(WandRecipe::new);
	
	public WandRecipe(CraftingRecipeCategory group){
		super(group);
	}
	
	public boolean matches(CraftingRecipeInput inventory, World world){
		Cap caps = null;
		// cap in top left
		if(inventory.getStackInSlot(0).getItem() instanceof Cap c)
			caps = c;
		// same cap in top right
		if(inventory.getStackInSlot(8).getItem() != caps)
			return false;
		// core in middle
		if(Core.asCore(inventory.getStackInSlot(4).getItem()) == null)
			return false;
		// nothing else
		for(int i = 0; i < 9; i++)
			if(i != 0 && i != 4 && i != 8)
				if(!inventory.getStackInSlot(i).isEmpty())
					return false;
		return true;
	}
	
	public ItemStack craft(CraftingRecipeInput inventory, RegistryWrapper.WrapperLookup lookup){
		Cap cap = (Cap)inventory.getStackInSlot(0).getItem();
		Core core = Core.asCore(inventory.getStackInSlot(4).getItem());
		return WandItem.withCapAndCore(cap, core);
	}
	
	public boolean fits(int width, int height){
		return width >= 3 && height >= 3;
	}
	
	public RecipeSerializer<?> getSerializer(){
		return SERIALIZER;
	}
}