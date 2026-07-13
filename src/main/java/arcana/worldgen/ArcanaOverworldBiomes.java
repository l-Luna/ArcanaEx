package arcana.worldgen;

import com.terraformersmc.biolith.api.biome.BiomePlacement;
import com.terraformersmc.biolith.api.biome.sub.CriterionBuilder;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;

import static arcana.Arcana.arcId;

public class ArcanaOverworldBiomes{
	
	public static void setup(){
		BiomePlacement.addSubOverworld(BiomeKeys.OLD_GROWTH_BIRCH_FOREST, arcBiomeKey("magical_forest"), CriterionBuilder.NEAR_INTERIOR);
		
		BiomePlacement.addOverworld(arcBiomeKey("crystal_caverns"), MultiNoiseUtil.createNoiseHypercube(
				MultiNoiseUtil.ParameterRange.of(-1, 1),
				MultiNoiseUtil.ParameterRange.of(-1, 1),
				MultiNoiseUtil.ParameterRange.of(0.3f, 1), // requires some continentalness
				MultiNoiseUtil.ParameterRange.of(-1, 1),
				MultiNoiseUtil.ParameterRange.of(0.3f, 0.9f), // caves depth
				MultiNoiseUtil.ParameterRange.of(0.7f, 1), // requires weirdness
				0
		));
	}
	
	public static RegistryKey<Biome> arcBiomeKey(String path){
		return RegistryKey.of(RegistryKeys.BIOME, arcId(path));
	}
}