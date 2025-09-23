package arcana.blocks.tainted;

import arcana.aura.Taint;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

/**
 * Marker interface for tainted block types.
 */
public interface Tainted{
	
	BooleanProperty STABILIZED = Taint.STABILIZED;
	
	default void taintTick(BlockState state, ServerWorld world, BlockPos pos, Random rng){
		Taint.randomTick(state, world, pos, rng);
	}
	
	default boolean hasTaintTicks(BlockState state){
		return !(state.getProperties().contains(STABILIZED) && state.get(STABILIZED));
	}
}