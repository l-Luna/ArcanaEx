package arcana.worldgen.greatwood;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.TestableWorld;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import net.minecraft.world.gen.foliage.FoliagePlacer;
import net.minecraft.world.gen.foliage.FoliagePlacerType;

import java.util.function.BiConsumer;

public class GreatwoodFoliagePlacer extends FoliagePlacer{
	
	public static final Codec<GreatwoodFoliagePlacer> CODEC = RecordCodecBuilder.create(
			i -> fillFoliagePlacerFields(i)
					.and(IntProvider.createValidatingCodec(0, 16).fieldOf("height").forGetter(placer -> placer.height))
					.apply(i, GreatwoodFoliagePlacer::new)
	);
	
	public static final FoliagePlacerType<GreatwoodFoliagePlacer> TYPE = new FoliagePlacerType<>(CODEC);
	
	protected final IntProvider height;
	
	public GreatwoodFoliagePlacer(IntProvider radius, IntProvider offset, IntProvider height){
		super(radius, offset);
		this.height = height;
	}
	
	protected FoliagePlacerType<?> getType(){
		return TYPE;
	}
	
	protected void generate(TestableWorld world,
	                        BiConsumer<BlockPos, BlockState> replacer,
	                        Random rng,
	                        TreeFeatureConfig config,
	                        int trunkHeight,
	                        TreeNode node,
	                        int foliageHeight,
	                        int radius,
	                        int offset){
		BlockPos pos = node.getCenter();
		// Iterate in a spheroid to place leaves
		for(int x1 = -3; x1 <= 3; x1++)
			for(int z1 = -3; z1 <= 3; z1++)
				for(int y1 = -2; y1 <= 2; y1++){
					double rX = x1 / 3.0;
					double rZ = z1 / 3.0;
					double rY = y1 / 2.0;
					// Scale the distance to customize the blob shape
					rX *= 1.1;
					rZ *= 1.1;
					rY *= 0.95;
					double dist = rX * rX + rZ * rZ + rY * rY;
					
					// Apply randomness to the radius and place leaves
					if(dist <= 1 + (rng.nextDouble() * 0.3))
						placeFoliageBlock(world, replacer, rng, config, pos.add(x1, y1, z1));
				}
	}
	
	public int getRandomHeight(Random random, int trunkHeight, TreeFeatureConfig config){
		return 3;
	}
	
	protected boolean isInvalidForLeaves(Random random, int dx, int y, int dz, int radius, boolean giantTrunk){
		return false;
	}
}