package arcana;

import com.mojang.datafixers.util.Pair;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BannerPattern;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.biome.Biome;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static arcana.Arcana.arcId;

public final class ArcanaTags{
	
	public static final TagKey<Block> CRUCIBLE_HEATING_BLOCKS = TagKey.of(RegistryKeys.BLOCK, arcId("crucible_heating_blocks"));
	public static final TagKey<Fluid> CRUCIBLE_HEATING_FLUIDS = TagKey.of(RegistryKeys.FLUID, arcId("crucible_heating_fluids"));
	
	public static final TagKey<Block> PROJECTED_ARROW_IGNORES = TagKey.of(RegistryKeys.BLOCK, arcId("projected_arrow_ignores"));
	public static final TagKey<Block> HALO_CONVERTIBLE_FLOWERS = TagKey.of(RegistryKeys.BLOCK, arcId("halo_convertible_flowers"));
	public static final TagKey<Block> HALO_CONVERTIBLE_MUSHROOMS = TagKey.of(RegistryKeys.BLOCK, arcId("halo_convertible_mushrooms"));
	public static final TagKey<Block> HALO_CONVERTED = TagKey.of(RegistryKeys.BLOCK, arcId("magical_plants"));
	public static final TagKey<Block> EARTHMOVER_MINEABLE = TagKey.of(RegistryKeys.BLOCK, arcId("earthmover_mineable"));
	
	public static final TagKey<Block> HUGE_MUSHROOM_REPLACEABLES = TagKey.of(RegistryKeys.BLOCK, arcId("worldgen/huge_mushroom_replaceables"));
	
	public static final TagKey<Item> SILVERWOOD_LOGS = TagKey.of(RegistryKeys.ITEM, arcId("silverwood_logs"));
	public static final TagKey<Item> GREATWOOD_LOGS = TagKey.of(RegistryKeys.ITEM, arcId("greatwood_logs"));
	public static final TagKey<Item> TAINTWOOD_LOGS = TagKey.of(RegistryKeys.ITEM, arcId("taintwood_logs"));
	public static final TagKey<Item> HOLLOWED_LOGS = TagKey.of(RegistryKeys.ITEM, arcId("hollowed_logs"));
	public static final TagKey<Item> SUBSTRATES = TagKey.of(RegistryKeys.ITEM, arcId("substrates"));
	public static final TagKey<Item> SCRIBING_TOOLS = TagKey.of(RegistryKeys.ITEM, arcId("scribing_tools"));
	public static final TagKey<Item> FLOATS = TagKey.of(RegistryKeys.ITEM, arcId("floats"));
	
	public static final TagKey<Item> VOID_PUTTY_REPAIR_WHITELIST = TagKey.of(RegistryKeys.ITEM, arcId("config/void_putty_repair_whitelist"));
	public static final TagKey<Item> PLANE_PROJECTION_WHITELIST = TagKey.of(RegistryKeys.ITEM, arcId("config/plane_projection_whitelist"));
	public static final TagKey<Item> WISP_ATTACK_WHITELIST = TagKey.of(RegistryKeys.ITEM, arcId("config/wisp_attack_whitelist"));
	public static final TagKey<Item> CRUCIBLE_REAGENT_BLACKLIST = TagKey.of(RegistryKeys.ITEM, arcId("config/crucible_reagent_blacklist"));
	public static final TagKey<Item> ARCANE_FURNACE_BLACKLIST = TagKey.of(RegistryKeys.ITEM, arcId("config/arcane_furnace_blacklist"));
	
	public static final TagKey<Item> PROJECTING_LEVEL_1 = TagKey.of(RegistryKeys.ITEM, arcId("config/max_projecting_level/1"));
	public static final TagKey<Item> PROJECTING_LEVEL_2 = TagKey.of(RegistryKeys.ITEM, arcId("config/max_projecting_level/2"));
	public static final TagKey<Item> PROJECTING_LEVEL_3 = TagKey.of(RegistryKeys.ITEM, arcId("config/max_projecting_level/3"));
	
	public static final TagKey<Biome> SILVERWOOD_SPAWNABLE = TagKey.of(RegistryKeys.BIOME, arcId("silverwood_spawnable"));
	public static final TagKey<Biome> GREATWOOD_SPAWNABLE = TagKey.of(RegistryKeys.BIOME, arcId("greatwood_spawnable"));
	
	public static final TagKey<Fluid> TAINT_GOO_FLUID = TagKey.of(RegistryKeys.FLUID, arcId("taint_goo"));
	public static final TagKey<Fluid> PUTREFACTION_FLUID = TagKey.of(RegistryKeys.FLUID, arcId("putrefaction"));
	
	public static final TagKey<BannerPattern> ELDRITCH_BANNER_PATTERNS = TagKey.of(RegistryKeys.BANNER_PATTERN, arcId("pattern_item/eldritch"));
	
	// status effect tags are under mob_effects
	public static final TagKey<StatusEffect> ASPECT_CANDY_EFFECTS = TagKey.of(RegistryKeys.STATUS_EFFECT, arcId("aspect_candy_effects"));
	public static final TagKey<StatusEffect> BYPASSES_PRESSURE = TagKey.of(RegistryKeys.STATUS_EFFECT, arcId("bypasses_pressure"));
	
	public static List<Item> itemsIn(TagKey<Item> tag){
		return Registries.ITEM.streamTagsAndEntries()
				.filter(x -> x.getFirst().equals(tag))
				.map(Pair::getSecond)
				.map(x -> x.stream().map(RegistryEntry::value).toList())
				.findAny().orElseGet(ArrayList::new);
	}
	
	public static @Nullable Item randomItemIn(TagKey<Item> tag, Random rng){
		return Registries.ITEM.getEntryList(tag).map(entries -> entries.get(rng.nextInt(entries.size())).value()).orElse(null);
	}
	
	public static boolean isOf(StatusEffect effect, TagKey<StatusEffect> tag){
		return isOf(effect, tag, Registries.STATUS_EFFECT);
	}
	
	public static <T> boolean isOf(T value, TagKey<T> tag, Registry<T> registry){
		return registry.getEntry(registry.getRawId(value)).get().isIn(tag);
	}
}