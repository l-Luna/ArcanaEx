package arcana.worldgen.greatwood;

import net.minecraft.block.sapling.LargeTreeSaplingGenerator;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import org.jetbrains.annotations.Nullable;

import static arcana.Arcana.arcId;

public class GreatwoodSaplingGenerator extends LargeTreeSaplingGenerator{
	
	@Nullable
	protected RegistryEntry<? extends ConfiguredFeature<?, ?>> getLargeTreeFeature(Random random){
		return BuiltinRegistries.CONFIGURED_FEATURE.getEntry(RegistryKey.of(Registry.CONFIGURED_FEATURE_KEY, arcId("scattered_greatwood_tree"))).get();
	}
	
	@Nullable
	protected RegistryEntry<? extends ConfiguredFeature<?, ?>> getTreeFeature(Random random, boolean bees){
		return null;
	}
}