package arcana.enchantments;

import arcana.util.RegistryMapping;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelBasedValue;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static arcana.Arcana.arcId;

public record LootSwapEffect(EnchantmentLevelBasedValue chance, Identifier mapping){

	public static final Codec<LootSwapEffect> CODEC = RecordCodecBuilder.create(i -> i.group(
			EnchantmentLevelBasedValue.CODEC.fieldOf("chance").forGetter(LootSwapEffect::chance),
			Identifier.CODEC.fieldOf("mapping").forGetter(LootSwapEffect::mapping)
	).apply(i, LootSwapEffect::new));
	
	public static final RegistryMapping<Item>
			PURIFYING_MAP = new RegistryMapping<>(Registries.ITEM),
			TRANSMUTATIVE_MAP = new RegistryMapping<>(Registries.ITEM);
	
	@NotNull
	public static List<ItemStack> applyLootSwaps(@NotNull List<ItemStack> original, ItemStack stack, Random rng){
		List<ItemStack> newDrops = null;
		Pair<List<LootSwapEffect>, Integer> ench = EnchantmentHelper.getEffectListAndLevel(stack, ArcanaEnchantmentComponents.LOOT_SWAP);
		if(ench != null)
			for(LootSwapEffect effect : ench.getFirst()){
				// only allocate new list if loot swaps are actually present
				if(newDrops == null)
					newDrops = new ArrayList<>(original);
				// reverse loop for removal
				for(int i = newDrops.size() - 1; i >= 0; i--)
					processStack(effect, rng, ench.getSecond(), newDrops.remove(i), newDrops::add);
			}
		return newDrops != null ? newDrops : original;
	}
	
	public static Consumer<ItemStack> applyLootSwaps(Consumer<ItemStack> next, ItemStack stack, Random rng){
		Consumer<ItemStack> it = next;
		Pair<List<LootSwapEffect>, Integer> ench = EnchantmentHelper.getEffectListAndLevel(stack, ArcanaEnchantmentComponents.LOOT_SWAP);
		if(ench != null)
			for(LootSwapEffect effect : ench.getFirst()){
				Consumer<ItemStack> prev = it;
				it = item -> processStack(effect, rng, ench.getSecond(), item, prev);
			}
		return it;
	}
	
	public static void processStack(LootSwapEffect enchantment, Random rng, int level, ItemStack in, Consumer<ItemStack> out){
		// TODO: allow custom ones
		RegistryMapping<Item> mapping = enchantment.mapping().equals(arcId("purifying")) ? PURIFYING_MAP :
				enchantment.mapping().equals(arcId("purifying")) ? PURIFYING_MAP :
				null;
		if(mapping == null){
			out.accept(in);
			return;
		}
		Item targetItem = mapping.apply(in.getItem()).orElse(null);
		float chance = level * enchantment.chance.getValue(level);
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
}