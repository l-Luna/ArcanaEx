package arcana.enchantments;

import arcana.util.RegistryMapping;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.enchantment.EnchantmentLevelBasedValue;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public record LootSwapEffect(EnchantmentLevelBasedValue chance, Identifier mapping){

	public static final Codec<LootSwapEffect> CODEC = RecordCodecBuilder.create(i -> i.group(
			EnchantmentLevelBasedValue.CODEC.fieldOf("chance").forGetter(LootSwapEffect::chance),
			Identifier.CODEC.fieldOf("mapping").forGetter(LootSwapEffect::mapping)
	).apply(i, LootSwapEffect::new));
	
	public static final RegistryMapping<Item>
			PURIFYING_MAP = new RegistryMapping<>(Registries.ITEM),
			TRANSMUTATIVE_MAP = new RegistryMapping<>(Registries.ITEM);
	
	/*
	
	@NotNull
	public static List<ItemStack> applyLootSwaps(@NotNull List<ItemStack> original, ItemStack stack, Random rng){
		List<ItemStack> newDrops = null;
		for(Map.Entry<Enchantment, Integer> enchant : EnchantmentHelper.get(stack).entrySet())
			if(enchant.getKey() instanceof LootSwapEnchantment ls){
				// only allocate new list if loot swaps are actually present
				if(newDrops == null)
					newDrops = new ArrayList<>(original);
				// reverse loop for removal
				for(int i = newDrops.size() - 1; i >= 0; i--)
					processStack(ls, rng, enchant.getValue(), newDrops.remove(i), newDrops::add);
			}
		return newDrops != null ? newDrops : original;
	}
	
	public static Consumer<ItemStack> applyLootSwaps(Consumer<ItemStack> next, ItemStack stack, Random rng){
		Consumer<ItemStack> it = next;
		for(Map.Entry<Enchantment, Integer> enchant : EnchantmentHelper.get(stack).entrySet()){
			if(enchant.getKey() instanceof LootSwapEnchantment ls){
				Consumer<ItemStack> prev = it;
				it = item -> processStack(ls, rng, enchant.getValue(), item, prev);
			}
		}
		return it;
	}
	
	public static void processStack(LootSwapEffect enchantment, Random rng, int level, ItemStack in, Consumer<ItemStack> out){
		Item targetItem = enchantment.swaps.apply(in.getItem()).orElse(null);
		float chance = level * enchantment.baseChance;
		if(targetItem != null){
			if(chance >= 1)
				out.accept(new ItemStack(targetItem, in.getCount()));
			else{
				// naive implementation but stack sizes are small
				int newAmount = 0;
				for(int c = 0; c < in.getCount(); c++)
					if(rng.nextFloat() < chance){
						in.decrement(1);
						newAmount++;
					}
				if(!in.isEmpty())
					out.accept(in);
				if(newAmount > 0)
					out.accept(new ItemStack(targetItem, newAmount));
			}
		}else
			out.accept(in);
	}
	
	*/
}