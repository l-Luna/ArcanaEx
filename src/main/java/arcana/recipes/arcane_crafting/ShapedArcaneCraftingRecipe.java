package arcana.recipes.arcane_crafting;

import arcana.api.RenamableRecipe;
import arcana.aspects.AspectMap;
import arcana.aspects.ItemAspectRegistry;
import arcana.recipes.ArcanaRecipe;
import arcana.recipes.XIngredient;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.registry.Registry;

import java.util.Optional;

import static arcana.Arcana.arcId;

public class ShapedArcaneCraftingRecipe extends ShapedRecipe implements ArcaneCraftingRecipe, RenamableRecipe, ArcanaRecipe{
	
	public static RecipeType<ArcaneCraftingRecipe> TYPE;
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
	
	private AspectMap aspects;
	private String translationKey;
	
	public ShapedArcaneCraftingRecipe(Identifier id, String group, int width, int height, DefaultedList<Ingredient> input, ItemStack output){
		super(id, group, width, height, input, output);
	}
	
	public ShapedArcaneCraftingRecipe(ShapedRecipe from){
		this(from.getId(), from.getGroup(), from.getWidth(), from.getHeight(), from.getIngredients(), from.getOutput());
	}
	
	public RecipeType<?> getType(){
		return TYPE;
	}
	
	public RecipeSerializer<?> getSerializer(){
		return SERIALIZER;
	}
	
	public AspectMap aspects(){
		return aspects;
	}
	
	public Optional<String> getTranslationKey(){
		return Optional.ofNullable(translationKey);
	}
	
	public boolean isIgnoredInRecipeBook(){
		return true;
	}
	
	public static class Serializer extends ShapedRecipe.Serializer{
		
		public ShapedRecipe read(Identifier id, JsonObject json){
			ShapedRecipe orig = ShapedRecipe.Serializer.read(id, json);
			ItemStack output = orig.getOutput();
			if(json.has("apply"))
				output = XIngredient.matcherFromString(json.get("apply").getAsString()).preview(output);
			ShapedArcaneCraftingRecipe recipe = new ShapedArcaneCraftingRecipe(orig.getId(), orig.getGroup(), orig.getWidth(), orig.getHeight(), orig.getIngredients(), output);
			recipe.aspects = ItemAspectRegistry.parseAspectStackList(id, JsonHelper.getArray(json, "aspects")).orElse(null);
			recipe.translationKey = JsonHelper.getString(json, "name", null);
			return recipe;
		}
		
		public void write(PacketByteBuf bytes, ShapedRecipe recipe){
			ShapedRecipe.Serializer.write(bytes, recipe);
			bytes.writeNbt(((ShapedArcaneCraftingRecipe)recipe).aspects.toNbt());
			String key = ((ShapedArcaneCraftingRecipe)recipe).translationKey;
			bytes.writeBoolean(key != null);
			if(key != null)
				bytes.writeString(key);
		}
		
		public ShapedRecipe read(Identifier id, PacketByteBuf bytes){
			ShapedRecipe orig = ShapedRecipe.Serializer.read(id, bytes);
			ShapedArcaneCraftingRecipe recipe = new ShapedArcaneCraftingRecipe(orig);
			recipe.aspects = AspectMap.fromNbt(bytes.readNbt());
			if(bytes.readBoolean())
				recipe.translationKey = bytes.readString();
			return recipe;
		}
	}
}