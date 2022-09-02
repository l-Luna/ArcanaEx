package arcana.blocks;

import arcana.screens.ResearchTableScreenHandler;
import com.unascribed.lib39.weld.api.BigBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Waterloggable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class ResearchTableBlock extends BigBlock implements Waterloggable, BlockEntityProvider{
	
	public static final BooleanProperty left = BooleanProperty.of("left");
	public static final DirectionProperty facing = Properties.HORIZONTAL_FACING;
	public static final BooleanProperty waterlogged = Properties.WATERLOGGED;
	
	public ResearchTableBlock(Settings settings){
		super(null, null, null, settings);
	}
	
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder){
		super.appendProperties(builder);
		builder.add(left, facing, waterlogged);
	}
	
	// BigBlock handles multi-block-ness for us, but we need to handle orientability ourselves
	
	public int getXSize(BlockState state){
		return Math.abs(state.get(facing).getOffsetX()) + 1;
	}
	
	public int getZSize(BlockState state){
		return Math.abs(state.get(facing).getOffsetZ()) + 1;
	}
	
	public int getX(BlockState state){
		var offset = state.get(facing).getOffsetX();
		return (offset < 0 ? 1 : 0) + (state.get(left) ? offset : 0);
	}
	
	public int getZ(BlockState state){
		var offset = state.get(facing).getOffsetZ();
		return (offset < 0 ? 1 : 0) + (state.get(left) ? offset : 0);
	}
	
	public BlockState setX(BlockState state, int z){
		return switch(state.get(facing)){
			case WEST -> state.with(left, z == 0);
			case EAST -> state.with(left, z == 1);
			case UP, DOWN, NORTH, SOUTH -> state;
		};
	}
	
	public BlockState setZ(BlockState state, int z){
		return switch(state.get(facing)){
			case NORTH -> state.with(left, z == 0);
			case SOUTH -> state.with(left, z == 1);
			case UP, DOWN, EAST, WEST -> state;
		};
	}
	
	public FluidState getFluidState(BlockState state){
		return state.get(waterlogged) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
	}
	
	public BlockState getPlacementState(ItemPlacementContext ctx){
		FluidState fluidState = ctx.getWorld().getFluidState(ctx.getBlockPos());
		return super.getPlacementState(ctx)
				.with(waterlogged, fluidState.getFluid() == Fluids.WATER)
				.with(facing, ctx.getPlayerFacing().rotateYCounterclockwise());
	}
	
	@Nullable
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state){
		return state.get(left) ? new ResearchTableBlockEntity(pos, state) : null;
	}
	
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit){
		if(world.isClient)
			return ActionResult.SUCCESS;
		else{
			player.openHandledScreen(state.createScreenHandlerFactory(world, pos));
			return ActionResult.CONSUME;
		}
	}
	
	@Nullable
	public NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos){
		return new SimpleNamedScreenHandlerFactory(
				(syncId, inventory, player) -> new ResearchTableScreenHandler(syncId, inventory, ScreenHandlerContext.create(world, pos)),
				Text.literal("")
		);
	}
}