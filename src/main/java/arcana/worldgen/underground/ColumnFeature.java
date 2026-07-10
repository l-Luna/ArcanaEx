package arcana.worldgen.underground;

import arcana.blocks.ConnectingPillarBlock;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.util.FeatureContext;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;

public class ColumnFeature extends Feature<ColumnFeature.Config>{
	
	public record Config(
			BlockStateProvider state,
			IntProvider radius,
			IntProvider height
	) implements FeatureConfig{
		
		public static final Codec<Config> CODEC = RecordCodecBuilder.create(i -> i.group(
				BlockStateProvider.TYPE_CODEC.fieldOf("state_provider").forGetter(Config::state),
				IntProvider.POSITIVE_CODEC.fieldOf("radius").forGetter(Config::radius),
				IntProvider.POSITIVE_CODEC.fieldOf("height").forGetter(Config::height)
		).apply(i, Config::new));
	}
	
	public ColumnFeature(){
		super(Config.CODEC);
	}
	
	public boolean generate(FeatureContext<Config> context){
		Config config = context.getConfig();
		BlockPos origin = context.getOrigin();
		Random rng = context.getRandom();
		StructureWorldAccess world = context.getWorld();
		
		int radius = config.radius.get(rng), height = config.height.get(rng);
		if(!world.getBlockState(origin.up(height)).isAir())
			return false;
		BlockPos c1 = origin.add(-radius, 0, -radius), c2 = origin.add(radius, 0, radius);
		boolean succeeded = false;
		for(BlockPos pos : BlockPos.iterate(c1, c2)){
			int distX = pos.getX() - origin.getX(), distZ = pos.getZ() - origin.getZ();
			if(distX*distX + distZ*distZ <= radius + rng.nextFloat()){
				if(world.getBlockState(pos).isAir())
					continue;
				for(int i = 0; i < height; i++){
					BlockState state = config.state.get(rng, pos);
					// it's not hardcoding, it's pragmatism
					if(state.getProperties().contains(ConnectingPillarBlock.DOWN)){
						state = state.with(ConnectingPillarBlock.DOWN, i > 0);
					}
					if(state.getProperties().contains(ConnectingPillarBlock.UP)){
						state = state.with(ConnectingPillarBlock.UP, i < height - 1);
					}
					succeeded |= world.setBlockState(pos.up(i), state, Block.NOTIFY_ALL);
				}
			}
		}
		return succeeded;
	}
}