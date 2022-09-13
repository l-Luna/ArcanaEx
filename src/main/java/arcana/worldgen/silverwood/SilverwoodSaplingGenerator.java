package arcana.worldgen.silverwood;

import net.minecraft.block.sapling.LargeTreeSaplingGenerator;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import org.jetbrains.annotations.Nullable;

public class SilverwoodSaplingGenerator extends LargeTreeSaplingGenerator{
	
	@Nullable
	protected RegistryEntry<? extends ConfiguredFeature<?, ?>> getLargeTreeFeature(Random random){
		return null; // TODO
	}
	
	@Nullable
	protected RegistryEntry<? extends ConfiguredFeature<?, ?>> getTreeFeature(Random random, boolean bees){
		return null;
	}
}