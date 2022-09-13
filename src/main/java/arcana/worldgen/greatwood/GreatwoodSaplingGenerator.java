package arcana.worldgen.greatwood;

import net.minecraft.block.sapling.LargeTreeSaplingGenerator;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import org.jetbrains.annotations.Nullable;

public class GreatwoodSaplingGenerator extends LargeTreeSaplingGenerator{
	
	@Nullable
	protected RegistryEntry<? extends ConfiguredFeature<?, ?>> getLargeTreeFeature(Random random){
		return RegistryEntry.of(GreatwoodTree.GREATWOOD_TREE);
	}
	
	@Nullable
	protected RegistryEntry<? extends ConfiguredFeature<?, ?>> getTreeFeature(Random random, boolean bees){
		return null;
	}
}