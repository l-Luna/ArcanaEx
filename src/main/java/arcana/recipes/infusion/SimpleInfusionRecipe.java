package arcana.recipes.infusion;

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
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static arcana.Arcana.arcId;

public class SimpleInfusionRecipe implements InfusionRecipe, ArcanaRecipe, RenamableRecipe{
	
	public static RecipeType<InfusionRecipe> TYPE;
	public static Serializer SERIALIZER;
	
	private final ItemStack result;
	private final List<Ingredient> outerIngredients;
	private final Ingredient centralIngredient;
	private final AspectMap aspects;
	private final int instability;
	private final @Nullable String name;
	
	public static void setup(){
		TYPE = Registry.register(
				Registries.RECIPE_TYPE,
				arcId("infusion"),
				new RecipeType<>(){
					public String toString(){
						return "arcana:infusion";
					}
				}
		);
		SERIALIZER = Registry.register(
				Registries.RECIPE_SERIALIZER,
				arcId("infusion"),
				new Serializer()
		);
	}
	
	public SimpleInfusionRecipe(ItemStack result, List<Ingredient> outerIngredients, Ingredient centralIngredient, AspectMap aspects, int instability, @Nullable String name){
		this.result = result;
		this.outerIngredients = outerIngredients;
		this.centralIngredient = centralIngredient;
		this.aspects = aspects;
		this.instability = instability;
		this.name = name;
	}
	
	public BakedInfusionRecipe craftInfusion(InfusionInput inventory){
		// check main ingredients
		if(!(centralIngredient.test(inventory.centre()) && inventory.aspects().contains(aspects)))
			return null;
		// resolve outer ingredients to specific items
		List<ItemStack> used = matchIngredients(inventory, outerIngredients);
		if(used == null)
			return null;
		return new BakedInfusionRecipe(result.copy(), used, aspects.copy(), instability);
	}
	
	@Nullable
	protected static List<ItemStack> matchIngredients(InfusionInput inventory, List<Ingredient> ingredients){
		List<ItemStack> available = new ArrayList<>(inventory.outerStacks());
		List<ItemStack> used = new ArrayList<>(ingredients.size());
		ingredient:
		for(Ingredient ingredient : ingredients){
			// safe to remove in this loop, since we break immediately
			for(int i = 0; i < available.size(); i++){
				ItemStack stack = available.get(i);
				if(ingredient.test(stack)){
					available.remove(stack);
					used.add(stack);
					continue ingredient;
				}
			}
			// `ingredient` isn't present, so we can't match
			return null;
		}
		return used;
	}
	
	public ItemStack craft(InfusionInput inventory, RegistryWrapper.WrapperLookup lookup){
		return result;
	}
	
	public ItemStack getResult(RegistryWrapper.WrapperLookup lookup){
		return result;
	}
	
	public ItemStack getResult(){
		return result;
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
	
	public Optional<String> getTranslationKey(){
		return Optional.ofNullable(name);
	}
	
	public static class Serializer implements RecipeSerializer<SimpleInfusionRecipe>{
		public static final MapCodec<SimpleInfusionRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
				ItemStack.VALIDATED_CODEC.fieldOf("result").forGetter(x -> x.result),
				Ingredient.DISALLOW_EMPTY_CODEC.listOf().fieldOf("outer_ingredients").forGetter(x -> x.outerIngredients),
				Ingredient.DISALLOW_EMPTY_CODEC.fieldOf("central").forGetter(x -> x.centralIngredient),
				AspectMap.CODEC.fieldOf("aspects").forGetter(x -> x.aspects),
				Codec.INT.optionalFieldOf("instability", 1).forGetter(x -> x.instability),
				Codec.STRING.optionalFieldOf("name", null).forGetter(x -> x.name)
		).apply(i, SimpleInfusionRecipe::new));
		
		public static final PacketCodec<RegistryByteBuf, SimpleInfusionRecipe> PACKET_CODEC = PacketCodec.tuple(
				ItemStack.PACKET_CODEC, (SimpleInfusionRecipe x) -> x.result,
				Ingredient.PACKET_CODEC.collect(PacketCodecs.toList()), x -> x.outerIngredients,
				Ingredient.PACKET_CODEC, x -> x.centralIngredient,
				AspectMap.PACKET_CODEC, x -> x.aspects,
				PacketCodecs.VAR_INT, x -> x.instability,
				PacketCodecUtil.nullable(PacketCodecs.STRING), x -> x.name,
				SimpleInfusionRecipe::new
		);
		
		public MapCodec<SimpleInfusionRecipe> codec(){
			return CODEC;
		}
		
		public PacketCodec<RegistryByteBuf, SimpleInfusionRecipe> packetCodec(){
			return PACKET_CODEC;
		}
	}
}