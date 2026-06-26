package arcana.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;

public class ConnectingPillarBlock extends Block{
	
	public static final BooleanProperty UP = Properties.UP, DOWN = Properties.DOWN;
	
	private static final MapCodec<ConnectingPillarBlock> CODEC = createCodec(ConnectingPillarBlock::new);
	
	public ConnectingPillarBlock(Settings settings){
		super(settings);
	}
	
	protected MapCodec<? extends Block> getCodec(){
		return CODEC;
	}
	
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder){
		super.appendProperties(builder);
		builder.add(UP, DOWN);
	}
	
	public BlockState getPlacementState(ItemPlacementContext ctx){
		return checkNeighbors(getDefaultState(), ctx.getWorld(), ctx.getBlockPos());
	}
	
	protected BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos){
		return checkNeighbors(state, world, pos);
	}
	
	private BlockState checkNeighbors(BlockState state,  WorldAccess world, BlockPos pos){
		return state.with(UP, world.getBlockState(pos.up()).isOf(this)).with(DOWN, world.getBlockState(pos.down()).isOf(this));
	}
}