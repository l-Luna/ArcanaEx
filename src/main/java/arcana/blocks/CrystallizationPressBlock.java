package arcana.blocks;

import arcana.ArcanaRegistry;
import arcana.api.AspectIo;
import arcana.aspects.AspectStack;
import arcana.blocks.be.CrystallizationPressBlockEntity;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public class CrystallizationPressBlock extends BlockWithEntity implements AspectIo, InventoryProvider{
	
	public CrystallizationPressBlock(Settings settings){
		super(settings);
		setDefaultState(stateManager.getDefaultState().with(Properties.HORIZONTAL_AXIS, Direction.Axis.X));
	}
	
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder){
		super.appendProperties(builder);
		builder.add(Properties.HORIZONTAL_AXIS);
	}
	
	public BlockState getPlacementState(ItemPlacementContext ctx){
		return getDefaultState().with(Properties.HORIZONTAL_AXIS, ctx.getPlayerFacing().getAxis());
	}
	
	public BlockRenderType getRenderType(BlockState state){
		return BlockRenderType.MODEL;
	}
	
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state){
		return new CrystallizationPressBlockEntity(pos, state);
	}
	
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World _world, BlockState _state, BlockEntityType<T> type){
		return checkType(type, ArcanaRegistry.CRYSTALLIZATION_PRESS_BE, CrystallizationPressBlockEntity::tick);
	}
	
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit){
		if(world.isClient)
			return ActionResult.SUCCESS;
		if(world.getBlockEntity(pos) instanceof CrystallizationPressBlockEntity press){
			player.openHandledScreen(press);
			player.incrementStat(Stats.INTERACT_WITH_ANVIL);
		}
		return ActionResult.CONSUME;
	}
	
	public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved){
		if(!state.isOf(newState.getBlock()))
			if(world.getBlockEntity(pos) instanceof CrystallizationPressBlockEntity be){
				ItemScatterer.spawn(world, pos, be.inventory);
				// TODO: add flux based on stored essentia
			}
		
		super.onStateReplaced(state, world, pos, newState, moved);
	}
	
	public @Nullable AspectStack accept(AspectStack stack, World world, BlockPos pos, Direction from){
		return world.getBlockEntity(pos) instanceof CrystallizationPressBlockEntity be ? be.accept(stack, world, pos, from) : stack;
	}
	
	public @Nullable AspectStack draw(int max, World world, BlockPos pos, Direction from){
		return null; // sorry! no returns
	}
	
	public SidedInventory getInventory(BlockState state, WorldAccess world, BlockPos pos){
		if(world.getBlockEntity(pos) instanceof CrystallizationPressBlockEntity be)
			return be.inventory;
		return null;
	}
}