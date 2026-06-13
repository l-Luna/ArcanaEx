package arcana.aspects;

import arcana.api.AspectRecipe;
import arcana.mixin.accessor.SmithingTransformRecipeAccessor;
import com.google.common.base.Stopwatch;
import com.google.gson.*;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceReloadListenerKeys;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Pair;
import net.minecraft.util.profiler.Profiler;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static arcana.Arcana.arcId;

public final class ItemAspectRegistry extends JsonDataLoader implements IdentifiableResourceReloadListener{
	
	private static final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	private static final Logger logger = LogUtils.getLogger();
	
	private static final Map<Item, AspectMap> itemAssociations = new HashMap<>();
	private static final Map<TagKey<Item>, AspectMap> itemTagAssociations = new HashMap<>();
	private static final Map<TagKey<Item>, AspectMap> itemTagBonuses = new HashMap<>();
	
	private static final Map<Class<? extends Recipe<?>>, Function<Recipe<?>, List<Ingredient>>> ingredientProviders = new HashMap<>();
	
	private static final Set<Item> generating = new HashSet<>();
	
	private static final Map<Item, AspectMap> itemAspects = new HashMap<>();
	private static final Collection<BiConsumer<ItemStack, AspectMap>> stackModifiers = new ArrayList<>();
	
	// TODO: would rather not do this
	public static RecipeManager recipes;
	private final RegistryWrapper.WrapperLookup lookup;
	
	public ItemAspectRegistry(RegistryWrapper.WrapperLookup lookup){
		super(gson, "arcana/aspects");
		this.lookup = lookup;
	}
	
	public Identifier getFabricId(){
		return arcId("aspects");
	}
	
	public Collection<Identifier> getFabricDependencies(){
		return Set.of(ResourceReloadListenerKeys.TAGS, ResourceReloadListenerKeys.RECIPES);
	}
	
	public static AspectMap get(ItemStack stack){
		var fromItem = get(stack.getItem());
		for(var fn : stackModifiers)
			fn.accept(stack, fromItem);
		return fromItem;
	}
	
	public static AspectMap get(Item item){
		var orig = itemAspects.get(item);
		return orig != null ? orig.copy() : new AspectMap();
	}
	
	public static Map<Item, AspectMap> getAllItemAspects(){
		return Collections.unmodifiableMap(itemAspects);
	}
	
	// for PkSyncItemAspectData
	public static void setAllItemAspects(Map<Item, AspectMap> aspects){
		itemAspects.clear();
		itemAspects.putAll(aspects);
	}
	
	protected void apply(Map<Identifier, JsonElement> prepared, ResourceManager manager, Profiler profiler){
		logger.info("Loading item aspects");
		
		// reset
		itemAssociations.clear();
		itemTagAssociations.clear();
		itemTagBonuses.clear();
		itemAspects.clear();
		stackModifiers.clear();
		ingredientProviders.clear();
		
		// always the same
		addStackFunctions();
		addIngredientProviders();
		
		// load associations
		prepared.forEach(this::applyJson);
		
		// TODO (just in case): if tags don't load, think about resource listener event again
		applyAssociations();
	}
	
	// applied after tag load event
	public void applyAssociations(){
		Stopwatch sw = Stopwatch.createStarted();
		for(Item item : Registries.ITEM){
			if(itemAssociations.containsKey(item))
				itemAspects.put(item, itemAssociations.get(item));
			else for(var tagAssoc : itemTagAssociations.entrySet()) // TODO: what if an item is in multiple tags?
				if(item.getRegistryEntry().isIn(tagAssoc.getKey())){
					itemAspects.put(item, tagAssoc.getValue());
					break;
				}
		}
		
		// compute via recipes
		computeInheritedAspects();
		
		// add bonuses after
		for(Item item : Registries.ITEM){
			var aspects = get(item);
			for(var tagBonus : itemTagBonuses.entrySet())
				if(item.getRegistryEntry().isIn(tagBonus.getKey())){
					var newAspects = new AspectMap();
					newAspects.add(aspects);
					newAspects.add(tagBonus.getValue());
					aspects = newAspects;
				}
			if(!aspects.isEmpty())
				itemAspects.put(item, aspects);
		}
		
		logger.info("Assigned aspects to {} items in {}ms", itemAspects.size(), sw.elapsed(TimeUnit.MILLISECONDS));
	}
	
	private void addStackFunctions(){
		// add 2 magic per enchantment level
		stackModifiers.add((stack, out) -> {
			var enchants = EnchantmentHelper.getEnchantments(stack);
			if(!enchants.isEmpty())
				out.add(new AspectStack(Aspects.MAGIC, enchants.getEnchantmentEntries().stream().mapToInt(Object2IntMap.Entry::getIntValue).sum() * 2));
		});
	}
	
	private void addIngredientProviders(){
		// smithing doesn't properly implement getIngredients
		ingredientProviders.put(SmithingTransformRecipe.class, recipe -> {
			if(recipe instanceof SmithingTransformRecipe sr){
				SmithingTransformRecipeAccessor acc = (SmithingTransformRecipeAccessor)sr;
				return List.of(acc.arcana$getTemplate(), acc.arcana$getBase(), acc.arcana$getAddition());
			}else return null;
		});
	}
	
