package arcana.worldgen.silverwood;

import arcana.components.AuraWorld;
import arcana.nodes.Node;
import arcana.nodes.NodeTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.TestableWorld;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import net.minecraft.world.gen.foliage.FoliagePlacer;
import net.minecraft.world.gen.trunk.TrunkPlacer;
import net.minecraft.world.gen.trunk.TrunkPlacerType;

import java.util.List;
import java.util.function.BiConsumer;

public class SilverwoodTrunkPlacer extends TrunkPlacer{
	
	public static final Codec<SilverwoodTrunkPlacer> CODEC = RecordCodecBuilder.create(
			i -> fillTrunkPlacerFields(i).apply(i, SilverwoodTrunkPlacer::new)
	);
	
	public static final TrunkPlacerType<SilverwoodTrunkPlacer> TYPE = new TrunkPlacerType<>(CODEC);
	
	public SilverwoodTrunkPlacer(int baseHeight, int firstRandomHeight, int secondRandomHeight){
		super(baseHeight, firstRandomHeight, secondRandomHeight);
	}
	
	protected TrunkPlacerType<?> getType(){
		return TYPE;
	}
	
	public List<FoliagePlacer.TreeNode> generate(TestableWorld world,
	                                             BiConsumer<BlockPos, BlockState> replacer,
	                                             Random rand,
	                                             int treeHeight,
	                                             BlockPos pos,
	                                             TreeFeatureConfig config){
		int height = rand.nextInt(2) + rand.nextInt(2) + 12;
		int y = pos.getY();
		if(y >= 1 && y + height + 1 < 255){
			for(int x1 = -2; x1 <= 2; x1++)
				for(int z1 = -2; z1 <= 2; z1++){
					// Manhattan distance from center
					int dist = Math.abs(x1) + Math.abs(z1);
					
					// Place in a 2-wide plus formation
					if(dist <= 2){
						// 0 distance: normal height
						int logHeight = height;
						if(dist == 1){
							// plus shape: 70% of height
							logHeight = (int)(height * 0.7) + rand.nextInt(2);
						}else if(dist == 2){
							// 2-wide plus shape: 1 high plus random
							logHeight = 1 + rand.nextInt(2);
						}
						
						// Place the logs
						generateLogColumn(world, rand, pos.add(x1, 0, z1), logHeight, config, replacer);
					}
				}
			
			// add pure node at half height
			AuraWorld aura = AuraWorld.from((StructureWorldAccess)world); // either World or ChunkRegion
			if(rand.nextInt(100) < 20){
				aura.getNodes().add(new Node(NodeTypes.PURE, aura.getWorld(), new Vec3d(pos.getX(), y + height / 2f, pos.getZ())));
				aura.sync();
			}
			
			return List.of(new FoliagePlacer.TreeNode(pos.up(height - 4), 1, false));
		}
		return List.of();
	}
	
	private void generateLogColumn(TestableWorld world, Random random, BlockPos start, int height, TreeFeatureConfig config, BiConsumer<BlockPos, BlockState> replacer){
		for(int y = 0; y < height; y++)
			trySetState(world, replacer, random, start.up(y).mutableCopy(), config);
	}
}