package arcana.worldgen;

import com.terraformersmc.biolith.api.biome.BiomePlacement;
import com.terraformersmc.biolith.api.biome.sub.CriterionBuilder;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;

import static arcana.Arcana.arcId;

public class ArcanaOverworldBiomes{
	
	public static void setup(){
		BiomePlacement.addSubOverworld(BiomeKeys.OLD_GROWTH_BIRCH_FOREST, arcBiomeKey("magical_forest"), CriterionBuilder.NEAR_INTERIOR);
	}
	
	public static RegistryKey<Biome> arcBiomeKey(String path){
		return RegistryKey.of(RegistryKeys.BIOME, arcId(path));
	}
}