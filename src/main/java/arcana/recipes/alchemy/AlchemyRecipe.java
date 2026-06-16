package arcana.recipes.alchemy;

import arcana.api.AspectRecipe;
import arcana.api.RenamableRecipe;
import arcana.aspects.AspectMap;
import arcana.recipes.ArcanaRecipe;
import arcana.util.PacketCodecUtil;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

import static arcana.Arcana.arcId;

public class AlchemyRecipe implements Recipe<AlchemyInput>, ArcanaRecipe, AspectRecipe, RenamableRecipe{
	
	public static RecipeType<AlchemyRecipe> TYPE;
	public static Serializer SERIALIZER;
	
	public static void setup(){
		TYPE = Registry.register(
				Registries.RECIPE_TYPE,
				arcId("alchemy"),
				new RecipeType<>(){
					public String toString(){
						return "arcana:alchemy";
					}
				}
		);
		SERIALIZER = Registry.register(
				Registries.RECIPE_SERIALIZER,
				arcId("alchemy"),
				new Serializer()
		);
	}
	
	public AlchemyRecipe(@Nullable Identifier researchId, Optional<Integer> researchStage, String translationKey, Ingredient ingredient, AspectMap aspects, ItemStack result){
		this.researchId = researchId;
		this.researchStage = researchStage;
		this.translationKey = translationKey;
		this.ingredient = ingredient;
		this.aspects = aspects;
		this.result = result;
	}
	
	private final @Nullable Identifier researchId;
	private final Optional<Integer> researchStage;
	private final String translationKey;
	
	private final Ingredient ingredient;
	private final AspectMap aspects;
	
	private final ItemStack result;
	
	public boolean matches(AlchemyInput inventory, World world){
		if(ingredient.test(inventory.getReagent()) && inventory.getAspects().contains(aspects))
			return researchId == null || inventory.complete(researchId, researchStage.orElse(-1));
		return false;
	}
	
	public ItemStack craft(AlchemyInput inventory, RegistryWrapper.WrapperLookup lookup){
		return result.copy();
	}
	
	public boolean fits(int width, int height){
		return true;
	}
	
	public DefaultedList<Ingredient> getIngredients(){
		return DefaultedList.copyOf(ingredient, ingredient);
	}
	
	public AspectMap getConsumedAspects(@Nullable AlchemyInput inventory){
		return aspects;
	}
	
	public Ingredient getIngredient(){
		return ingredient;
	}
	
	public ItemStack getResult(RegistryWrapper.WrapperLookup lookup){
		return result;
	}
	
	public ItemStack getResult(){
		return result;
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
	
	public Optional<Integer> getResearchStage(){
		return researchStage;
	}
	
	public void affect(AspectMap aspects){
		aspects.add(this.aspects);
	}
	
	public Optional<String> getTranslationKey(){
		return Optional.ofNullable(translationKey);
	}
	
	public static class Serializer implements RecipeSerializer<AlchemyRecipe>{
		
		// TODO: restore "arcana:entry@stage" syntax?
		private static final MapCodec<AlchemyRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
				Identifier.CODEC.optionalFieldOf("research_id", null).forGetter(AlchemyRecipe::getResearchId),
				Codec.INT.optionalFieldOf("research_stage").forGetter(x -> x.researchStage),
				Codec.STRING.optionalFieldOf("name", null).forGetter(x -> x.translationKey),
				Ingredient.DISALLOW_EMPTY_CODEC.fieldOf("ingredient").forGetter(AlchemyRecipe::getIngredient),
				AspectMap.CODEC.fieldOf("aspects").forGetter(x->x.aspects),
				ItemStack.CODEC.fieldOf("result").forGetter(x->x.result)
		).apply(i, AlchemyRecipe::new));
		
		private static final PacketCodec<RegistryByteBuf, AlchemyRecipe> PACKET_CODEC = PacketCodec.tuple(
				PacketCodecUtil.nullable(Identifier.PACKET_CODEC), AlchemyRecipe::getResearchId,
				PacketCodecs.optional(PacketCodecs.VAR_INT), x -> x.researchStage,
				PacketCodecUtil.nullable(PacketCodecs.STRING), x -> x.translationKey,
				Ingredient.PACKET_CODEC, AlchemyRecipe::getIngredient,
				AspectMap.PACKET_CODEC, x -> x.aspects,
				ItemStack.PACKET_CODEC, x -> x.result,
		AlchemyRecipe::new);
		
		public MapCodec<AlchemyRecipe> codec(){
			return CODEC;
		}
		
		public PacketCodec<RegistryByteBuf, AlchemyRecipe> packetCodec(){
			return PACKET_CODEC;
		}
	}
}