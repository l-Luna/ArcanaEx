package arcana.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;

public class ArcaneFurnaceBlock extends Block{
	
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
	
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		return getDefaultState().with(facing, ctx.getPlayerFacing().getOpposite());
	}
}