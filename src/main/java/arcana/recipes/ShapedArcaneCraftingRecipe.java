package arcana.recipes;

import arcana.aspects.AspectMap;
import arcana.aspects.ItemAspectRegistry;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.registry.Registry;

import static arcana.Arcana.arcId;

public class ShapedArcaneCraftingRecipe extends ShapedRecipe implements ArcaneCraftingRecipe{
	
	public static RecipeType<ShapedArcaneCraftingRecipe> TYPE;
	public static Serializer SERIALIZER;
	
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
		SERIALIZER = Registry.register(
				Registry.RECIPE_SERIALIZER,
				arcId("arcane_crafting"),
				new Serializer()
		);
	}
	
	AspectMap aspects;
	
	public ShapedArcaneCraftingRecipe(Identifier id, String group, int width, int height, DefaultedList<Ingredient> input, ItemStack output){
		super(id, group, width, height, input, output);
	}
	
	public ShapedArcaneCraftingRecipe(ShapedRecipe from){
		this(from.getId(), from.getGroup(), from.getWidth(), from.getHeight(), from.getIngredients(), from.getOutput());
	}
	
	public RecipeType<?> getType(){
		return TYPE;
	}
	
	public AspectMap aspects(){
		return aspects;
	}
	
	public static class Serializer extends ShapedRecipe.Serializer{
		
		public ShapedRecipe read(Identifier id, JsonObject json){
			ShapedRecipe orig = super.read(id, json);
			ShapedArcaneCraftingRecipe recipe = new ShapedArcaneCraftingRecipe(orig);
			recipe.aspects = AspectMap.fromAspectStackList(ItemAspectRegistry.parseAspectStackList(id, JsonHelper.getArray(json, "aspects")).orElse(null));
			return recipe;
		}
		
		public void write(PacketByteBuf bytes, ShapedRecipe recipe){
			super.write(bytes, recipe);
			bytes.writeNbt(((ShapedArcaneCraftingRecipe)recipe).aspects.toNbt());
		}
		
		public ShapedRecipe read(Identifier id, PacketByteBuf bytes){
			ShapedRecipe orig = super.read(id, bytes);
			ShapedArcaneCraftingRecipe recipe = new ShapedArcaneCraftingRecipe(orig);
			recipe.aspects = AspectMap.fromNbt(bytes.readNbt());
			return recipe;
		}
	}
}