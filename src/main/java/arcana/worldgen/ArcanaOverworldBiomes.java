package arcana.worldgen;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.biome.Biome;

import static arcana.Arcana.arcId;

public class ArcanaOverworldBiomes{
	
	/*public void onTerraBlenderInitialized(){
		Regions.register(new MagicalBiomesRegion());
	}*/
	
	public static RegistryKey<Biome> arcBiomeKey(String path){
		return RegistryKey.of(RegistryKeys.BIOME, arcId(path));
	}
	
	/*public static class MagicalBiomesRegion extends Region{
		
		public MagicalBiomesRegion(){
			super(arcId("biomes"), RegionType.OVERWORLD, 3);
		}
		
		public void addBiomes(Registry<Biome> registry, Consumer<Pair<MultiNoiseUtil.NoiseHypercube, RegistryKey<Biome>>> mapper){
			addModifiedVanillaOverworldBiomes(mapper, builder -> {
				
				// TODO: custom worldgen location?
				*//*List<MultiNoiseUtil.NoiseHypercube> magicalForestPoints = new ParameterPointListBuilder()
						.temperature(Temperature.WARM, Temperature.COOL)
						.humidity(Humidity.HUMID, Humidity.NEUTRAL)
						.continentalness(Continentalness.INLAND)
						.erosion(Erosion.EROSION_2, Erosion.EROSION_3, Erosion.EROSION_4, Erosion.EROSION_5)
						.depth(Depth.SURFACE, Depth.FLOOR)
						.weirdness(Weirdness.LOW_SLICE_VARIANT_ASCENDING, Weirdness.MID_SLICE_VARIANT_ASCENDING, Weirdness.PEAK_VARIANT, Weirdness.MID_SLICE_VARIANT_DESCENDING)
						.build();
				for(MultiNoiseUtil.NoiseHypercube point : magicalForestPoints)
					builder.replaceBiome(point, arcBiomeKey("magical_forest"));*//*
				builder.replaceBiome(RegistryKey.of(Registry.BIOME_KEY, Identifier.of("birch_forest")), arcBiomeKey("magical_forest"));
				builder.replaceBiome(RegistryKey.of(Registry.BIOME_KEY, Identifier.of("old_growth_birch_forest")), arcBiomeKey("magical_forest"));
			});
		}
	}*/
}