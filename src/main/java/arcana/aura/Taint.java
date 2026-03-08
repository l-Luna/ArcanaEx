package arcana.aura;

import arcana.util.RegistryMapping;
import arcana.util.SearchUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class Taint{
	
	public static final RegistryMapping<Block>
			TAINT_MAP = new RegistryMapping<>(Registry.BLOCK),
			UNTAINT_MAP = new RegistryMapping<>(Registry.BLOCK);
	
	// use a separate method to name the ? as T
	private static <T extends Comparable<T>> BlockState preserve(BlockState newState, BlockState fromState, Property<T> prop){
		return newState.with(prop, fromState.get(prop));
	}
	
	private static BlockState tfState(BlockState state, RegistryMapping<Block> mapping){
		Block block = state.getBlock();
		BlockState transformed = mapping.apply(block).map(Block::getDefaultState).orElse(null);
		if(transformed != null)
			for(Property<?> prop : state.getProperties())
				if(transformed.getProperties().contains(prop))
					transformed = preserve(transformed, state, prop);
		return transformed;
	}
	
	private static boolean tfSingleBlock(World world, BlockPos pos, RegistryMapping<Block> mapping){
		BlockState tainted = tfState(world.getBlockState(pos), mapping);
		if(tainted != null)
			world.setBlockState(pos, tainted, Block.FORCE_STATE | Block.NOTIFY_LISTENERS);
		return tainted != null;
	}
	
	private static boolean tfBlock(World world, BlockPos pos, RegistryMapping<Block> mapping){
		BlockState block = world.getBlockState(pos);
		boolean result = tfSingleBlock(world, pos, mapping);
		if(result && block.getProperties().contains(Properties.DOUBLE_BLOCK_HALF))
			tfSingleBlock(world, block.get(Properties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER ? pos.up() : pos.down(), mapping);
		return result;
	}
	
	public static boolean taintBlock(World world, BlockPos pos){
		return tfBlock(world, pos, TAINT_MAP);
	}
	
	public static boolean untaintBlock(World world, BlockPos pos){
		return tfBlock(world, pos, UNTAINT_MAP);
	}
	
	// tainted block behaviour
	
	public static final BooleanProperty STABILIZED = BooleanProperty.of("stabilized");
	
	public static void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random rng){
		if(!state.getProperties().contains(STABILIZED) || state.get(STABILIZED))
			return;
		if(world.isClient)
			return;
		
		AuraWorld aura = AuraWorld.from((World)world);
		AuraChunk localAura = AuraChunk.from(world, pos);
		// if the local flux is great enough to spread taint
		if(localAura != null && localAura.flux() > 12){
			// taint 1-3 blocks nearby
			int times = rng.nextBetween(1, 3);
			for(int i = 0; i < times; i++)
				SearchUtil.randomSearch(world, pos, 1, 3,
						(there, st) -> {
							// check if there's no pure nodes in an 8 block radius
							Box box = new Box(pos).expand(8);
							if(aura.getNodesInBounds(box).stream().anyMatch(n -> n.getType() == NodeTypes.PURE && n.asBlockPos().getSquaredDistance(pos) <= 8 * 8))
								return false;
							if(taintBlock(world, there)){
								localAura.setFlux(localAura.flux() - 2);
								return true;
							}
							return false;
						});
		}
	}
}