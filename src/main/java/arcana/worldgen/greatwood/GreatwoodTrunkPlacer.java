package arcana.worldgen.greatwood;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.TestableWorld;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import net.minecraft.world.gen.foliage.FoliagePlacer;
import net.minecraft.world.gen.trunk.TrunkPlacer;
import net.minecraft.world.gen.trunk.TrunkPlacerType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

public class GreatwoodTrunkPlacer extends TrunkPlacer{
	
	public static final Codec<GreatwoodTrunkPlacer> CODEC = RecordCodecBuilder.create(
			i -> fillTrunkPlacerFields(i).apply(i, GreatwoodTrunkPlacer::new)
	);
	
	public static final TrunkPlacerType<GreatwoodTrunkPlacer> TYPE = new TrunkPlacerType<>(CODEC);
	
	public GreatwoodTrunkPlacer(int baseHeight, int firstRandomHeight, int secondRandomHeight){
		super(baseHeight, firstRandomHeight, secondRandomHeight);
	}
	
	protected TrunkPlacerType<?> getType(){
		return TYPE;
	}
	
	public List<FoliagePlacer.TreeNode> generate(TestableWorld world,
	                                             BiConsumer<BlockPos, BlockState> replacer,
	                                             Random rng,
	                                             int treeHeight,
	                                             BlockPos pos,
	                                             TreeFeatureConfig config){
		// todo: customisation options?
		int height = rng.nextInt(3) + rng.nextInt(3) + /*treeHeight*/ 18;
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();
		if(y >= 1 && y + height + 1 < 255){
			BlockPos ground = pos.down();
			trySetState(world, replacer, rng, ground.mutableCopy(), config);
			trySetState(world, replacer, rng, ground.east().mutableCopy(), config);
			trySetState(world, replacer, rng, ground.south().mutableCopy(), config);
			trySetState(world, replacer, rng, ground.east().south().mutableCopy(), config);
			Set<BlockPos> leafNodes = new HashSet<>();
			// roots
			for(int x1 = -1; x1 <= 2; x1++){
				for(int z1 = -1; z1 <= 2; z1++){
					// Skip root placement if we're in the trunk
					if((x1 == 0 || x1 == 1) && (z1 == 0 || z1 == 1))
						continue;
					
					// Get the root height by nesting random calls to make it biased towards 0
					int rootHeight = rng.nextInt(rng.nextInt(4) + 1);
					
					if(isRootOnEdge(x1) && isRootOnEdge(z1))
						rootHeight--; // Reduce on corners
					
					if(rootHeight > 0){
						BlockPos groundPos = new BlockPos(x + x1, y - 1, z + z1);
						
						if(world.testBlockState(groundPos, AbstractBlock.AbstractBlockState::isAir))
							replacer.accept(groundPos, Blocks.DIRT.getDefaultState());
						
						// Place roots
						for(int curHeight = 0; curHeight < rootHeight; curHeight++)
							trySetState(world, replacer, rng, new BlockPos(x + x1, y + curHeight, z + z1).mutableCopy(), config);
					}
				}
			}
			
			// main trunk
			for(int curHeight = 0; curHeight < height; ++curHeight){
				int curY = y + curHeight;
				BlockPos curPos = new BlockPos(x, curY, z);
				if(canReplace(world, curPos)){
					trySetState(world, replacer, rng, curPos.mutableCopy(), config);
					trySetState(world, replacer, rng, curPos.east().mutableCopy(), config);
					trySetState(world, replacer, rng, curPos.south().mutableCopy(), config);
					trySetState(world, replacer, rng, curPos.east().south().mutableCopy(), config);
				}
				
				// Branches
				if(curHeight > 6 && curHeight < height - 3){
					int branchCount = 1 + (curHeight / 8);
					double offset = Math.PI * 2 * rng.nextDouble();
					
					// Make fewer branches at the bottom, but more at the top
					for(int i = 0; i < branchCount; i++){
						double angle = (((double)i / branchCount) * (Math.PI * 2)) + offset + (rng.nextDouble() * 0.2);
						int length = rng.nextInt(2) + (6 - branchCount) + curHeight / 10;
						// Choose a starting location on the trunk
						BlockPos start = chooseStart(curPos, rng);
						
						for(int j = 0; j <= length; j++){
							// Traverse through the branch
							BlockPos local = start.add(Math.cos(angle) * j, j / 2.0, Math.sin(angle) * j);
							
							// Place logs if it's air
							trySetState(world, replacer, rng, local.mutableCopy(), config);
							
							// If we're at the end, mark this position for generating leaves
							if(j == length)
								leafNodes.add(local);
						}
					}
				}
				
				// Add leaves to the top of the trunk
				if(curHeight == height - 1){
					leafNodes.add(curPos);
					leafNodes.add(curPos.east());
					leafNodes.add(curPos.south());
					leafNodes.add(curPos.east().south());
				}
			}
			List<FoliagePlacer.TreeNode> list = new ArrayList<>();
			for(BlockPos node : leafNodes)
				list.add(new FoliagePlacer.TreeNode(node, 1, false));
			return list;
		}
		return List.of();
	}
	
	private boolean isRootOnEdge(int axis){
		return axis == -1 || axis == 2;
	}
	
	private static BlockPos chooseStart(BlockPos start, Random random){
		return switch(random.nextInt(4)){
			case 0 -> start;
			case 1 -> start.east();
			case 2 -> start.south();
			case 3 -> start.east().south();
			default -> throw new IllegalStateException("how");
		};
	}
}