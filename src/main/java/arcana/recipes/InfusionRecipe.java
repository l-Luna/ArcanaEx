package arcana.recipes;

import arcana.aspects.AspectMap;
import arcana.aspects.ItemAspectRegistry;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

import static arcana.Arcana.arcId;

public class InfusionRecipe implements Recipe<InfusionInventory>{
	
	public static RecipeType<InfusionRecipe> TYPE;
	public static Serializer SERIALIZER;
	
	Identifier id;
	ItemStack result;
	List<Ingredient> outerIngredients;
	Ingredient centralIngredient;
	AspectMap aspects;
	int instability;
	// node aspects...
	
	public static void setup(){
		TYPE = Registry.register(
				Registry.RECIPE_TYPE,
				arcId("infusion"),
				new RecipeType<>(){
					public String toString(){
						return "arcana:infusion";
					}
				}
		);
		SERIALIZER = Registry.register(
				Registry.RECIPE_SERIALIZER,
				arcId("infusion"),
				new Serializer()
		);
	}
	
	public InfusionRecipe(Identifier id, ItemStack result, List<Ingredient> outerIngredients, Ingredient centralIngredient, AspectMap aspects, int instability){
		this.id = id;
		this.result = result;
		this.outerIngredients = outerIngredients;
		this.centralIngredient = centralIngredient;
		this.aspects = aspects;
		this.instability = instability;
	}
	
	public boolean matches(InfusionInventory inventory, World world){
		// check centre
		if(!centralIngredient.test(inventory.centre))
			return false;
		// check others
		List<ItemStack> copy = new ArrayList<>(inventory.outerStacks);
		ingredient:
		for(Ingredient ingredient : outerIngredients){
			// safe to remove in this loop, since we break immediately
			for(int i = 0; i < copy.size(); i++){
				ItemStack stack = copy.get(i);
				if(ingredient.test(stack)){
					copy.remove(stack);
					continue ingredient;
				}
			}
			return false;
		}
		// check aspects
		/*if(!inventory.aspects.contains(aspects))
			return false;*/
		return true;
	}
	
	public ItemStack craft(InfusionInventory inventory){
		return result;
	}
	
	public boolean fits(int width, int height){
		return true;
	}
	
	public ItemStack getOutput(){
		return result;
	}
	
	public Identifier getId(){
		return id;
	}
	
	public DefaultedList<Ingredient> getIngredients(){
		DefaultedList<Ingredient> ret = DefaultedList.ofSize(outerIngredients.size() + 1);
		ret.add(centralIngredient);
		ret.addAll(outerIngredients);
		return ret;
	}
	
	public AspectMap aspects(){
		return aspects;
	}
	
	public Ingredient centralIngredient(){
		return centralIngredient;
	}
	
	public List<Ingredient> outerIngredients(){
		return outerIngredients;
	}
	
	public int instability(){
		return instability;
	}
	
	public RecipeSerializer<?> getSerializer(){
		return SERIALIZER;
	}
	
	public RecipeType<?> getType(){
		return TYPE;
	}
	
	public static class Serializer implements RecipeSerializer<InfusionRecipe>{
		
		public InfusionRecipe read(Identifier id, JsonObject json){
			ItemStack result = ShapedRecipe.outputFromJson(JsonHelper.getObject(json, "result"));
			Ingredient central = Ingredient.fromJson(JsonHelper.getObject(json, "central"));
			List<Ingredient> outers = new ArrayList<>();
			for(JsonElement ingredients : JsonHelper.getArray(json, "ingredients"))
				outers.add(Ingredient.fromJson(ingredients));
			var aspects = ItemAspectRegistry.parseAspectStackList(id, JsonHelper.getArray(json, "aspects")).orElseGet(AspectMap::new);
			int instability = JsonHelper.getInt(json, "instability", 1);
			return new InfusionRecipe(id, result, outers, central, aspects, instability);
		}
		
		public void write(PacketByteBuf buf, InfusionRecipe recipe){
			buf.writeItemStack(recipe.result);
			buf.writeVarInt(recipe.outerIngredients.size());
			for(Ingredient ingredient : recipe.outerIngredients)
				ingredient.write(buf);
			recipe.centralIngredient.write(buf);
			buf.writeNbt(recipe.aspects.toNbt());
			buf.writeVarInt(recipe.instability);
		}
		
		public InfusionRecipe read(Identifier id, PacketByteBuf buf){
			var result = buf.readItemStack();
			int size = buf.readVarInt();
			List<Ingredient> outer = new ArrayList<>(size);
			for(int i = 0; i < size; i++)
				outer.add(Ingredient.fromPacket(buf));
			return new InfusionRecipe(id, result, outer, Ingredient.fromPacket(buf), AspectMap.fromNbt(buf.readNbt()), buf.readVarInt());
		}
	}
}