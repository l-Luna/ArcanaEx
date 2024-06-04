package arcana.blocks;

import arcana.blocks.be.ArcaneFurnaceBlockEntity;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

public class ArcaneFurnaceBlock extends BlockWithEntity{
	
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
	
	public BlockRenderType getRenderType(BlockState state){
		return BlockRenderType.MODEL;
	}
}