package arcana.worldgen.silverwood;

import arcana.ArcanaRegistry;
import net.minecraft.block.sapling.LargeTreeSaplingGenerator;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import net.minecraft.world.gen.feature.size.TwoLayersFeatureSize;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import org.jetbrains.annotations.Nullable;

public class SilverwoodTree extends LargeTreeSaplingGenerator{
	
	public static final ConfiguredFeature<TreeFeatureConfig, ?> SILVERWOOD_TREE = new ConfiguredFeature<>(
			Feature.TREE,
			new TreeFeatureConfig.Builder(
					BlockStateProvider.of(ArcanaRegistry.SILVERWOOD_LOG),
					new SilverwoodTrunkPlacer(4, 2, 0),
					BlockStateProvider.of(ArcanaRegistry.SILVERWOOD_LEAVES),
					new SilverwoodFoliagePlacer(ConstantIntProvider.create(2), ConstantIntProvider.create(0), ConstantIntProvider.create(15)),
					new TwoLayersFeatureSize(1, 0, 1)
			).ignoreVines().build()
	);
	
	@Nullable
	protected RegistryEntry<? extends ConfiguredFeature<?, ?>> getLargeTreeFeature(Random random){
		return RegistryEntry.of(SILVERWOOD_TREE);
	}
	
	@Nullable
	protected RegistryEntry<? extends ConfiguredFeature<?, ?>> getTreeFeature(Random random, boolean bees){
		return null;
	}
}