package arcana.worldgen.silverwood;

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

public class SilverwoodFoliagePlacer extends FoliagePlacer{
	
	public static final Codec<SilverwoodFoliagePlacer> CODEC = RecordCodecBuilder.create(
			i -> fillFoliagePlacerFields(i)
					.and(IntProvider.createValidatingCodec(0, 16).fieldOf("height").forGetter(placer -> placer.height))
					.apply(i, SilverwoodFoliagePlacer::new)
	);
	
	public static final FoliagePlacerType<SilverwoodFoliagePlacer> TYPE = new FoliagePlacerType<>(CODEC);
	
	protected final IntProvider height;
	
	public SilverwoodFoliagePlacer(IntProvider radius, IntProvider offset, IntProvider height){
		super(radius, offset);
		this.height = height;
	}
	
	protected FoliagePlacerType<?> getType(){
		return TYPE;
	}
	
	protected void generate(TestableWorld world,
	                        BiConsumer<BlockPos, BlockState> replacer,
	                        Random random,
	                        TreeFeatureConfig config,
	                        int trunkHeight,
	                        TreeNode treeNode,
	                        int foliageHeight,
	                        int radius,
	                        int offset){
		BlockPos pos = treeNode.getCenter();
		for(int x1 = -4; x1 <= 4; x1++)
			for(int z1 = -4; z1 <= 4; z1++)
				for(int y1 = -5; y1 <= 5; y1++){
					double rX = x1 / 4.0;
					double rZ = z1 / 4.0;
					double rY = y1 / 5.0;
					double dist = rX * rX + rZ * rZ + rY * rY;
					
					// Apply randomness to the radius and place leaves
					if(dist <= 0.8 + (random.nextDouble() * 0.4))
						placeFoliageBlock(world, replacer, random, config, pos.add(x1, y1, z1));
				}
	}
	
	public int getRandomHeight(Random random, int trunkHeight, TreeFeatureConfig config){
		return 0;
	}
	
	protected boolean isInvalidForLeaves(Random random, int dx, int y, int dz, int radius, boolean giantTrunk){
		return false;
	}
}