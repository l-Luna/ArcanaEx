package arcana.blocks.tubes;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ConnectingBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

public class EssentiaRedirectBlock extends EssentiaTubeBlock{
	
	private static final MapCodec<EssentiaRedirectBlock> CODEC = createCodec(EssentiaRedirectBlock::new);
	
	public static final DirectionProperty FACING = Properties.FACING;
	
	public EssentiaRedirectBlock(Settings settings){
		super(settings);
		setDefaultState(getStateManager().getDefaultState().with(FACING, Direction.UP));
	}
	
	protected MapCodec<? extends ConnectingBlock> getCodec(){
		return CODEC;
	}
	
	public @Nullable BlockState getPlacementState(ItemPlacementContext ctx){
		return super.getPlacementState(ctx).with(FACING, ctx.getSide().getOpposite());
	}
	
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder){
		super.appendProperties(builder);
		builder.add(FACING);
	}
	
	@Nullable
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state){
		return new EssentiaRedirectBlockEntity(pos, state);
	}
}