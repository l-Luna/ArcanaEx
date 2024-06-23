package arcana.blocks.tainted;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FallingBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

public class TaintedFallingBlock extends FallingBlock implements Tainted{
	
	public TaintedFallingBlock(Settings settings){
		super(settings);
		setDefaultState(getStateManager().getDefaultState().with(STABILIZED, false));
	}
	
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder){
		super.appendProperties(builder);
		builder.add(STABILIZED);
	}
	
	public boolean hasRandomTicks(BlockState state){
		return hasTaintTicks(state);
	}
	
	public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random){
		taintTick(state, world, pos, random);
	}
}