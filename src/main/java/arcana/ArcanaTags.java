package arcana;

import com.mojang.datafixers.util.Pair;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BannerPattern;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.tag.TagKey;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static arcana.Arcana.arcId;

public final class ArcanaTags{
	
	public static final TagKey<Block> CRUCIBLE_HEATING_BLOCKS = TagKey.of(Registry.BLOCK_KEY, arcId("crucible_heating_blocks"));
	public static final TagKey<Fluid> CRUCIBLE_HEATING_FLUIDS = TagKey.of(Registry.FLUID_KEY, arcId("crucible_heating_fluids"));
	
	public static final TagKey<Block> PROJECTED_ARROW_IGNORES = TagKey.of(Registry.BLOCK_KEY, arcId("projected_arrow_ignores"));
	public static final TagKey<Block> HALO_CONVERTIBLE_FLOWERS = TagKey.of(Registry.BLOCK_KEY, arcId("halo_convertible_flowers"));
	public static final TagKey<Block> HALO_CONVERTIBLE_MUSHROOMS = TagKey.of(Registry.BLOCK_KEY, arcId("halo_convertible_mushrooms"));
	public static final TagKey<Block> HALO_CONVERTED = TagKey.of(Registry.BLOCK_KEY, arcId("magical_plants"));
	public static final TagKey<Block> EARTHMOVER_MINEABLE = TagKey.of(Registry.BLOCK_KEY, arcId("earthmover_mineable"));
	
	public static final TagKey<Item> SILVERWOOD_LOGS = TagKey.of(Registry.ITEM_KEY, arcId("silverwood_logs"));
	public static final TagKey<Item> GREATWOOD_LOGS = TagKey.of(Registry.ITEM_KEY, arcId("greatwood_logs"));
	public static final TagKey<Item> TAINTWOOD_LOGS = TagKey.of(Registry.ITEM_KEY, arcId("taintwood_logs"));
	public static final TagKey<Item> HOLLOWED_LOGS = TagKey.of(Registry.ITEM_KEY, arcId("hollowed_logs"));
	public static final TagKey<Item> SUBSTRATES = TagKey.of(Registry.ITEM_KEY, arcId("substrates"));
	public static final TagKey<Item> SCRIBING_TOOLS = TagKey.of(Registry.ITEM_KEY, arcId("scribing_tools"));
	public static final TagKey<Item> FLOATS = TagKey.of(Registry.ITEM_KEY, arcId("floats"));
	
	public static final TagKey<Item> VOID_PUTTY_REPAIR_WHITELIST = TagKey.of(Registry.ITEM_KEY, arcId("config/void_putty_repair_whitelist"));
	public static final TagKey<Item> PLANE_PROJECTION_WHITELIST = TagKey.of(Registry.ITEM_KEY, arcId("config/plane_projection_whitelist"));
	public static final TagKey<Item> WISP_ATTACK_WHITELIST = TagKey.of(Registry.ITEM_KEY, arcId("config/wisp_attack_whitelist"));
	public static final TagKey<Item> CRUCIBLE_REAGENT_BLACKLIST = TagKey.of(Registry.ITEM_KEY, arcId("config/crucible_reagent_blacklist"));
	public static final TagKey<Item> ARCANE_FURNACE_BLACKLIST = TagKey.of(Registry.ITEM_KEY, arcId("config/arcane_furnace_blacklist"));
	
	public static final TagKey<Biome> SILVERWOOD_SPAWNABLE = TagKey.of(Registry.BIOME_KEY, arcId("silverwood_spawnable"));
	public static final TagKey<Biome> GREATWOOD_SPAWNABLE = TagKey.of(Registry.BIOME_KEY, arcId("greatwood_spawnable"));
	
	public static final TagKey<Fluid> TAINT_GOO_FLUID = TagKey.of(Registry.FLUID_KEY, arcId("taint_goo"));
	public static final TagKey<Fluid> PUTREFACTION_FLUID = TagKey.of(Registry.FLUID_KEY, arcId("putrefaction"));
	
	public static final TagKey<BannerPattern> ELDRITCH_BANNER_PATTERNS = TagKey.of(Registry.BANNER_PATTERN_KEY, arcId("pattern_item/eldritch"));
	
	// status effect tags are under mob_effects
	public static final TagKey<StatusEffect> ASPECT_CANDY_EFFECTS = TagKey.of(Registry.MOB_EFFECT_KEY, arcId("aspect_candy_effects"));
	public static final TagKey<StatusEffect> BYPASSES_PRESSURE = TagKey.of(Registry.MOB_EFFECT_KEY, arcId("bypasses_pressure"));
	
	public static List<Item> itemsIn(TagKey<Item> tag){
		return Registry.ITEM.streamTagsAndEntries()
				.filter(x -> x.getFirst().equals(tag))
				.map(Pair::getSecond)
				.map(x -> x.stream().map(RegistryEntry::value).toList())
				.findAny().orElseGet(ArrayList::new);
	}
	
	public static @Nullable Item randomItemIn(TagKey<Item> tag, Random rng){
		return Registry.ITEM.getEntryList(tag).map(entries -> entries.get(rng.nextInt(entries.size())).value()).orElse(null);
	}
	
	public static boolean isOf(StatusEffect effect, TagKey<StatusEffect> tag){
		return isOf(effect, tag, Registry.STATUS_EFFECT);
	}
	
	public static <T> boolean isOf(T value, TagKey<T> tag, Registry<T> registry){
		return registry.getEntry(registry.getRawId(value)).get().isIn(tag);
	}
}