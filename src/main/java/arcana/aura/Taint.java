package arcana.aura;

import arcana.util.RegistryMapping;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

public class Taint{
	
	public static final RegistryMapping<Block>
			TAINT_MAP = new RegistryMapping<>(Registries.BLOCK),
			UNTAINT_MAP = new RegistryMapping<>(Registries.BLOCK);
	
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
	
	public static boolean isBlockProtected(World world, BlockPos pos){
		return AuraWorld.from(world)
				.getNodesInBounds(new Box(pos).expand(8))
				.stream()
				.anyMatch(n -> n.getType() == NodeTypes.PURE && n.asBlockPos().getSquaredDistance(pos) <= 8 * 8);
	}
}