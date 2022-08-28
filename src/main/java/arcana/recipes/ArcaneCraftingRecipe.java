package arcana.recipes;

import arcana.screens.ArcaneCraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class ArcaneCraftingRecipe implements Recipe<ArcaneCraftingInventory>{
	
	public boolean matches(ArcaneCraftingInventory inventory, World world){
		return false;
	}
	
	public ItemStack craft(ArcaneCraftingInventory inventory){
		return null;
	}
	
	public boolean fits(int width, int height){
		return false;
	}
	
	public ItemStack getOutput(){
		return null;
	}
	
	public Identifier getId(){
		return null;
	}
	
	public RecipeSerializer<?> getSerializer(){
		return null;
	}
	
	public RecipeType<?> getType(){
		return null;
	}
}
