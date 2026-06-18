package arcana.util;

import arcana.ArcanaTags;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.entry.LeafEntry;
import net.minecraft.loot.entry.LootPoolEntryType;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

import java.util.List;
import java.util.function.Consumer;

// Similar to TagEntry, but one item is chosen from the tag at random
public class TagGiftLootEntry extends LeafEntry{

	public static final MapCodec<TagGiftLootEntry> CODEC = RecordCodecBuilder.mapCodec(i -> i
			.group(TagKey.unprefixedCodec(RegistryKeys.ITEM).fieldOf("tag").forGetter(x -> x.tag))
			.and(addLeafFields(i))
			.apply(i, (tag, weight, quality, cond, fs) -> new TagGiftLootEntry(weight, quality, cond, fs, tag)));
	
	public static final LootPoolEntryType TYPE = new LootPoolEntryType(CODEC);
	
	public final TagKey<Item> tag;
	
	protected TagGiftLootEntry(int weight, int quality, List<LootCondition> conditions, List<LootFunction> functions, TagKey<Item> tag){
		super(weight, quality, conditions, functions);
		this.tag = tag;
	}
	
	protected void generateLoot(Consumer<ItemStack> lootConsumer, LootContext context){
		Item item = ArcanaTags.randomItemIn(tag, context.getRandom());
		if(item != null)
			lootConsumer.accept(new ItemStack(item));
	}
	
	public LootPoolEntryType getType(){
		return TYPE;
	}
}