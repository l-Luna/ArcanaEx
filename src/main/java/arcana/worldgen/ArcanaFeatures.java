package arcana.worldgen;

import arcana.ArcanaTags;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.gen.GenerationStep;

import java.util.List;

import static arcana.Arcana.arcId;

public final class ArcanaFeatures{
	
	public static void addToWorldgen(){
		// overworld features
		
		// nodal geodes
		for(String primal : List.of("air", "fire", "water", "earth", "order", "entropy"))
			BiomeModifications.addFeature(
					BiomeSelectors.foundInOverworld(),
					GenerationStep.Feature.LOCAL_MODIFICATIONS,
					RegistryKey.of(RegistryKeys.PLACED_FEATURE, arcId("geodes/" + primal))
			);
		
		// surface nodes
		BiomeModifications.addFeature(
				BiomeSelectors.foundInOverworld(),
				GenerationStep.Feature.VEGETAL_DECORATION,
				RegistryKey.of(RegistryKeys.PLACED_FEATURE, arcId("surface_node"))
		);
		
		// silverwood & greatwood trees
		BiomeModifications.addFeature(
				x -> x.hasTag(ArcanaTags.SILVERWOOD_SPAWNABLE),
				GenerationStep.Feature.VEGETAL_DECORATION,
				RegistryKey.of(RegistryKeys.PLACED_FEATURE, arcId("scattered_silverwood_tree"))
		);
		BiomeModifications.addFeature(
				x -> x.hasTag(ArcanaTags.GREATWOOD_SPAWNABLE),
				GenerationStep.Feature.VEGETAL_DECORATION,
				RegistryKey.of(RegistryKeys.PLACED_FEATURE, arcId("scattered_greatwood_tree"))
		);
		
		// nether features
		
		// hanging nodes
		BiomeModifications.addFeature(
				BiomeSelectors.foundInTheNether(),
				GenerationStep.Feature.VEGETAL_DECORATION,
				RegistryKey.of(RegistryKeys.PLACED_FEATURE, arcId("hanging_node"))
		);
	}
}