package arcana.aspects;

import com.google.gson.*;
import com.mojang.logging.LogUtils;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceReloadListenerKeys;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.SmithingRecipe;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Pair;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.Registry;
import org.slf4j.Logger;

import java.util.*;
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
	
	public ItemAspectRegistry(){
		super(gson, "arcana/aspects");
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
	}
	
	// applied after tag load event
	public void applyAssociations(){
		for(Item item : Registry.ITEM){
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
		for(Item item : Registry.ITEM){
			var aspects = get(item);
			for(var tagBonus : itemTagBonuses.entrySet())
				if(item.getRegistryEntry().isIn(tagBonus.getKey())){
					var newAspects = new AspectMap();
					newAspects.add(aspects);
					newAspects.add(tagBonus.getValue());
					aspects = newAspects;
				}
			if(aspects.size() > 0)
				itemAspects.put(item, aspects);
		}
		
		logger.info("Assigned aspects to %s items".formatted(itemAspects.size()));
	}
	
	private void addStackFunctions(){
		// add 2 magic per enchantment level
		stackModifiers.add((stack, out) -> {
			var enchants = EnchantmentHelper.get(stack);
			if(enchants.size() > 0)
				out.add(new AspectStack(Aspects.MAGIC, enchants.values().stream().mapToInt(x -> x).sum() * 2));
		});
	}
	
	private void addIngredientProviders(){
		// smithing doesn't properly implement getIngredients
		ingredientProviders.put(SmithingRecipe.class, recipe -> {
			if(recipe instanceof SmithingRecipe sr){
				return List.of(sr.base, sr.addition);
			}else return null; // unreachable
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
					TagKey<Item> tag = TagKey.of(Registry.ITEM_KEY, new Identifier(key.substring(1)));
					parseAspectStackList(file, value).ifPresent(x -> (additional ? itemTagBonuses : itemTagAssociations).put(tag, x));
				}else{
					Item item = Registry.ITEM.get(new Identifier(key));
					if(item != Items.AIR)
						parseAspectStackList(file, value).ifPresent(x -> itemAssociations.put(item, x));
					else
						logger.warn("Invalid item \"%s\" in file \"%s\", ignoring".formatted(key, file));
				}
			}
		}else
			logger.warn("Root in aspect map \"%s\" is not a JSON object, ignoring".formatted(file));
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
						logger.warn("Invalid aspect \"%s\" referenced in file \"%s\", ignoring".formatted(aspectName, file));
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
						logger.warn("Invalid aspect \"%s\" referenced in file \"%s\", ignoring".formatted(name, file));
				}else
					logger.warn("Aspect stack in file \"" + file + "\" is not an object or array, ignoring");
			}
			return Optional.of(ret);
		}else
			logger.warn("Aspect stack list in file \"%s\" is not a JSON list, ignoring".formatted(file));
		return Optional.empty();
	}
	
	private void computeInheritedAspects(){
		// TODO: this is a naive approach
		// the proper way to do this would be to create a directed graph between items with recipes as edges,
		// removing all edges into items with set aspects,
		// removing all cycles in the graph (how?),
		// topologically sorting it so that every item is processed after all possible ingredients,
		// calculating the aspects assigned by each recipe, and then choosing whichever provides the least
		
		// here we simply look at each possible craftable item and recursively generate aspects,
		// producing weird behaviour on cycles
		// this is also quadratic over recipes
		for(Recipe<?> recipe : recipes.values()){
			var output = recipe.getOutput().getItem();
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
		if(generating.contains(item)) // counts as nothing to itself
			return new AspectMap();
		generating.add(item);
		// consider every recipe that produces this
		List<AspectMap> choices = new ArrayList<>();
		for(Recipe<?> recipe : recipes.values()){
			if(recipe.getOutput().getItem().equals(item)){
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
					collected.set(stack.type(), stack.amount() / recipe.getOutput().getCount());
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