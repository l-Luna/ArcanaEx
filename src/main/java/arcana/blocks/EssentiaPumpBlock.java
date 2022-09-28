package arcana.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

public class EssentiaPumpBlock extends EssentiaTubeBlock{
	
	public static final DirectionProperty facing = Properties.FACING;
	
	public EssentiaPumpBlock(Settings settings){
		super(settings);
		setDefaultState(getStateManager().getDefaultState().with(facing, Direction.UP));
	}
	
	public @Nullable BlockState getPlacementState(ItemPlacementContext ctx){
		return super.getPlacementState(ctx).with(facing, ctx.getSide());
	}
	
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder){
		super.appendProperties(builder);
		builder.add(facing);
	}
}