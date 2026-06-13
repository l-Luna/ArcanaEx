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
import net.minecraft.potion.Potion;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class XIngredient implements Predicate<ItemStack>{
	
	public static final Map<String, Function<String, StackMatcher>> matchers = Map.of(
			"any", __ -> new AnyMatcher(),
			"max_durability", __ -> new MaxDurabilityMatcher(),
			"enchanted_with", EnchantedWithMatcher::new,
			"has_potion_type", PotionTypeMatcher::new
	);
	
	// one of
	private final Item item;
	private final TagKey<Item> tag;
	
	@NotNull
	private final StackMatcher stackMatcher;
	
	// for recipe viewing
	private ItemStack[] matchingStacks = null;
	
	private XIngredient(Item item, TagKey<Item> tag, @NotNull StackMatcher stackMatcher){
		this.item = item;
		this.tag = tag;
		this.stackMatcher = stackMatcher;
	}
	
	public XIngredient(Item item, @NotNull StackMatcher matcher){
		this(item, null, matcher);
	}
	
	public XIngredient(TagKey<Item> tag, @NotNull StackMatcher matcher){
		this(null, tag, matcher);
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
		StackMatcher stackMatcher = AnyMatcher.INSTANCE;
		if(json.has("matches"))
			stackMatcher = matcherFromString(JsonHelper.getString(json, "matches", "any"));
		
		if(json.has("item") && json.has("tag"))
			throw new JsonParseException("An ingredient should either be an item or tag, not both");
		else if(json.has("item"))
			return new XIngredient(ShapedRecipe.getItem(json), stackMatcher);
		else if(json.has("tag")){
			TagKey<Item> tag = TagKey.of(RegistryKeys.ITEM, Identifier.of(JsonHelper.getString(json, "tag")));
			return new XIngredient(tag, stackMatcher);
		}else
			throw new JsonParseException("An ingredient needs either an item or tag");
	}
	
	public void write(PacketByteBuf buf){
		if(item != null){
			buf.writeIdentifier(Registries.ITEM.getId(item));
			buf.writeBoolean(true);
		}else{
			buf.writeIdentifier(tag.id());
			buf.writeBoolean(false);
		}
		buf.writeString(stackMatcher.asString());
	}
	
	public static XIngredient read(PacketByteBuf buf){
		Identifier id = buf.readIdentifier();
		if(buf.readBoolean())
			return new XIngredient(Registries.ITEM.get(id), matcherFromString(buf.readString()));
		else
			return new XIngredient(TagKey.of(RegistryKeys.ITEM, id), matcherFromString(buf.readString()));
	}
	
	public static StackMatcher matcherFromString(String desc){
		var split = desc.split(" ", 2);
		String matcherName = split[0];
		String matcherParams = split.length > 1 ? split[1] : "";
		return matchers.get(matcherName).apply(matcherParams);
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
			candidates = Registries.ITEM.streamTagsAndEntries()
					.filter(x -> x.getFirst().equals(tag))
					.flatMap(x -> x.getSecond().stream())
					.map(RegistryEntry::value)
					.map(ItemStack::new);
		// TODO: apply multiple matchers in turn
		candidates = stackMatcher.previewStream(candidates);
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
		
		ItemStack preview(ItemStack in);
		
		Stream<ItemStack> previewStream(Stream<ItemStack> in);
		
		String asString();
	}
	
	public static final class AnyMatcher implements StackMatcher{
		
		public static final AnyMatcher INSTANCE = new AnyMatcher();
		
		public ItemStack preview(ItemStack in){
			return in;
		}
		
		public Stream<ItemStack> previewStream(Stream<ItemStack> in){
			return in;
		}
		
		public boolean test(ItemStack stack){
			return true;
		}
		
		public String asString(){
			return "any";
		}
	}
	
	public static final class MaxDurabilityMatcher implements StackMatcher{
		
		public ItemStack preview(ItemStack in){
			return in;
		}
		
		public Stream<ItemStack> previewStream(Stream<ItemStack> in){
			return in; // stacks are only suggested at full durability
		}
		
		public boolean test(ItemStack stack){
			return stack.getDamage() == stack.getMaxDamage();
		}
		
		public String asString(){
			return "max_durability";
		}
	}
	
	public static final class EnchantedWithMatcher implements StackMatcher{
		
		private final Enchantment enchantment;
		
		public EnchantedWithMatcher(Identifier enchantmentId){
			enchantment = Registries.ENCHANTMENT.get(enchantmentId);
		}
		
		public EnchantedWithMatcher(String enchantmentId){
			this(Identifier.of(enchantmentId));
		}
		
		public ItemStack preview(ItemStack in){
			ItemStack stack = in.copy();
			enchant(stack);
			return stack;
		}
		
		public Stream<ItemStack> previewStream(Stream<ItemStack> in){
			return in.filter(stack -> enchantment.isAcceptableItem(stack) || stack.getItem() instanceof EnchantedBookItem)
					.map(ItemStack::copy)
					.peek(this::enchant);
		}
		
		public boolean test(ItemStack stack){
			Map<Enchantment, Integer> enchants = EnchantmentHelper.get(stack);
			return enchants.containsKey(enchantment) && enchants.get(enchantment) >= 1;
		}
		
		public String asString(){
			return "enchanted_with " + Registries.ENCHANTMENT.getId(enchantment);
		}
		
		private void enchant(ItemStack stack){
			if(stack.getItem() == Items.ENCHANTED_BOOK)
				EnchantedBookItem.addEnchantment(stack, new EnchantmentLevelEntry(enchantment, 1));
			else
				stack.addEnchantment(enchantment, 1);
		}
	}
	
	public static final class PotionTypeMatcher implements StackMatcher{
		
		private final Potion potion;
		
		public PotionTypeMatcher(String potionId){
			potion = Potion.byId(potionId);
		}
		
		public ItemStack preview(ItemStack in){
			return PotionUtil.setPotion(in, potion);
		}
		
		public Stream<ItemStack> previewStream(Stream<ItemStack> in){
			return in.map(x -> PotionUtil.setPotion(x, potion));
		}
		
		public boolean test(ItemStack stack){
			return Objects.equals(PotionUtil.getPotion(stack), potion);
		}
		
		public String asString(){
			return "has_potion_type " + Registries.POTION.getId(potion);
		}
	}
}