package arcana.worldgen;

import arcana.aspects.Aspect;
import arcana.aspects.Aspects;
import arcana.blocks.CrystalClusterBlock;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.block.Blocks;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.YOffset;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.placementmodifier.HeightRangePlacementModifier;
import net.minecraft.world.gen.placementmodifier.RarityFilterPlacementModifier;
import net.minecraft.world.gen.placementmodifier.SquarePlacementModifier;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import net.minecraft.world.gen.stateprovider.NoiseBlockStateProvider;

import java.util.List;

import static arcana.Arcana.arcId;

public class NodalGeodes{
	
	public static void addToWorldgen(){
		for(String primal : List.of("air", "fire", "water", "earth", "order", "entropy"))
			BiomeModifications.addFeature(
					BiomeSelectors.foundInOverworld(),
					GenerationStep.Feature.LOCAL_MODIFICATIONS,
					RegistryKey.of(Registry.PLACED_FEATURE_KEY, arcId(primal + "_geode"))
			);
	}
	
	public static final NodalGeodeFeature NODAL_GEODE_FEATURE = new NodalGeodeFeature();
	
	public static final ConfiguredFeature<NodalGeodeFeatureConfig, ?> AIR_GEODE = aspectGeode(
			Aspects.AIR,
			BlockStateProvider.of(Blocks.SMOOTH_QUARTZ),
			BlockStateProvider.of(Blocks.SMOOTH_SANDSTONE),
			BlockStateProvider.of(Blocks.SMOOTH_RED_SANDSTONE),
			BlockStateProvider.of(Blocks.GLOWSTONE)
	);
	
	public static final ConfiguredFeature<NodalGeodeFeatureConfig, ?> FIRE_GEODE = aspectGeode(
			Aspects.FIRE,
			BlockStateProvider.of(Blocks.BASALT),
			BlockStateProvider.of(Blocks.BLACKSTONE),
			BlockStateProvider.of(Blocks.MAGMA_BLOCK),
			BlockStateProvider.of(Blocks.OBSIDIAN)
	);
	
	public static final ConfiguredFeature<NodalGeodeFeatureConfig, ?> WATER_GEODE = aspectGeode(
			Aspects.WATER,
			BlockStateProvider.of(Blocks.MUD),
			BlockStateProvider.of(Blocks.PRISMARINE),
			BlockStateProvider.of(Blocks.PACKED_ICE),
			BlockStateProvider.of(Blocks.BLUE_ICE)
	);
	
	public static final ConfiguredFeature<NodalGeodeFeatureConfig, ?> EARTH_GEODE = aspectGeode(
			Aspects.EARTH,
			BlockStateProvider.of(Blocks.TUFF),
			BlockStateProvider.of(Blocks.BLACKSTONE),
			BlockStateProvider.of(Blocks.SMOOTH_BASALT),
			BlockStateProvider.of(Blocks.OBSIDIAN)
	);
	
	public static final ConfiguredFeature<NodalGeodeFeatureConfig, ?> ORDER_GEODE = aspectGeode(
			Aspects.ORDER,
			BlockStateProvider.of(Blocks.DEEPSLATE_TILES),
			BlockStateProvider.of(Blocks.POLISHED_DEEPSLATE),
			BlockStateProvider.of(Blocks.SMOOTH_QUARTZ),
			BlockStateProvider.of(Blocks.CHISELED_QUARTZ_BLOCK)
	);
	
	public static final ConfiguredFeature<NodalGeodeFeatureConfig, ?> ENTROPY_GEODE = aspectGeode(
			Aspects.ENTROPY,
			BlockStateProvider.of(Blocks.TERRACOTTA),
			new NoiseBlockStateProvider(0, new DoublePerlinNoiseSampler.NoiseParameters(1, 2, 3), 1, List.of(
					Blocks.COBBLESTONE.getDefaultState(),
					Blocks.MOSSY_COBBLESTONE.getDefaultState()
			)),
			new NoiseBlockStateProvider(0, new DoublePerlinNoiseSampler.NoiseParameters(1, 2, 3), 1, List.of(
					Blocks.ANDESITE.getDefaultState(),
					Blocks.GRANITE.getDefaultState(),
					Blocks.DIORITE.getDefaultState()
			)),
			new NoiseBlockStateProvider(0, new DoublePerlinNoiseSampler.NoiseParameters(1, 2, 3), 1, List.of(
					Blocks.ANDESITE.getDefaultState(),
					Blocks.GRANITE.getDefaultState(),
					Blocks.DIORITE.getDefaultState()
			))
	);
	
