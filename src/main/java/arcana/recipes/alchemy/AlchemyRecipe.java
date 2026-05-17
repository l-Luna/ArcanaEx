package arcana.recipes.alchemy;

import arcana.api.AspectRecipe;
import arcana.api.RenamableRecipe;
import arcana.aspects.AspectMap;
import arcana.aspects.ItemAspectRegistry;
import arcana.recipes.ArcanaRecipe;
import arcana.recipes.XIngredient;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.OptionalInt;

import static arcana.Arcana.arcId;
import static arcana.Arcana.maybeArcId;

public class AlchemyRecipe implements Recipe<AlchemyInventory>, ArcanaRecipe, AspectRecipe, RenamableRecipe{
	
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
	
	public AlchemyRecipe(Identifier id, @Nullable Identifier researchId, OptionalInt researchStage, String translationKey, XIngredient ingredient, AspectMap aspects, ItemStack output){
		this.id = id;
		this.researchId = researchId;
		this.researchStage = researchStage;
		this.translationKey = translationKey;
		this.ingredient = ingredient;
		this.aspects = aspects;
		this.output = output;
	}
	
	private final Identifier id;
	private final @Nullable Identifier researchId;
	private final OptionalInt researchStage;
	private final String translationKey;
	
	private final XIngredient ingredient;
	private final AspectMap aspects;
	
	private final ItemStack output;
	
	public boolean matches(AlchemyInventory inventory, World world){
		if(ingredient.test(inventory.getStack(0)) && inventory.getAspects().contains(aspects))
			return researchId == null || inventory.complete(researchId, researchStage.orElse(-1));
		return false;
	}
	
	public ItemStack craft(AlchemyInventory inventory){
		return output.copy();
	}
	
	public boolean fits(int width, int height){
		return true;
	}
	
	public DefaultedList<Ingredient> getIngredients(){
		Ingredient basic = ingredient.basic();
		return DefaultedList.copyOf(basic, basic);
	}
	
	public AspectMap getConsumedAspects(@Nullable AlchemyInventory inventory){
		return aspects;
	}
	
	public XIngredient getIngredient(){
		return ingredient;
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
	
	public boolean isIgnoredInRecipeBook(){
		return true;
	}
	
	public @Nullable Identifier getResearchId(){
		return researchId;
	}
	
	public OptionalInt getResearchStage(){
		return researchStage;
	}
	
	public void affect(AspectMap aspects){
		aspects.add(this.aspects);
	}
	
	public Optional<String> getTranslationKey(){
		return Optional.ofNullable(translationKey);
	}
	
	public static class Serializer implements RecipeSerializer<AlchemyRecipe>{
		
		public AlchemyRecipe read(Identifier id, JsonObject json){
			XIngredient ingredient = XIngredient.fromJson(JsonHelper.getObject(json, "ingredient"));
			var aspects = ItemAspectRegistry.parseAspectStackList(id, JsonHelper.getArray(json, "aspects")).orElseGet(AspectMap::new);
			ItemStack output = ShapedRecipe.outputFromJson(JsonHelper.getObject(json, "result"));
			Identifier researchId = null;
			var researchStage = OptionalInt.empty();
			if(json.has("research")){
				String researchString = json.get("research").getAsString();
				if(researchString.contains("@")){
					var split = researchString.split("@", 2);
					researchString = split[0];
					researchStage = OptionalInt.of(Integer.parseInt(split[1]));
				}
				researchId = maybeArcId(researchString);
			}
			String name = JsonHelper.getString(json, "name", null);
			return new AlchemyRecipe(id, researchId, researchStage, name, ingredient, aspects, output);
		}
		
		public void write(PacketByteBuf buf, AlchemyRecipe recipe){
			recipe.ingredient.write(buf);
			buf.writeNbt(recipe.aspects.toNbt());
			buf.writeItemStack(recipe.output);
			boolean hasReq = recipe.researchId != null;
			buf.writeBoolean(hasReq);
			if(hasReq){
				buf.writeIdentifier(recipe.researchId);
				boolean hasStage = recipe.researchStage.isPresent();
				buf.writeBoolean(hasStage);
				if(hasStage)
					buf.writeVarInt(recipe.researchStage.getAsInt());
			}
			buf.writeBoolean(recipe.translationKey != null);
			if(recipe.translationKey != null)
				buf.writeString(recipe.translationKey);
		}
		
		public AlchemyRecipe read(Identifier id, PacketByteBuf buf){
			XIngredient ingredient = XIngredient.read(buf);
			AspectMap aspects = AspectMap.fromNbt(buf.readNbt());
			ItemStack output = buf.readItemStack();
			Identifier researchId = null;
			OptionalInt researchStage = OptionalInt.empty();
			if(buf.readBoolean()){
				researchId = buf.readIdentifier();
				if(buf.readBoolean())
					researchStage = OptionalInt.of(buf.readVarInt());
			}
			String name = null;
			if(buf.readBoolean())
				name = buf.readString();
			return new AlchemyRecipe(id, researchId, researchStage, name, ingredient, aspects, output);
		}
	}
}