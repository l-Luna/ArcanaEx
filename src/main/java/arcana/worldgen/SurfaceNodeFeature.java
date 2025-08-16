package arcana.worldgen;

import arcana.aspects.Aspect;
import arcana.aspects.Aspects;
import arcana.aura.AuraWorld;
import arcana.aura.Node;
import arcana.aura.NodeType;
import arcana.aura.NodeTypes;
import arcana.blocks.CrystalClusterBlock;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

import java.util.List;

import static arcana.Arcana.arcId;

public class SurfaceNodeFeature extends Feature<DefaultFeatureConfig>{
	
	// TODO: mod config
	private static final int nodeChance = 1;
	private static final int specialNodeChance = 15;
	
	public SurfaceNodeFeature(){
		super(DefaultFeatureConfig.CODEC);
	}
	
	public static void addToWorldgen(){
		BiomeModifications.addFeature(
				BiomeSelectors.foundInOverworld(),
				GenerationStep.Feature.VEGETAL_DECORATION,
				RegistryKey.of(Registry.PLACED_FEATURE_KEY, arcId("surface_node"))
		);
	}
	
	public boolean generate(FeatureContext<DefaultFeatureConfig> context){
		BlockPos pos = context.getOrigin();
		var rng = context.getRandom();
		StructureWorldAccess world = context.getWorld();
		BlockPos floorPos = world.getTopPosition(Heightmap.Type.OCEAN_FLOOR_WG, pos);
		pos = floorPos.getY() < pos.getY() ? floorPos : pos;
		if(rng.nextInt(200) < nodeChance){
			NodeType type = randomType(rng);
			AuraWorld aura = AuraWorld.from(world);
			BlockPos nodePos = type != NodeTypes.HUNGRY ? pos.up(5) : pos.up(rng.nextBetween(-2, 2));
			// add the node
			aura.addNode(new Node(type, new Vec3d(nodePos.getX() + rng.nextDouble(), nodePos.getY() + rng.nextDouble(), nodePos.getZ() + rng.nextDouble()), type.randomCap(rng)));
			if(type == NodeTypes.TAINTED)
				aura.incrementFlux(rng.nextBetween(7, 12), null, nodePos);
			// add some crystal clusters
			int successes = 0;
			for(int i = 0; i < 40 && successes < (rng.nextInt(5) + 6); i++){
				BlockPos toSet = pos.add(rng.nextTriangular(0, 6), rng.nextTriangular(0, 3), rng.nextTriangular(0, 6));
				var there = world.getBlockState(toSet);
				if(there.isAir() || there.getMaterial().isReplaceable()){
					Aspect c = from(Aspects.primals, world.getRandom());
					for(Direction direction : Direction.shuffle(world.getRandom())){
						var onPos = toSet.offset(direction.getOpposite());
						BlockState on = world.getBlockState(onPos);
						if(on.isOpaqueFullCube(world, onPos)){
							world.setBlockState(
									toSet,
									Aspects.clusters.get(c).getDefaultState()
											.with(CrystalClusterBlock.facing, direction)
											.with(CrystalClusterBlock.size, 3)
											.with(CrystalClusterBlock.waterlogged, world.getFluidState(toSet).isIn(FluidTags.WATER)),
									Block.NOTIFY_ALL | Block.FORCE_STATE
							);
							successes++;
							break;
						}
					}
				}
			}
		}
		return false;
	}
	
	public static NodeType randomType(Random rng){
		return from(rng.nextInt(100) < specialNodeChance ? NodeTypes.specialTypes : NodeTypes.normalTypes, rng);
	}
	
	private static <T> T from(List<T> from, Random rng){
		return from.get(rng.nextInt(from.size()));
	}
}