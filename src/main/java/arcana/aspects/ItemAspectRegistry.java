package arcana.aspects;

import arcana.util.JsonUtil;
import arcana.util.StreamUtil;
import com.google.gson.*;
import com.mojang.logging.LogUtils;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.Registry;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static arcana.Arcana.arcId;

public final class ItemAspectRegistry extends JsonDataLoader implements IdentifiableResourceReloadListener{
	
	private static final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	private static final Logger logger = LogUtils.getLogger();
	
	private static final Map<Item, List<AspectStack>> itemAssociations = new HashMap<>();
	private static final Map<TagKey<Item>, List<AspectStack>> itemTagAssociations = new HashMap<>();
	private static final Collection<BiConsumer<ItemStack, List<AspectStack>>> stackModifiers = new ArrayList<>();
	
	private static final Set<Item> generating = new HashSet<>();
	
	private static final Map<Item, List<AspectStack>> itemAspects = new HashMap<>();
	
	public static RecipeManager recipes;
	
	public ItemAspectRegistry(){
		super(gson, "arcana/aspects");
	}
	
	public Identifier getFabricId(){
		return arcId("aspects");
	}
	
	public static List<AspectStack> get(ItemStack stack){
		var fromItem = get(stack.getItem());
		for(var fn : stackModifiers)
			fn.accept(stack, fromItem);
		return fromItem;
	}
	
	public static List<AspectStack> get(Item item){
		var orig = itemAspects.get(item);
		return orig != null ? new ArrayList<>(orig) : new ArrayList<>();
	}
	
	protected void apply(Map<Identifier, JsonElement> prepared, ResourceManager manager, Profiler profiler){
		logger.info("Loading item aspects");
		
		// reset
		itemAssociations.clear();
		itemTagAssociations.clear();
		itemAspects.clear();
		stackModifiers.clear();
		
		// always the same
		addStackFunctions();
		
		// apply hardcoded associations
		prepared.forEach(this::applyJson);
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
	}
	
	private void addStackFunctions(){
		// add 2 magic per enchantment level
		stackModifiers.add((stack, out) -> {
			var enchants = EnchantmentHelper.get(stack);
			if(enchants.size() > 0)
				out.add(new AspectStack(Aspects.MAGIC, enchants.values().stream().mapToInt(x -> x).sum() * 2));
		});
	}
	
	private void applyJson(Identifier file, JsonElement json){
		if(json.isJsonObject()){
			JsonObject object = json.getAsJsonObject();
			for(Map.Entry<String, JsonElement> entry : object.entrySet()){
				String key = entry.getKey();
				JsonElement value = entry.getValue();
				if(key.startsWith("#")){
					TagKey<Item> tag = TagKey.of(Registry.ITEM_KEY, new Identifier(key.substring(1)));
					parseAspectStackList(file, value).ifPresent(x -> itemTagAssociations.put(tag, x));
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
	
	private static Optional<List<AspectStack>> parseAspectStackList(Identifier file, JsonElement json){
		if(json.isJsonArray()){
			JsonArray array = json.getAsJsonArray();
			List<AspectStack> ret = new ArrayList<>();
			for(JsonElement element : array){
				if(element.isJsonObject()){
					JsonObject object = element.getAsJsonObject();
					String aspectName = object.get("aspect").getAsString();
					int amount = JsonUtil.getInt(object, "amount", 1);
					Aspect aspect = Aspects.byName(aspectName);
					if(aspect != null)
						ret.add(new AspectStack(aspect, amount));
					else
						logger.warn("Invalid aspect \"%s\" referenced in file \"%s\", ignoring".formatted(aspectName, file));
				}else
					logger.warn("Aspect stack in file \"" + file + "\" is not an object, ignoring");
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
	
	private List<AspectStack> getOrGenerate(ItemStack stack){
		// TODO: apply stack modifiers to generated items here
		return itemAspects.containsKey(stack.getItem()) ? get(stack) : generate(stack.getItem());
	}
	
	private List<AspectStack> generate(Item item){
		if(generating.contains(item)) // counts as nothing to itself
			return Collections.emptyList();
		generating.add(item);
		// consider every recipe that produces this
		List<List<AspectStack>> choices = new ArrayList<>();
		for(Recipe<?> recipe : recipes.values()){
			if(recipe.getOutput().getItem().equals(item)){
				List<AspectStack> collected = new ArrayList<>();
				// collect aspects from every ingredient
				for(Ingredient ingredient : recipe.getIngredients()){
					var stacks = ingredient.getMatchingStacks();
					if(stacks.length > 0){
						// currently only look at the first possible stack
						var stack = stacks[0];
						if(!generating.contains(stack.getItem()))
							collected.addAll(getOrGenerate(stack));
					}
				}
				// squish stacks of the same type & divide by the amount produced
				collected = squish(collected.stream())
						.map(x -> new AspectStack(x.type(), x.amount() / recipe.getOutput().getCount()))
						.toList();
				choices.add(collected);
			}
		}
		// choose the recipe that provides the fewest aspects
		var choice = choices.stream()
				.map(x -> new Pair<>(x, x.stream().mapToInt(AspectStack::amount).sum()))
				.filter(x -> x.getRight() > 0)
				.min(Comparator.comparingInt(Pair::getRight))
				.map(Pair::getLeft).orElseGet(ArrayList::new);
		// store the choice for future use
		itemAspects.put(item, choice);
		generating.remove(item);
		return choice;
	}
	
	private static Stream<AspectStack> squish(Stream<AspectStack> unsquished){
		return StreamUtil.partialReduce(unsquished, AspectStack::type, (x, y) -> new AspectStack(x.type(), x.amount() + y.amount()));
	}
}