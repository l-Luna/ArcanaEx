package arcana.blocks;

import arcana.blocks.be.MagicMirrorBlockEntity;
import arcana.cca_components.MagicMirrorQueue;
import com.google.common.collect.Maps;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class MagicMirrorBlock extends WaterloggableBlock implements BlockEntityProvider{
	
	private static final MapCodec<MagicMirrorBlock> CODEC = createCodec(MagicMirrorBlock::new);
	
	public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
	
	private static final Map<Direction, VoxelShape> BOUNDING_SHAPES = Maps.newEnumMap(
			Map.of(
					Direction.NORTH, Block.createCuboidShape(2, 0, 15, 14, 16, 16),
					Direction.SOUTH, Block.createCuboidShape(2, 0, 0, 14, 16, 1),
					Direction.EAST, Block.createCuboidShape(0, 0, 2, 1, 16, 14),
					Direction.WEST, Block.createCuboidShape(15, 0, 2, 16, 16, 14)
			)
	);
	
	public MagicMirrorBlock(Settings settings){
		super(settings);
		setDefaultState(stateManager.getDefaultState().with(FACING, Direction.NORTH));
	}
	
	protected MapCodec<? extends Block> getCodec(){
		return CODEC;
	}
	
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder){
		super.appendProperties(builder);
		builder.add(FACING);
	}
	
	protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit){
		if(!stack.isEmpty() && world.getBlockEntity(pos) instanceof MagicMirrorBlockEntity mm){
			MagicMirrorQueue.from(world).push(mm.getTag(), mm.getId(), stack);
			player.setStackInHand(hand, ItemStack.EMPTY);
			// TODO: SFX
			return ItemActionResult.SUCCESS;
		}
		return super.onUseWithItem(stack, state, world, pos, player, hand, hit);
	}
	
	public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos){
		Direction dir = state.get(FACING);
		BlockPos supportPos = pos.offset(dir.getOpposite());
		return world.getBlockState(supportPos).isSideSolidFullSquare(world, supportPos, dir);
	}
	
	public BlockState getPlacementState(ItemPlacementContext ctx){
		BlockState state = super.getPlacementState(ctx);
		for(Direction direction : ctx.getPlacementDirections())
			if(direction.getAxis().isHorizontal()){
				state = state.with(FACING, direction);
				if(state.canPlaceAt(ctx.getWorld(), ctx.getBlockPos()))
					return state;
			}
		
		return null;
	}
	
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context){
		return BOUNDING_SHAPES.get(state.get(FACING));
	}
	
	@Nullable
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state){
		return new MagicMirrorBlockEntity(pos, state);
	}
	
	@Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type){
		return (w, p, s, be) -> {
			if(be instanceof MagicMirrorBlockEntity mm)
				mm.tick(w, p, s);
		};
	}
}