	public static PlacedFeature PLACED_AIR_GEODE = new PlacedFeature(
			RegistryEntry.of(AIR_GEODE),
			List.of(
					RarityFilterPlacementModifier.of(24 * 6),
					SquarePlacementModifier.of(),
					HeightRangePlacementModifier.uniform(YOffset.aboveBottom(6), YOffset.fixed(30))
					//BiomePlacementModifier.of()
			)
	);
	
	public static PlacedFeature PLACED_FIRE_GEODE = new PlacedFeature(
			RegistryEntry.of(FIRE_GEODE),
			List.of(
					RarityFilterPlacementModifier.of(24 * 6),
					SquarePlacementModifier.of(),
					HeightRangePlacementModifier.uniform(YOffset.aboveBottom(6), YOffset.fixed(30))
					//BiomePlacementModifier.of()
			)
	);
	
	public static PlacedFeature PLACED_WATER_GEODE = new PlacedFeature(
			RegistryEntry.of(WATER_GEODE),
			List.of(
					RarityFilterPlacementModifier.of(24 * 6),
					SquarePlacementModifier.of(),
					HeightRangePlacementModifier.uniform(YOffset.aboveBottom(6), YOffset.fixed(30))
					//BiomePlacementModifier.of()
			)
	);
	
	public static PlacedFeature PLACED_EARTH_GEODE = new PlacedFeature(
			RegistryEntry.of(EARTH_GEODE),
			List.of(
					RarityFilterPlacementModifier.of(24 * 6),
					SquarePlacementModifier.of(),
					HeightRangePlacementModifier.uniform(YOffset.aboveBottom(6), YOffset.fixed(30))
					//BiomePlacementModifier.of()
			)
	);
	
	public static PlacedFeature PLACED_ORDER_GEODE = new PlacedFeature(
			RegistryEntry.of(ORDER_GEODE),
			List.of(
					RarityFilterPlacementModifier.of(24 * 6),
					SquarePlacementModifier.of(),
					HeightRangePlacementModifier.uniform(YOffset.aboveBottom(6), YOffset.fixed(30))
					//BiomePlacementModifier.of()
			)
	);
	
	public static PlacedFeature PLACED_ENTROPY_GEODE = new PlacedFeature(
			RegistryEntry.of(ENTROPY_GEODE),
			List.of(
					RarityFilterPlacementModifier.of(24 * 6),
					SquarePlacementModifier.of(),
					HeightRangePlacementModifier.uniform(YOffset.aboveBottom(6), YOffset.fixed(30))
					//BiomePlacementModifier.of()
			)
	);
	
	private static ConfiguredFeature<NodalGeodeFeatureConfig, ?> aspectGeode(
			Aspect aspect,
			BlockStateProvider outer,
			BlockStateProvider middle,
			BlockStateProvider inner,
			BlockStateProvider innerAlt
	){
		return new ConfiguredFeature<>(
				NODAL_GEODE_FEATURE,
				new NodalGeodeFeatureConfig(
						new GeodeFeatureConfig(
								new GeodeLayerConfig(
										BlockStateProvider.of(Blocks.AIR),
										inner,
										innerAlt,
										middle,
										outer,
										List.of(
												Aspects.clusters.get(aspect).getDefaultState().with(CrystalClusterBlock.size, 0),
												Aspects.clusters.get(aspect).getDefaultState().with(CrystalClusterBlock.size, 1),
												Aspects.clusters.get(aspect).getDefaultState().with(CrystalClusterBlock.size, 2),
												Aspects.clusters.get(aspect).getDefaultState().with(CrystalClusterBlock.size, 3)
										),
										BlockTags.FEATURES_CANNOT_REPLACE,
										BlockTags.GEODE_INVALID_BLOCKS
								),
								new GeodeLayerThicknessConfig(1.7, 2.2, 3.2, 4.2),
								new GeodeCrackConfig(0.75, 3.0, 2),
								0.33,
								0.3,
								true,
								UniformIntProvider.create(4, 6),
								UniformIntProvider.create(3, 4),
								UniformIntProvider.create(1, 2),
								-16,
								16,
								0.05,
								1
						),
						aspect));
	}
}