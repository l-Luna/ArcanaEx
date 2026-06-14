package arcana.enchantments;

import net.minecraft.component.ComponentType;
import net.minecraft.enchantment.effect.EnchantmentValueEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import java.util.List;

import static arcana.Arcana.arcId;

public final class ArcanaEnchantmentComponents{
	
	public static final ComponentType<DynamicMaxLevelsEffect> DYNAMIC_MAX_LEVELS = ComponentType
			.<DynamicMaxLevelsEffect>builder()
			.codec(DynamicMaxLevelsEffect.CODEC)
			.build();
	
	public static final ComponentType<List<LootSwapEffect>> LOOT_SWAP = ComponentType
			.<List<LootSwapEffect>>builder()
			.codec(LootSwapEffect.CODEC.listOf())
			.build();
	
	// TODO: warp effect provider
	public static final ComponentType<List<EnchantmentValueEffect>> WARPING = ComponentType
			.<List<EnchantmentValueEffect>>builder()
			.codec(EnchantmentValueEffect.CODEC.listOf())
			.build();
	
	public static void setup(){
		register("dynamic_max_levels", DYNAMIC_MAX_LEVELS);
		register("loot_swap", LOOT_SWAP);
		register("warping", WARPING);
	}
	
	private static void register(String id, ComponentType<?> type){
		Registry.register(Registries.ENCHANTMENT_EFFECT_COMPONENT_TYPE, arcId(id), type);
	}
}