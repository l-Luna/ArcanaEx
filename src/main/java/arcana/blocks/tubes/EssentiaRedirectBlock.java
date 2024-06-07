package arcana.blocks.tubes;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

public class EssentiaRedirectBlock extends EssentiaTubeBlock{
	
	public static final DirectionProperty facing = Properties.FACING;
	
	public EssentiaRedirectBlock(Settings settings){
		super(settings);
		setDefaultState(getStateManager().getDefaultState().with(facing, Direction.UP));
	}
	
	public @Nullable BlockState getPlacementState(ItemPlacementContext ctx){
		return super.getPlacementState(ctx).with(facing, ctx.getSide().getOpposite());
	}
	
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder){
		super.appendProperties(builder);
		builder.add(facing);
	}
	
	@Nullable
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state){
		return new EssentiaRedirectBlockEntity(pos, state);
	}
}