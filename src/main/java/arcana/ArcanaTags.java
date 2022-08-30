package arcana;

import com.mojang.datafixers.util.Pair;
import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.tag.TagKey;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;

import java.util.ArrayList;
import java.util.List;

import static arcana.Arcana.arcId;

public final class ArcanaTags{
	
	public static final TagKey<Block> CRUCIBLE_HEATING_BLOCKS = TagKey.of(Registry.BLOCK_KEY, arcId("crucible_heating_blocks"));
	public static final TagKey<Fluid> CRUCIBLE_HEATING_FLUIDS = TagKey.of(Registry.FLUID_KEY, arcId("crucible_heating_fluids"));
	
	public static List<Item> itemsIn(TagKey<Item> tag){
		return Registry.ITEM.streamTagsAndEntries()
				.filter(x -> x.getFirst().equals(tag))
				.map(Pair::getSecond)
				.map(x -> x.stream().map(RegistryEntry::value).toList())
				.findAny().orElseGet(ArrayList::new);
	}
}