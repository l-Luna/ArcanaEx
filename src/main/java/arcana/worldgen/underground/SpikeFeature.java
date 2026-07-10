package arcana.worldgen.underground;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.util.FeatureContext;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;

// based on the vanilla IceSpikeFeature, but un-hardcoded
public class SpikeFeature extends Feature<SpikeFeature.Config>{
	
	public record Config(
			BlockStateProvider state,
			IntProvider height,
			RegistryEntryList<Block> replaceable
	) implements FeatureConfig{
	
		public static final Codec<Config> CODEC = RecordCodecBuilder.create(i -> i.group(
				BlockStateProvider.TYPE_CODEC.fieldOf("state_provider").forGetter(Config::state),
				IntProvider.POSITIVE_CODEC.fieldOf("height").forGetter(Config::height),
				RegistryCodecs.entryList(RegistryKeys.BLOCK).fieldOf("replaceable").forGetter(Config::replaceable)
		).apply(i, Config::new));
	}
	
	public SpikeFeature(){
		super(Config.CODEC);
	}
	
	public boolean generate(FeatureContext<Config> context){
		BlockPos origin = context.getOrigin();
		Random rng = context.getRandom();
		StructureWorldAccess world = context.getWorld();
		Config config = context.getConfig();
		
		while(world.isAir(origin) && origin.getY() > world.getBottomY() + 2)
			origin = origin.down();
		
		if(!(world.getBlockState(origin).isIn(config.replaceable()) && world.getBlockState(origin.up()).isAir()))
			return false;
		else{
			origin = origin.up(rng.nextInt(4));
			int height = config.height.get(rng);
			int absRad = height / 4 + rng.nextInt(2);
			if(absRad > 1 && rng.nextInt(60) == 0)
				origin = origin.up(10 + rng.nextInt(30));
			
			for(int y = 0; y < height; y++){
				float rad = (1 - (float)y / height) * absRad;
				int radI = MathHelper.ceil(rad);
				
				for(int xOff = -radI; xOff <= radI; xOff++){
					float xl = MathHelper.abs(xOff) - 0.25f;
					
					for(int zOff = -radI; zOff <= radI; zOff++){
						float zl = MathHelper.abs(zOff) - 0.25f;
						if((xOff == 0 && zOff == 0 || xl*xl + zl*zl <= rad*rad) && (xOff != -radI && xOff != radI && zOff != -radI && zOff != radI || !(rng.nextFloat() > 0.75f))){
							BlockPos posA = origin.add(xOff, y, zOff);
							BlockState there = world.getBlockState(posA);
							if(there.isAir() || isSoil(there) || there.isIn(config.replaceable()))
								setBlockState(world, posA, config.state().get(rng, posA));
							
							if(y != 0 && radI > 1){
								BlockPos posB = origin.add(xOff, -y, zOff);
								there = world.getBlockState(posB);
								if(there.isAir() || isSoil(there) || there.isIn(config.replaceable()))
									setBlockState(world, posB, config.state().get(rng, posB));
							}
						}
					}
				}
			}
			
			int baseRad = absRad - 1;
			if(baseRad < 0)
				baseRad = 0;
			else if(baseRad > 1)
				baseRad = 1;
			
			for(int x = -baseRad; x <= baseRad; x++)
				for(int z = -baseRad; z <= baseRad; z++){
					BlockPos pos = origin.add(x, -1, z);
					int p = 50;
					if(Math.abs(x) == 1 && Math.abs(z) == 1)
						p = rng.nextInt(5);
					
					for(int depth = 0; depth < 10; depth++){
						BlockState there = world.getBlockState(pos);
						if(!there.isAir()
								&& !isSoil(there)
								&& !there.isIn(config.replaceable()))
							break;
						
						setBlockState(world, pos, config.state().get(rng, pos));
						pos = pos.down();
						if(--p <= 0){
							pos = pos.down(rng.nextInt(5) + 1);
							p = rng.nextInt(5);
						}
					}
				}
			
			return true;
		}
	}
}