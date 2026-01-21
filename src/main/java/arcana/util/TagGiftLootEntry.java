package arcana.util;

import arcana.ArcanaTags;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.entry.LeafEntry;
import net.minecraft.loot.entry.LootPoolEntryType;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;

import java.util.function.Consumer;

// Similar to TagEntry, but one item is chosen from the tag at random
public class TagGiftLootEntry extends LeafEntry{

	public static final LootPoolEntryType TYPE = new LootPoolEntryType(new Serializer());
	
	public final TagKey<Item> tag;
	
	protected TagGiftLootEntry(int weight, int quality, LootCondition[] conditions, LootFunction[] functions, TagKey<Item> tag){
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
	
	public static class Serializer extends LeafEntry.Serializer<TagGiftLootEntry>{
		public void addEntryFields(JsonObject obj, TagGiftLootEntry entry, JsonSerializationContext ctx){
			super.addEntryFields(obj, entry, ctx);
			obj.addProperty("name", entry.tag.id().toString());
		}
		
		protected TagGiftLootEntry fromJson(
				JsonObject jsonObject,
				JsonDeserializationContext ctx,
				int weight,
				int quality,
				LootCondition[] conditions,
				LootFunction[] functions){
			TagKey<Item> tag = TagKey.of(Registry.ITEM_KEY, new Identifier(JsonHelper.getString(jsonObject, "name")));
			return new TagGiftLootEntry(weight, quality, conditions, functions, tag);
		}
	}
}