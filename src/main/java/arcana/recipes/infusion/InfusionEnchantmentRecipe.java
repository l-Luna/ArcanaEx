package arcana.recipes.infusion;

import arcana.api.DynamicMaxLevelEnchantment;
import arcana.api.RenamableRecipe;
import arcana.aspects.AspectMap;
import arcana.aspects.ItemAspectRegistry;
import arcana.recipes.ArcanaRecipe;
import arcana.recipes.XIngredient;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static arcana.Arcana.arcId;

public class InfusionEnchantmentRecipe implements InfusionRecipe, ArcanaRecipe, RenamableRecipe{
	
	public static Serializer SERIALIZER;
	
	private final Enchantment enchantment;
	private final List<XIngredient> baseIngredients;
	private final AspectMap baseAspects;
	private final ItemStack preview;
	
	private final Identifier id;
	private final int baseInstability;
	private final @Nullable String name;
	
	public static void setup(){
		SERIALIZER = Registry.register(
				Registry.RECIPE_SERIALIZER,
				arcId("infusion_enchantment"),
				new Serializer()
		);
	}
	
	public InfusionEnchantmentRecipe(Enchantment enchantment, List<XIngredient> baseIngredients, AspectMap baseAspects, ItemStack preview, Identifier id, int baseInstability, @Nullable String name){
		this.enchantment = enchantment;
		this.baseIngredients = baseIngredients;
		this.baseAspects = baseAspects;
		this.preview = preview;
		this.id = id;
		this.baseInstability = baseInstability;
		this.name = name;
	}
	
	public Optional<String> getTranslationKey(){
		return Optional.ofNullable(name);
	}
	
	public BakedInfusionRecipe craftInfusion(InfusionInventory inventory){
		ItemStack central = inventory.centre.copy();
		int currentLevel = EnchantmentHelper.getLevel(enchantment, central);
		if(!(enchantment.isAcceptableItem(central) || central.isOf(Items.BOOK)) || currentLevel >= DynamicMaxLevelEnchantment.getMaxLevel(enchantment, central))
			return null;
		int multiplier = 1 << currentLevel;
		AspectMap cost = baseAspects.copy();
		cost.multiply(__ -> (float)multiplier);
		if(!inventory.aspects.contains(cost))
			return null;
		List<XIngredient> actualIngredients = new ArrayList<>(baseIngredients.size() * multiplier);
		for(int i = 0; i < multiplier; i++)
			actualIngredients.addAll(baseIngredients);
		List<ItemStack> used = SimpleInfusionRecipe.matchIngredients(inventory, actualIngredients);
		if(used == null)
			return null;
		Map<Enchantment, Integer> currentEnchants = EnchantmentHelper.get(central);
		currentEnchants.put(enchantment, currentLevel + 1);
		EnchantmentHelper.set(currentEnchants, central);
		return new BakedInfusionRecipe(central, used, cost, baseInstability + currentLevel + 1);
	}
	
	public ItemStack getOutput(){
		return preview;
	}
	
	public Identifier getId(){
		return id;
	}
	
	public Enchantment getEnchantment(){
		return enchantment;
	}
	
	public List<XIngredient> getBaseIngredients(){
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
		
		public InfusionEnchantmentRecipe read(Identifier id, JsonObject json){
			Enchantment enchantment = Registry.ENCHANTMENT.get(Identifier.of(JsonHelper.getString(json, "enchantment")));
			ItemStack preview = ShapedRecipe.outputFromJson(JsonHelper.getObject(json, "preview"));
			List<XIngredient> baseIngredients = new ArrayList<>();
			for(JsonElement ingredients : JsonHelper.getArray(json, "base_ingredients"))
				baseIngredients.add(XIngredient.fromJson(ingredients));
			AspectMap baseAspects = ItemAspectRegistry.parseAspectStackList(id, JsonHelper.getArray(json, "base_aspects")).orElseGet(AspectMap::new);
			int instability = JsonHelper.getInt(json, "base_instability", 0);
			String name = JsonHelper.getString(json, "name", null);
			
			return new InfusionEnchantmentRecipe(enchantment, baseIngredients, baseAspects, preview, id, instability, name);
		}
		
		public void write(PacketByteBuf buf, InfusionEnchantmentRecipe recipe){
			// name?, preview, enchantment, baseIngredients, baseEssentia, instability
			buf.writeBoolean(recipe.name != null);
			if(recipe.name != null)
				buf.writeString(recipe.name);
			buf.writeItemStack(recipe.preview);
			buf.writeRegistryValue(Registry.ENCHANTMENT, recipe.enchantment);
			buf.writeVarInt(recipe.baseIngredients.size());
			for(XIngredient ingredient : recipe.baseIngredients)
				ingredient.write(buf);
			buf.writeNbt(recipe.baseAspects.toNbt());
			buf.writeVarInt(recipe.baseInstability);
		}
		
		public InfusionEnchantmentRecipe read(Identifier id, PacketByteBuf buf){
			String name = null;
			if(buf.readBoolean())
				name = buf.readString();
			ItemStack preview = buf.readItemStack();
			Enchantment enchantment = buf.readRegistryValue(Registry.ENCHANTMENT);
			int size = buf.readVarInt();
			List<XIngredient> baseIngredients = new ArrayList<>(size);
			for(int i = 0; i < size; i++)
				baseIngredients.add(XIngredient.read(buf));
			AspectMap baseEssentia = AspectMap.fromNbt(buf.readNbt());
			int instability = buf.readVarInt();
			return new InfusionEnchantmentRecipe(enchantment, baseIngredients, baseEssentia, preview, id, instability, name);
		}
	}
}