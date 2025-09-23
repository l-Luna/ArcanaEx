package arcana.aura;

import arcana.util.SearchUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Taint{
	
	// taint mapping
	
	public static final Map<Block, Block> TAINT_MAP = new HashMap<>();
	public static final List<Pair<TagKey<Block>, Block>> TAINT_TAGS = new ArrayList<>();
	public static final Map<Block, Block> UNTAINT_MAP = new HashMap<>();
	public static final List<Pair<TagKey<Block>, Block>> UNTAINT_TAGS = new ArrayList<>();
	
	private static final List<Property<?>> PRESERVED = new ArrayList<>(List.of(
			Properties.SNOWY,
			Properties.WATERLOGGED
	));
	
	public static void resetMappings(){
		TAINT_MAP.clear();
		TAINT_TAGS.clear();
		UNTAINT_MAP.clear();
		UNTAINT_TAGS.clear();
	}
	
	public static boolean canTfBlock(BlockState state, Map<Block, Block> blockMaps, List<Pair<TagKey<Block>, Block>> blockTags){
		return blockMaps.containsKey(state.getBlock()) || blockTags.stream().anyMatch(x -> state.isIn(x.getLeft()));
	}
	
	public static BlockState tfBlock(BlockState state, Map<Block, Block> blockMaps, List<Pair<TagKey<Block>, Block>> blockTags){
		BlockState transformed = null;
		Block block = state.getBlock();
		if(blockMaps.containsKey(block))
			transformed = blockMaps.get(block).getDefaultState();
		else
			for(Pair<TagKey<Block>, Block> pair : blockTags)
				if(state.isIn(pair.getLeft())){
					transformed = pair.getRight().getDefaultState();
					break;
				}
		
		if(transformed != null){
			for(Property<?> prop : PRESERVED)
				transformed = preserve(transformed, state, prop);
			return transformed;
		}
		return null;
	}
	
	// use a separate method to name the ? as T
	private static <T extends Comparable<T>> BlockState preserve(BlockState newState, BlockState fromState, Property<T> prop){
		if(newState.getProperties().contains(prop) && fromState.getProperties().contains(prop))
			return newState.with(prop, fromState.get(prop));
		return newState;
	}
	
	public static boolean canTaintBlock(BlockState state){
		return canTfBlock(state, TAINT_MAP, TAINT_TAGS);
	}
	
	public static boolean canUntaintBlock(BlockState state){
		return canTfBlock(state, UNTAINT_MAP, UNTAINT_TAGS);
	}
	
	public static BlockState taintBlock(BlockState state){
		return tfBlock(state, TAINT_MAP, TAINT_TAGS);
	}
	
	public static BlockState untaintBlock(BlockState state){
		return tfBlock(state, UNTAINT_MAP, UNTAINT_TAGS);
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
							return canTaintBlock(st);
						},
						(there, st) -> {
							world.setBlockState(there, taintBlock(st));
							localAura.setFlux(localAura.flux() - 2);
						});
		}
	}
}