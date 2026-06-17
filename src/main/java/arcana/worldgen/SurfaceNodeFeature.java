package arcana.worldgen;

import arcana.aspects.Aspect;
import arcana.aspects.AspectMap;
import arcana.aspects.Aspects;
import arcana.aura.AuraWorld;
import arcana.aura.Node;
import arcana.aura.NodeType;
import arcana.aura.NodeTypes;
import arcana.blocks.CrystalClusterBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

import java.util.List;

public class SurfaceNodeFeature extends Feature<DefaultFeatureConfig>{
	
	// TODO: mod config
	private static final int nodeChance = 1;
	private static final int specialNodeChance = 15;
	
	public SurfaceNodeFeature(){
		super(DefaultFeatureConfig.CODEC);
	}
	
	public boolean generate(FeatureContext<DefaultFeatureConfig> context){
		BlockPos pos = context.getOrigin();
		Random rng = context.getRandom();
		StructureWorldAccess world = context.getWorld();
		BlockPos floorPos = world.getTopPosition(Heightmap.Type.OCEAN_FLOOR_WG, pos);
		pos = floorPos.getY() < pos.getY() ? floorPos : pos;
		if(rng.nextInt(200) < nodeChance){
			spawnRandomNode(rng, world, pos);
			// add some crystal clusters
			int successes = 0;
			for(int i = 0; i < 40 && successes < (rng.nextInt(5) + 6); i++){
				BlockPos toSet = pos.add((int)rng.nextTriangular(0, 6), (int)rng.nextTriangular(0, 3), (int)rng.nextTriangular(0, 6));
				if(tryPutCrystal(world, toSet, world.getBlockState(toSet)))
					successes++;
			}
		}
		return true;
	}
	
	public static NodeType randomType(Random rng){
		return from(rng.nextInt(100) < specialNodeChance ? NodeTypes.specialTypes : NodeTypes.normalTypes, rng);
	}
	
	public static void spawnRandomNode(Random rng, StructureWorldAccess world, BlockPos pos){
		NodeType type = randomType(rng);
		AuraWorld aura = AuraWorld.from(world);
		BlockPos nodePos = type != NodeTypes.HUNGRY ? pos.up(5) : pos.up(rng.nextBetween(-2, 2));
		AspectMap cap = type.randomCap(rng);
		Node node = new Node(type, new Vec3d(nodePos.getX() + rng.nextDouble(), nodePos.getY() + rng.nextDouble(), nodePos.getZ() + rng.nextDouble()), cap);
		node.getAspects().add(cap);
		aura.addNode(node);
		if(type == NodeTypes.TAINTED)
			aura.incrementFlux(rng.nextBetween(7, 12), null, nodePos);
	}
	
	public static boolean tryPutCrystal(StructureWorldAccess world, BlockPos toSet, BlockState there){
		if(there.isAir() || there.isReplaceable()){
			Aspect c = from(Aspects.primals, world.getRandom());
			for(Direction direction : Direction.shuffle(world.getRandom())){
				var onPos = toSet.offset(direction.getOpposite());
				BlockState on = world.getBlockState(onPos);
				if(on.isOpaqueFullCube(world, onPos)){
					world.setBlockState(
							toSet,
							Aspects.clusters.get(c).getDefaultState()
									.with(CrystalClusterBlock.FACING, direction)
									.with(CrystalClusterBlock.SIZE, 3)
									.with(CrystalClusterBlock.WATERLOGGED, world.getFluidState(toSet).isIn(FluidTags.WATER)),
							Block.NOTIFY_ALL | Block.FORCE_STATE
					);
					return true;
				}
			}
		}
		return false;
	}
	
	private static <T> T from(List<T> from, Random rng){
		return from.get(rng.nextInt(from.size()));
	}
}