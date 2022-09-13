package arcana.worldgen.silverwood;

import arcana.ArcanaTags;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.feature.size.TwoLayersFeatureSize;
import net.minecraft.world.gen.placementmodifier.SquarePlacementModifier;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;

import java.util.List;

import static arcana.Arcana.arcId;
import static arcana.ArcanaRegistry.SILVERWOOD_LEAVES;
import static arcana.ArcanaRegistry.SILVERWOOD_LOG;

public class SilverwoodTree{
	public static final ConfiguredFeature<TreeFeatureConfig, ?> SILVERWOOD_TREE = new ConfiguredFeature<>(
			Feature.TREE,
			new TreeFeatureConfig.Builder(
					BlockStateProvider.of(SILVERWOOD_LOG),
					new SilverwoodTrunkPlacer(4, 2, 0),
					BlockStateProvider.of(SILVERWOOD_LEAVES),
					new SilverwoodFoliagePlacer(ConstantIntProvider.create(2), ConstantIntProvider.create(0), ConstantIntProvider.create(15)),
					new TwoLayersFeatureSize(1, 0, 1)
			).ignoreVines().build()
	);
	public static final PlacedFeature SCATTERED_SILVERWOOD_TREE = new PlacedFeature(
			RegistryEntry.of(SILVERWOOD_TREE),
			List.of(
					SquarePlacementModifier.of(),
					PlacedFeatures.createCountExtraModifier(0, 0.1f, 1), // 0.004 chance /chunk
					PlacedFeatures.createCountExtraModifier(0, 0.2f, 1),
					PlacedFeatures.createCountExtraModifier(0, 0.2f, 1),
					VegetationPlacedFeatures.NOT_IN_SURFACE_WATER_MODIFIER,
					PlacedFeatures.OCEAN_FLOOR_HEIGHTMAP
			)
	);
	
	public static void addToWorldgen(){
		BiomeModifications.addFeature(
				/*BiomeSelectors.tag(ArcanaTags.SILVERWOOD_SPAWNABLE)*/ x -> x.hasTag(ArcanaTags.SILVERWOOD_SPAWNABLE),
				GenerationStep.Feature.VEGETAL_DECORATION,
				RegistryKey.of(Registry.PLACED_FEATURE_KEY, arcId("silverwood_tree"))
		);
	}
}