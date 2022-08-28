package arcana.recipes;

import arcana.aspects.AspectMap;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.registry.Registry;

import static arcana.Arcana.arcId;

public class ShapedArcaneCraftingRecipe extends ShapedRecipe implements ArcaneCraftingRecipe{
	
	public static RecipeType<ShapedArcaneCraftingRecipe> TYPE;
	
	public static void setup(){
		TYPE = Registry.register(
				Registry.RECIPE_TYPE,
				arcId("arcane_crafting"),
				new RecipeType<>(){
					public String toString(){
						return "arcana:arcane_crafting";
					}
				}
		);
	}
	
	AspectMap aspects;
	
	public ShapedArcaneCraftingRecipe(Identifier id, String group, int width, int height, DefaultedList<Ingredient> input, ItemStack output){
		super(id, group, width, height, input, output);
	}
	
	public RecipeType<?> getType(){
		return TYPE;
	}
	
	public AspectMap aspects(){
		return aspects;
	}
}