	private void applyJson(Identifier file, JsonElement json){
		if(json.isJsonObject()){
			JsonObject object = json.getAsJsonObject();
			for(Map.Entry<String, JsonElement> entry : object.entrySet()){
				String key = entry.getKey();
				JsonElement value = entry.getValue();
				boolean additional = key.startsWith("+");
				if(additional)
					key = key.substring(1);
				if(key.startsWith("#")){
					TagKey<Item> tag = TagKey.of(RegistryKeys.ITEM, Identifier.of(key.substring(1)));
					parseAspectStackList(file, value).ifPresent(x -> (additional ? itemTagBonuses : itemTagAssociations).put(tag, x));
				}else{
					Item item = Registries.ITEM.get(Identifier.of(key));
					if(item != Items.AIR)
						parseAspectStackList(file, value).ifPresent(x -> itemAssociations.put(item, x));
					else
						logger.warn("Invalid item \"{}\" in file \"{}\", ignoring", key, file);
				}
			}
		}else
			logger.warn("Root in aspect map \"{}\" is not a JSON object, ignoring", file);
	}
	
	public static Optional<AspectMap> parseAspectStackList(Identifier file, JsonElement json){
		if(json.isJsonArray()){
			JsonArray array = json.getAsJsonArray();
			AspectMap ret = new AspectMap();
			for(JsonElement element : array){
				if(element.isJsonObject()){
					JsonObject object = element.getAsJsonObject();
					String aspectName = object.get("aspect").getAsString();
					int amount = JsonHelper.getInt(object, "amount", 1);
					Aspect aspect = Aspects.byName(aspectName);
					if(aspect != null)
						ret.add(aspect, amount);
					else
						logger.warn("Invalid aspect \"{}\" referenced in file \"{}\", ignoring", aspectName, file);
				}else if(element.isJsonPrimitive()){
					JsonPrimitive p = element.getAsJsonPrimitive();
					String name = p.getAsString();
					int amount = 1;
					if(name.contains("*")){
						var split = name.split("\\*", 2);
						amount = Integer.parseInt(split[0]);
						name = split[1];
					}
					Aspect aspect = Aspects.byName(name);
					if(aspect != null)
						ret.add(aspect, amount);
					else
						logger.warn("Invalid aspect \"{}\" referenced in file \"{}\", ignoring", name, file);
				}else
					logger.warn("Aspect stack in file \"{}\" is not an object or array, ignoring", file);
			}
			return Optional.of(ret);
		}else
			logger.warn("Aspect stack list in file \"{}\" is not a JSON list, ignoring", file);
		return Optional.empty();
	}
	
	private void computeInheritedAspects(){
		// TODO: this is a naive approach
		for(RecipeEntry<?> recipe : recipes.values()){
			var output = recipe.value().getResult(lookup).getItem();
			if(!itemAspects.containsKey(output))
				generate(output);
			generating.clear();
		}
	}
	
	private AspectMap getOrGenerate(ItemStack stack){
		// TODO: apply stack modifiers to generated items here
		return itemAspects.containsKey(stack.getItem()) ? get(stack) : generate(stack.getItem());
	}
	
	private AspectMap generate(Item item){
		if(item == Items.AIR)
			return new AspectMap();
		if(generating.contains(item)){
			// counts as nothing to itself
			logger.warn("Encountered cycle picking aspects for {}", Registries.ITEM.getId(item));
			return new AspectMap();
		}
		generating.add(item);
		// consider every recipe that produces this
		List<AspectMap> choices = new ArrayList<>();
		for(RecipeEntry<?> recipeEntry : recipes.values()){
			Recipe<?> recipe = recipeEntry.value();
			ItemStack result = recipe.getResult(lookup);
			if(result.getItem().equals(item) && result.getCount() > 0){
				AspectMap collected = new AspectMap();
				List<Ingredient> ingredients = recipe.getIngredients();
				if(ingredientProviders.containsKey(recipe.getClass())){
					var i = ingredientProviders.get(recipe.getClass()).apply(recipe);
					if(i != null)
						ingredients = i;
				}
				// collect aspects from every ingredient
				for(Ingredient ingredient : ingredients){
					var stacks = ingredient.getMatchingStacks();
					if(stacks.length > 0){
						// currently only look at the first possible stack
						var stack = stacks[0];
						if(!generating.contains(stack.getItem()))
							collected.add(getOrGenerate(stack));
					}
				}
				if(recipe instanceof AspectRecipe ar)
					ar.affect(collected);
				// divide by the amount produced
				for(AspectStack stack : collected.asStacks())
					collected.set(stack.type(), stack.amount() / result.getCount());
				choices.add(collected);
			}
		}
		// choose the recipe that provides the fewest aspects
		var choice = choices.stream()
				.map(x -> new Pair<>(x, x.asStacks().stream().mapToInt(AspectStack::amount).sum()))
				.filter(x -> x.getRight() > 0)
				.min(Comparator.comparingInt(Pair::getRight))
				.map(Pair::getLeft).orElseGet(AspectMap::new);
		// store the choice for future use
		itemAspects.put(item, choice);
		generating.remove(item);
		return choice;
	}
}