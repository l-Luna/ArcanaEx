package arcana.worldgen.greatwood;

import arcana.ArcanaTags;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.feature.size.TwoLayersFeatureSize;
import net.minecraft.world.gen.placementmodifier.HeightmapPlacementModifier;
import net.minecraft.world.gen.placementmodifier.SquarePlacementModifier;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;

import java.util.List;

import static arcana.Arcana.arcId;
import static arcana.ArcanaRegistry.GREATWOOD_LEAVES;
import static arcana.ArcanaRegistry.GREATWOOD_LOG;

public class GreatwoodTree{
	
	public static final ConfiguredFeature<TreeFeatureConfig, ?> GREATWOOD_TREE = new ConfiguredFeature<>(
			Feature.TREE,
			new TreeFeatureConfig.Builder(
					BlockStateProvider.of(GREATWOOD_LOG),
					new GreatwoodTrunkPlacer(4, 2, 0),
					BlockStateProvider.of(GREATWOOD_LEAVES),
					new GreatwoodFoliagePlacer(ConstantIntProvider.create(2), ConstantIntProvider.create(0), ConstantIntProvider.create(15)),
					new TwoLayersFeatureSize(1, 0, 1)
			).ignoreVines().build()
	);
	public static final PlacedFeature SCATTERED_GREATWOOD_TREE = new PlacedFeature(
			RegistryEntry.of(GREATWOOD_TREE),
			List.of(
					SquarePlacementModifier.of(),
					PlacedFeatures.createCountExtraModifier(0, 0.1f, 1), // 0.01 chance /chunk
					PlacedFeatures.createCountExtraModifier(0, 0.1f, 1),
					VegetationPlacedFeatures.NOT_IN_SURFACE_WATER_MODIFIER,
					HeightmapPlacementModifier.of(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES)
			)
	);
	
	public static void addToWorldgen(){
		BiomeModifications.addFeature(
				x -> x.hasTag(ArcanaTags.GREATWOOD_SPAWNABLE),
				GenerationStep.Feature.VEGETAL_DECORATION,
				RegistryKey.of(Registry.PLACED_FEATURE_KEY, arcId("greatwood_tree"))
		);
	}
}