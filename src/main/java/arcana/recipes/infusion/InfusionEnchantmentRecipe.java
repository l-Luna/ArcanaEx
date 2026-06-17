package arcana.recipes.infusion;

import arcana.api.RenamableRecipe;
import arcana.aspects.AspectMap;
import arcana.recipes.ArcanaRecipe;
import arcana.util.PacketCodecUtil;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryElementCodec;
import net.minecraft.registry.entry.RegistryEntry;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static arcana.Arcana.arcId;

public class InfusionEnchantmentRecipe implements InfusionRecipe, ArcanaRecipe, RenamableRecipe{
	
	public static Serializer SERIALIZER;
	
	private final RegistryEntry<Enchantment> enchantment;
	private final List<Ingredient> baseIngredients;
	private final AspectMap baseAspects;
	private final ItemStack preview;
	
	private final int baseInstability;
	private final @Nullable String name;
	
	public static void setup(){
		SERIALIZER = Registry.register(
				Registries.RECIPE_SERIALIZER,
				arcId("infusion_enchantment"),
				new Serializer()
		);
	}
	
	public InfusionEnchantmentRecipe(RegistryEntry<Enchantment> enchantment, List<Ingredient> baseIngredients, AspectMap baseAspects, ItemStack preview, int baseInstability, @Nullable String name){
		this.enchantment = enchantment;
		this.baseIngredients = baseIngredients;
		this.baseAspects = baseAspects;
		this.preview = preview;
		this.baseInstability = baseInstability;
		this.name = name;
	}
	
	public Optional<String> getTranslationKey(){
		return Optional.ofNullable(name);
	}
	
	public BakedInfusionRecipe craftInfusion(InfusionInput inventory){
		ItemStack central = inventory.centre().copy();
		int currentLevel = EnchantmentHelper.getLevel(enchantment, central);
		// TODO: check incompatible enchantments
		// TODO: check dynamic max levels
		if(!(enchantment.value().isAcceptableItem(central) || central.isOf(Items.BOOK)))
			return null;
		int multiplier = 1 << currentLevel;
		AspectMap cost = baseAspects.copy();
		cost.multiply(__ -> (float)multiplier);
		if(!inventory.aspects().contains(cost))
			return null;
		List<Ingredient> actualIngredients = new ArrayList<>(baseIngredients.size() * multiplier);
		for(int i = 0; i < multiplier; i++)
			actualIngredients.addAll(baseIngredients);
		List<ItemStack> used = SimpleInfusionRecipe.matchIngredients(inventory, actualIngredients);
		if(used == null)
			return null;
		EnchantmentHelper.apply(central, x -> x.set(enchantment, currentLevel + 1));
		return new BakedInfusionRecipe(central, used, cost, baseInstability + currentLevel + 1);
	}
	
	public ItemStack getResult(RegistryWrapper.WrapperLookup lookup){
		return preview;
	}
	
	public RegistryEntry<Enchantment> getEnchantment(){
		return enchantment;
	}
	
	public List<Ingredient> getBaseIngredients(){
		return baseIngredients;
	}
	
	public AspectMap getBaseAspects(){
		return baseAspects;
	}
	
	public int getBaseInstability(){
		return baseInstability;
	}
	
	public ItemStack getPreviewStack(){
		return preview;
	}
	
	public RecipeSerializer<?> getSerializer(){
		return null;
	}
	
	public RecipeType<?> getType(){
		return SimpleInfusionRecipe.TYPE;
	}
	
	public static class Serializer implements RecipeSerializer<InfusionEnchantmentRecipe>{
		
		private static final MapCodec<InfusionEnchantmentRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
				RegistryElementCodec.of(RegistryKeys.ENCHANTMENT, Enchantment.CODEC).fieldOf("enchantment").forGetter(InfusionEnchantmentRecipe::getEnchantment),
				Ingredient.DISALLOW_EMPTY_CODEC.listOf().fieldOf("base_ingredients").forGetter(InfusionEnchantmentRecipe::getBaseIngredients),
				AspectMap.CODEC.fieldOf("base_aspects").forGetter(InfusionEnchantmentRecipe::getBaseAspects),
				ItemStack.VALIDATED_CODEC.fieldOf("preview").forGetter(InfusionEnchantmentRecipe::getPreviewStack),
				Codec.INT.optionalFieldOf("base_instability", 0).forGetter(InfusionEnchantmentRecipe::getBaseInstability),
				Codec.STRING.optionalFieldOf("name").forGetter(x -> Optional.ofNullable(x.name))
		).apply(i, (a, b, c, d, e, f) -> new InfusionEnchantmentRecipe(a, b, c, d, e, f.orElse(null))));
		
		private static final PacketCodec<RegistryByteBuf, InfusionEnchantmentRecipe> PACKET_CODEC = PacketCodec.tuple(
				PacketCodecs.registryEntry(RegistryKeys.ENCHANTMENT), InfusionEnchantmentRecipe::getEnchantment,
				Ingredient.PACKET_CODEC.collect(PacketCodecs.toList()), InfusionEnchantmentRecipe::getBaseIngredients,
				AspectMap.PACKET_CODEC, InfusionEnchantmentRecipe::getBaseAspects,
				ItemStack.PACKET_CODEC, InfusionEnchantmentRecipe::getPreviewStack,
				PacketCodecs.VAR_INT, InfusionEnchantmentRecipe::getBaseInstability,
				PacketCodecUtil.nullable(PacketCodecs.STRING), x -> x.name,
				InfusionEnchantmentRecipe::new
		);
		
		public MapCodec<InfusionEnchantmentRecipe> codec(){
			return CODEC;
		}
		
		public PacketCodec<RegistryByteBuf, InfusionEnchantmentRecipe> packetCodec(){
			return PACKET_CODEC;
		}
	}
}