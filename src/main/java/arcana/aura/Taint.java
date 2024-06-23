package arcana.aura;

import arcana.ArcanaRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Taint{
	
	// static initialization moment
	public static final class Props{
		
		public static final BooleanProperty STABILIZED = BooleanProperty.of("stabilized");
	}
	
	public static final Map<Block, Block> TAINT_MAP = new HashMap<>(Map.of(
			Blocks.STONE, ArcanaRegistry.TAINTED_ROCK,
			Blocks.DIRT, ArcanaRegistry.TAINTED_SOIL,
			Blocks.GRASS_BLOCK, ArcanaRegistry.TAINTED_GRASS_BLOCK
	));
	
	public static final List<Property<?>> PRESERVE = new ArrayList<>(List.of(
			Properties.SNOWY,
			Properties.WATERLOGGED
	));
	
	public static void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random rng){
		if(state.getProperties().contains(Props.STABILIZED) && state.get(Props.STABILIZED))
			return;
		if(world.isClient)
			return;
		
		AuraChunk localAura = AuraChunk.at(world, pos);
		// if the local flux is great enough to spread taint
		if(localAura.getFlux() > 12){
			// make four attempts to taint a block nearby
			for(int i = 0; i < 4; i++){
				BlockPos target = pos.add(rng.nextBetween(-1, 1), rng.nextBetween(-1, 1), rng.nextBetween(-1, 1));
				BlockState targetState = world.getBlockState(target);
				Block targetBlock = targetState.getBlock();
				if(TAINT_MAP.containsKey(targetBlock)){
					BlockState newState = TAINT_MAP.get(targetBlock).getDefaultState();
					for(Property<?> prop : PRESERVE)
						newState = preserve(newState, targetState, prop);
					world.setBlockState(target, newState);
					localAura.incrementFlux(-2, null);
					localAura.world.sync();
					break;
				}
			}
		}
	}
	
	private static <T extends Comparable<T>> BlockState preserve(BlockState newState, BlockState fromState, Property<T> prop){
		if(newState.getProperties().contains(prop) && fromState.getProperties().contains(prop))
			return newState.with(prop, fromState.get(prop));
		return newState;
	}
}