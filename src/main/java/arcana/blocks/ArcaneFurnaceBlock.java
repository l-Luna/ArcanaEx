package arcana.blocks;

import arcana.ArcanaRegistry;
import arcana.blocks.be.ArcaneFurnaceBlockEntity;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class ArcaneFurnaceBlock extends BlockWithEntity{
	
	public static final Map<Item, SubstrateData> substrateTimes = new HashMap<>();
	
	public static final DirectionProperty facing = HorizontalFacingBlock.FACING;
	public static final BooleanProperty on = Properties.LIT;
	
	public ArcaneFurnaceBlock(Settings settings){
		super(settings);
		setDefaultState(stateManager.getDefaultState().with(facing, Direction.NORTH).with(on, Boolean.FALSE));
	}
	
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder){
		super.appendProperties(builder);
		builder.add(on, facing);
	}
	
	public BlockState getPlacementState(ItemPlacementContext ctx){
		return getDefaultState().with(facing, ctx.getPlayerFacing().getOpposite());
	}
	
	@Nullable
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state){
		return new ArcaneFurnaceBlockEntity(pos, state);
	}
	
	@Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World _world, BlockState _state, BlockEntityType<T> type){
		return checkType(type, ArcanaRegistry.ARCANE_FURNACE_BE, ArcaneFurnaceBlockEntity::tick);
	}
	
	public BlockRenderType getRenderType(BlockState state){
		return BlockRenderType.MODEL;
	}
	
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit){
		if(world.isClient)
			return ActionResult.SUCCESS;
		else{
			if(world.getBlockEntity(pos) instanceof ArcaneFurnaceBlockEntity furnace){
				player.openHandledScreen(furnace);
				player.incrementStat(Stats.INTERACT_WITH_FURNACE);
			}
			return ActionResult.CONSUME;
		}
	}
	
	public record SubstrateData(int amount, int colour){}
}