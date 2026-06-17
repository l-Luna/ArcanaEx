package arcana.blocks.tubes;

import arcana.ArcanaRegistry;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ConnectingBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class EssentiaPumpBlock extends EssentiaTubeBlock{
	
	private static final MapCodec<EssentiaPumpBlock> CODEC = createCodec(EssentiaPumpBlock::new);
	
	public static final DirectionProperty FACING = Properties.FACING;
	
	public EssentiaPumpBlock(Settings settings){
		super(settings);
		setDefaultState(getStateManager().getDefaultState().with(FACING, Direction.UP));
	}
	
	protected MapCodec<? extends ConnectingBlock> getCodec(){
		return CODEC;
	}
	
	public @Nullable BlockState getPlacementState(ItemPlacementContext ctx){
		return super.getPlacementState(ctx).with(FACING, ctx.getSide());
	}
	
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder){
		super.appendProperties(builder);
		builder.add(FACING);
	}
	
	@Nullable
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state){
		return new EssentiaPumpBlockEntity(pos, state);
	}
	
	@Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World _world, BlockState _state, BlockEntityType<T> type){
		BlockEntityTicker<EssentiaPumpBlockEntity> ticker = type == ArcanaRegistry.ESSENTIA_PUMP_BE ? EssentiaPumpBlockEntity::tick : null;
		return (BlockEntityTicker<T>)ticker;
	}
}