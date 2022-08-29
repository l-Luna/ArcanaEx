package arcana.recipes;

import arcana.aspects.AspectMap;
import arcana.aspects.ItemAspectRegistry;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import static arcana.Arcana.arcId;

public class AlchemyRecipe implements Recipe<AlchemyInventory>{
	
	public static RecipeType<AlchemyRecipe> TYPE;
	public static Serializer SERIALIZER;
	
	public static void setup(){
		TYPE = Registry.register(
				Registry.RECIPE_TYPE,
				arcId("alchemy"),
				new RecipeType<>(){
					public String toString(){
						return "arcana:alchemy";
					}
				}
		);
		SERIALIZER = Registry.register(
				Registry.RECIPE_SERIALIZER,
				arcId("alchemy"),
				new Serializer()
		);
	}
	
	public AlchemyRecipe(Identifier id, Ingredient ingredient, AspectMap aspects, ItemStack output){
		this.id = id;
		this.ingredient = ingredient;
		this.aspects = aspects;
		this.output = output;
	}
	
	private final Identifier id;
	// TODO: research requirement
	
	private final Ingredient ingredient;
	private final AspectMap aspects;
	
	private final ItemStack output;
	
	public boolean matches(AlchemyInventory inventory, World world){
		return ingredient.test(inventory.getStack(0)) && inventory.getAspects().contains(aspects);
	}
	
	public ItemStack craft(AlchemyInventory inventory){
		return output.copy();
	}
	
	public boolean fits(int width, int height){
		return true;
	}
	
	public DefaultedList<Ingredient> getIngredients(){
		return DefaultedList.copyOf(ingredient, ingredient);
	}
	
	public ItemStack getOutput(){
		return output;
	}
	
	public Identifier getId(){
		return id;
	}
	
	public RecipeSerializer<?> getSerializer(){
		return SERIALIZER;
	}
	
	public RecipeType<?> getType(){
		return TYPE;
	}
	
	public AspectMap getAspects(){
		return aspects;
	}
	
	public static class Serializer implements RecipeSerializer<AlchemyRecipe>{
		
		public AlchemyRecipe read(Identifier id, JsonObject json){
			Ingredient ingredient = Ingredient.fromJson(
					JsonHelper.hasArray(json, "ingredient")
					? JsonHelper.getArray(json, "ingredient")
					: JsonHelper.getObject(json, "ingredient"));
			var aspects = AspectMap.fromAspectStackList(ItemAspectRegistry.parseAspectStackList(id, JsonHelper.getArray(json, "aspects")).orElse(null));
			ItemStack output = ShapedRecipe.outputFromJson(JsonHelper.getObject(json, "result"));
			return new AlchemyRecipe(id, ingredient, aspects, output);
		}
		
		public void write(PacketByteBuf buf, AlchemyRecipe recipe){
			recipe.ingredient.write(buf);
			buf.writeNbt(recipe.aspects.toNbt());
			buf.writeItemStack(recipe.output);
		}
		
		public AlchemyRecipe read(Identifier id, PacketByteBuf buf){
			return new AlchemyRecipe(id, Ingredient.fromPacket(buf), AspectMap.fromNbt(buf.readNbt()), buf.readItemStack());
		}
	}
}