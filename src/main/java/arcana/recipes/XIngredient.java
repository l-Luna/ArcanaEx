package arcana.recipes;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Either;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class XIngredient implements Predicate<ItemStack>{
	
	public static final Map<String, Function<String, StackMatcher>> matchers = Map.of(
			"any", __ -> new AnyMatcher(),
			"max_durability", __ -> new MaxDurabilityMatcher(),
			"enchanted_with", EnchantedWithMatcher::new
	);
	
	// one of
	private final Item item;
	private final TagKey<Item> tag;
	
	private final String matcherName, matcherParams;
	private final StackMatcher stackMatcher;
	
	// for recipe viewing
	private ItemStack[] matchingStacks = null;
	
	private XIngredient(Item item, TagKey<Item> tag, String matcherName, String matcherParams, StackMatcher stackMatcher){
		this.item = item;
		this.tag = tag;
		this.matcherName = matcherName;
		this.matcherParams = matcherParams;
		this.stackMatcher = stackMatcher;
	}
	
	public XIngredient(Item item, String matcherName, String matcherParams){
		this(item, null, matcherName, matcherParams, matchers.get(matcherName).apply(matcherParams));
	}
	
	public XIngredient(TagKey<Item> tag, String matcherName, String matcherParams){
		this(null, tag, matcherName, matcherParams, matchers.get(matcherName).apply(matcherParams));
	}
	
	public boolean test(ItemStack stack){
		return ((item != null && stack.getItem() == item) || (tag != null && stack.isIn(tag))) && stackMatcher.test(stack);
	}
	
	public static XIngredient fromJson(JsonElement element){
		if(element instanceof JsonObject jo)
			return fromJson(jo);
		throw new JsonParseException("An XIngredient must be a JSON object");
	}
	
	public static XIngredient fromJson(JsonObject json){
		String matcherName = "any", matcherParams = "";
		if(json.has("matches")){
			String matcher = JsonHelper.getString(json, "matches", "any");
			String[] parts = matcher.split(" ", 2);
			matcherName = parts[0];
			if(parts.length > 1)
				matcherParams = parts[1];
		}
		
		if(json.has("item") && json.has("tag"))
			throw new JsonParseException("An ingredient should either be an item or tag, not both");
		else if(json.has("item"))
			return new XIngredient(ShapedRecipe.getItem(json), matcherName, matcherParams);
		else if(json.has("tag")){
			TagKey<Item> tag = TagKey.of(Registry.ITEM_KEY, new Identifier(JsonHelper.getString(json, "tag")));
			return new XIngredient(tag, matcherName, matcherParams);
		}else
			throw new JsonParseException("An ingredient needs either an item or tag");
	}
	
	public void write(PacketByteBuf buf){
		if(item != null){
			buf.writeIdentifier(Registry.ITEM.getId(item));
			buf.writeBoolean(true);
		}else{
			buf.writeIdentifier(tag.id());
			buf.writeBoolean(false);
		}
		buf.writeString(matcherName);
		buf.writeString(matcherParams);
	}
	
	public static XIngredient read(PacketByteBuf buf){
		Identifier id = buf.readIdentifier();
		if(buf.readBoolean())
			return new XIngredient(Registry.ITEM.get(id), buf.readString(), buf.readString());
		else
			return new XIngredient(TagKey.of(Registry.ITEM_KEY, id), buf.readString(), buf.readString());
	}
	
	@NotNull
	public ItemStack[] getMatchingStacks(){
		return matchingStacks == null ? (matchingStacks = calcMatchingStacks()) : matchingStacks;
	}
	
	private ItemStack[] calcMatchingStacks(){
		Stream<ItemStack> candidates;
		if(item != null)
			candidates = Stream.of(new ItemStack(item));
		else
			candidates = Registry.ITEM.streamTagsAndEntries()
					.filter(x -> x.getFirst().equals(tag))
					.flatMap(x -> x.getSecond().stream())
					.map(RegistryEntry::value)
					.map(ItemStack::new);
		// TODO: apply multiple matchers in turn
		candidates = stackMatcher.applyMatching(candidates);
		return candidates.toArray(ItemStack[]::new);
	}
	
	public Ingredient basic(){
		return Ingredient.ofStacks(getMatchingStacks());
	}
	
	public Either<Item, TagKey<Item>> getContent(){
		if(item != null)
			return Either.left(item);
		else
			return Either.right(tag);
	}
	
	public interface StackMatcher extends Predicate<ItemStack>{
		
		Stream<ItemStack> applyMatching(Stream<ItemStack> in);
	}
	
	public static final class AnyMatcher implements StackMatcher{
		
		public Stream<ItemStack> applyMatching(Stream<ItemStack> in){
			return in;
		}
		
		public boolean test(ItemStack stack){
			return true;
		}
	}
	
	public static final class MaxDurabilityMatcher implements StackMatcher{
		
		public Stream<ItemStack> applyMatching(Stream<ItemStack> in){
			return in; // stacks are only suggested at full durability
		}
		
		public boolean test(ItemStack stack){
			return stack.getDamage() == stack.getMaxDamage();
		}
	}
	
	public static final class EnchantedWithMatcher implements StackMatcher{
		
		private final Enchantment enchantment;
		
		public EnchantedWithMatcher(Identifier enchantmentId){
			enchantment = Registry.ENCHANTMENT.get(enchantmentId);
		}
		
		public EnchantedWithMatcher(String enchantmentId){
			this(new Identifier(enchantmentId));
		}
		
		public Stream<ItemStack> applyMatching(Stream<ItemStack> in){
			return in.filter(stack -> enchantment.isAcceptableItem(stack) || stack.getItem() instanceof EnchantedBookItem)
					.map(ItemStack::copy)
					.peek(this::enchant);
		}
		
		public boolean test(ItemStack stack){
			return EnchantmentHelper.getLevel(enchantment, stack) > 0;
		}
		
		private void enchant(ItemStack stack){
			if(stack.getItem() == Items.ENCHANTED_BOOK)
				EnchantedBookItem.addEnchantment(stack, new EnchantmentLevelEntry(enchantment, 1));
			else
				stack.addEnchantment(enchantment, 1);
		}
	}
}
