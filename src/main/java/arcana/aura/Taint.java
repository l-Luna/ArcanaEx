package arcana.aura;

import arcana.ArcanaRegistry;
import arcana.util.SearchUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Taint{
	
	// static initialization moment
	public static final class Props{
		
		public static final BooleanProperty STABILIZED = BooleanProperty.of("stabilized");
	}
	
	public static final Map<Block, Block> TAINT_MAP = new HashMap<>();
	public static final Map<Block, List<Block>> UNTAINT_MAP = new HashMap<>();
	
	public static final List<Property<?>> PRESERVE = new ArrayList<>(List.of(
			Properties.SNOWY,
			Properties.WATERLOGGED
	));
	
	public static void setup(){
		registerTaintMapping(Blocks.STONE, ArcanaRegistry.TAINTED_ROCK);
		registerTaintMapping(Blocks.ANDESITE, ArcanaRegistry.TAINTED_ANDESITE);
		registerTaintMapping(Blocks.GRANITE, ArcanaRegistry.TAINTED_GRANITE);
		registerTaintMapping(Blocks.DIORITE, ArcanaRegistry.TAINTED_DIORITE);
		registerTaintMapping(Blocks.DIRT, ArcanaRegistry.TAINTED_SOIL);
		registerTaintMapping(Blocks.GRASS_BLOCK, ArcanaRegistry.TAINTED_GRASS_BLOCK);
		registerTaintMapping(Blocks.SAND, ArcanaRegistry.TAINTED_SAND);
		registerTaintMapping(Blocks.SANDSTONE, ArcanaRegistry.TAINTED_SANDSTONE);
		registerTaintMapping(Blocks.GRAVEL, ArcanaRegistry.TAINTED_GRAVEL);
		registerTaintMapping(Blocks.SNOW_BLOCK, ArcanaRegistry.TAINTED_SNOW_BLOCK);
	}
	
	public static void registerTaintMapping(Block original, Block tainted){
		TAINT_MAP.put(original, tainted);
		UNTAINT_MAP.computeIfAbsent(tainted, __ -> new ArrayList<>()).add(original);
	}
	
	public static boolean canTaintBlock(BlockState state){
		return TAINT_MAP.containsKey(state.getBlock());
	}
	
	public static BlockState taintBlock(BlockState state){
		if(TAINT_MAP.containsKey(state.getBlock())){
			BlockState tainted = TAINT_MAP.get(state.getBlock()).getDefaultState();
			for(Property<?> prop : PRESERVE)
				tainted = preserve(tainted, state, prop);
			return tainted;
		}
		
		return null;
	}
	
	public static boolean canUntaintBlock(BlockState state){
		return UNTAINT_MAP.containsKey(state.getBlock());
	}
	
	public static BlockState untaintBlock(BlockState state, Random rng){
		if(UNTAINT_MAP.containsKey(state.getBlock())){
			List<Block> choices = UNTAINT_MAP.get(state.getBlock());
			BlockState untainted = choices.get(rng.nextInt(choices.size())).getDefaultState();
			for(Property<?> prop : PRESERVE)
				untainted = preserve(untainted, state, prop);
			return untainted;
		}
		
		return null;
	}
	
	public static void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random rng){
		if(state.getProperties().contains(Props.STABILIZED) && state.get(Props.STABILIZED))
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
	
	private static <T extends Comparable<T>> BlockState preserve(BlockState newState, BlockState fromState, Property<T> prop){
		if(newState.getProperties().contains(prop) && fromState.getProperties().contains(prop))
			return newState.with(prop, fromState.get(prop));
		return newState;
	}
}