package arcana.worldgen;

import arcana.ArcanaRegistry;
import arcana.util.SearchUtil;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

import static arcana.Arcana.arcId;

public class HangingNodeFeature extends Feature<DefaultFeatureConfig>{
	
	public HangingNodeFeature(){
		super(DefaultFeatureConfig.CODEC);
	}
	
	public static void addToWorldgen(){
		BiomeModifications.addFeature(
				BiomeSelectors.foundInTheNether(),
				GenerationStep.Feature.VEGETAL_DECORATION,
				RegistryKey.of(Registry.PLACED_FEATURE_KEY, arcId("hanging_node"))
		);
	}
	
	public boolean generate(FeatureContext<DefaultFeatureConfig> context){
		StructureWorldAccess world = context.getWorld();
		Random rng = context.getRandom();
		BlockPos pos = context.getOrigin();
		// check for blocks 10 below (and slightly above)
		for(int i = -4; i < 12; i++)
			if(!world.isAir(pos.down(i)))
				return false;
		// check for blocks above
		int height;
		for(height = 4; height < 16; height++)
			if(!world.isAir(pos.up(height)))
				break;
		if(height == 16)
			return false; // no ceiling in range
		// place the node itself
		// TODO: replace hungry nodes here with something else special
		SurfaceNodeFeature.spawnRandomNode(rng, world, pos.down(3));
		// place balanced crystal pillars upwards
		for(int y = 0; y < height; y++)
			setBlockState(world, pos.up(y), ArcanaRegistry.BALANCED_CRYSTAL_PILLAR.getDefaultState());
		// then a sphere of balanced crystal
		for(int x = -5; x <= 5; x++)
			for(int y = -5; y <= 5; y++)
				for(int z = -5; z <= 5; z++)
					if(x * x + y * y + z * z < 14)
						setBlockState(world, pos.add(x, y, z), ArcanaRegistry.BALANCED_CRYSTAL.getDefaultState());
		// then add crystals
		int successes = 6 + rng.nextInt(5);
		for(int i = 0; i < successes; i++)
			SearchUtil.randomSearch(world, rng, pos, 6, 20, (where, what) -> SurfaceNodeFeature.tryPutCrystal(world, where, what));
		return false;
	}
	
}