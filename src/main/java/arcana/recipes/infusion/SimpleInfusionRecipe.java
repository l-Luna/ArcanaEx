package arcana.recipes.infusion;

import arcana.api.RenamableRecipe;
import arcana.aspects.AspectMap;
import arcana.aspects.ItemAspectRegistry;
import arcana.recipes.ArcanaRecipe;
import arcana.recipes.XIngredient;
import com.google.gson.JsonElement;
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
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static arcana.Arcana.arcId;

public class SimpleInfusionRecipe implements InfusionRecipe, ArcanaRecipe, RenamableRecipe{
	
	public static RecipeType<InfusionRecipe> TYPE;
	public static Serializer SERIALIZER;
	
	private final Identifier id;
	private final ItemStack result;
	private final List<XIngredient> outerIngredients;
	private final Ingredient centralIngredient;
	private final AspectMap aspects;
	private final int instability;
	private final @Nullable String name;
	
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
	
	public SimpleInfusionRecipe(Identifier id, ItemStack result, List<XIngredient> outerIngredients, Ingredient centralIngredient, AspectMap aspects, int instability, @Nullable String name){
		this.id = id;
		this.result = result;
		this.outerIngredients = outerIngredients;
		this.centralIngredient = centralIngredient;
		this.aspects = aspects;
		this.instability = instability;
		this.name = name;
	}
	
	public boolean matches(InfusionInventory inventory, World world){
		// check centre
		if(!centralIngredient.test(inventory.centre))
			return false;
		// check others
		List<ItemStack> copy = new ArrayList<>(inventory.outerStacks);
		ingredient:
		for(XIngredient ingredient : outerIngredients){
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
		return inventory.aspects.contains(aspects);
	}
	
	public BakedInfusionRecipe craftInfusion(InfusionInventory inventory){
		// resolve ingredients to specific items
		List<ItemStack> available = new ArrayList<>(inventory.outerStacks);
		List<ItemStack> used = new ArrayList<>(outerIngredients.size());
		ingredient:
		for(XIngredient ingredient : outerIngredients){
			// safe to remove in this loop, since we break immediately
			for(int i = 0; i < available.size(); i++){
				ItemStack stack = available.get(i);
				if(ingredient.test(stack)){
					available.remove(stack);
					used.add(stack);
					continue ingredient;
				}
			}
		}
		return new BakedInfusionRecipe(result.copy(), used, aspects.copy(), instability);
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
		ret.addAll(outerIngredients.stream().map(XIngredient::basic).toList());
		return ret;
	}
	
	public AspectMap aspects(){
		return aspects;
	}
	
	public Ingredient centralIngredient(){
		return centralIngredient;
	}
	
	public List<XIngredient> outerIngredients(){
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
	
	public Optional<String> getTranslationKey(){
		return Optional.ofNullable(name);
	}
	
	public boolean isIgnoredInRecipeBook(){
		return true;
	}
	
	public static class Serializer implements RecipeSerializer<SimpleInfusionRecipe>{
		
		public SimpleInfusionRecipe read(Identifier id, JsonObject json){
			ItemStack result = ShapedRecipe.outputFromJson(JsonHelper.getObject(json, "result"));
			Ingredient central = Ingredient.fromJson(JsonHelper.getObject(json, "central"));
			List<XIngredient> outers = new ArrayList<>();
			for(JsonElement ingredients : JsonHelper.getArray(json, "ingredients"))
				outers.add(XIngredient.fromJson(ingredients));
			AspectMap aspects = ItemAspectRegistry.parseAspectStackList(id, JsonHelper.getArray(json, "aspects")).orElseGet(AspectMap::new);
			int instability = JsonHelper.getInt(json, "instability", 1);
			String name = JsonHelper.getString(json, "name", null);
			return new SimpleInfusionRecipe(id, result, outers, central, aspects, instability, name);
		}
		
		public void write(PacketByteBuf buf, SimpleInfusionRecipe recipe){
			buf.writeBoolean(recipe.name != null);
			if(recipe.name != null)
				buf.writeString(recipe.name);
			buf.writeItemStack(recipe.result);
			buf.writeVarInt(recipe.outerIngredients.size());
			for(XIngredient ingredient : recipe.outerIngredients)
				ingredient.write(buf);
			recipe.centralIngredient.write(buf);
			buf.writeNbt(recipe.aspects.toNbt());
			buf.writeVarInt(recipe.instability);
		}
		
		public SimpleInfusionRecipe read(Identifier id, PacketByteBuf buf){
			String name = null;
			if(buf.readBoolean())
				name = buf.readString();
			var result = buf.readItemStack();
			int size = buf.readVarInt();
			List<XIngredient> outer = new ArrayList<>(size);
			for(int i = 0; i < size; i++)
				outer.add(XIngredient.read(buf));
			return new SimpleInfusionRecipe(id, result, outer, Ingredient.fromPacket(buf), AspectMap.fromNbt(buf.readNbt()), buf.readVarInt(), name);
		}
	}
}