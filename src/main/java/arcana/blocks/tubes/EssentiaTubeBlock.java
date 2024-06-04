package arcana.blocks.tubes;

import arcana.ArcanaRegistry;
import arcana.aspects.AspectIo;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.ConnectingBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public class EssentiaTubeBlock extends ConnectingBlock implements BlockEntityProvider{
	
	public EssentiaTubeBlock(Settings settings){
		super(.1875f, settings);
		setDefaultState(getStateManager().getDefaultState()
				.with(NORTH, Boolean.FALSE)
				.with(EAST, Boolean.FALSE)
				.with(SOUTH, Boolean.FALSE)
				.with(WEST, Boolean.FALSE)
				.with(UP, Boolean.FALSE)
				.with(DOWN, Boolean.FALSE));
	}
	
	private boolean canConnect(BlockState neighbor){
		return connectsTo(neighbor.getBlock());
	}
	
	public static boolean connectsTo(Block block){
		return block instanceof AspectIo || block instanceof EssentiaTubeBlock;
	}
	
	public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighbor, WorldAccess world, BlockPos pos, BlockPos neighborPos){
		return state.with(FACING_PROPERTIES.get(direction), canConnect(neighbor));
	}
	
	@Nullable
	public BlockState getPlacementState(ItemPlacementContext ctx){
		WorldAccess world = ctx.getWorld();
		BlockPos pos = ctx.getBlockPos();
		return getDefaultState()
				.with(DOWN, canConnect(world.getBlockState(pos.down())))
				.with(UP, canConnect(world.getBlockState(pos.up())))
				.with(NORTH, canConnect(world.getBlockState(pos.north())))
				.with(EAST, canConnect(world.getBlockState(pos.east())))
				.with(SOUTH, canConnect(world.getBlockState(pos.south())))
				.with(WEST, canConnect(world.getBlockState(pos.west())));
	}
	
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder){
		super.appendProperties(builder);
		builder.add(NORTH, SOUTH, EAST, WEST, UP, DOWN);
	}
	
	public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type){
		return false;
	}
	
	@Nullable
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state){
		return new EssentiaTubeBlockEntity(pos, state);
	}
	
	@Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World _world, BlockState _state, BlockEntityType<T> type){
		BlockEntityTicker<EssentiaTubeBlockEntity> ticker = type == ArcanaRegistry.ESSENTIA_TUBE_BE ? EssentiaTubeBlockEntity::tick : null;
		return (BlockEntityTicker<T>)ticker;
	}
}