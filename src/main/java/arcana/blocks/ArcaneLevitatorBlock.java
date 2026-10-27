package arcana.blocks;

import arcana.ArcanaRegistry;
import arcana.blocks.be.ArcaneLevitatorBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ArcaneLevitatorBlock extends BlockWithEntity{
	
	private static final MapCodec<ArcaneLevitatorBlock> CODEC = createCodec(ArcaneLevitatorBlock::new);
	
	public static final DirectionProperty LEVITATOR_FACING = DirectionProperty.of("facing", facing -> facing != Direction.DOWN);
	
	public ArcaneLevitatorBlock(Settings settings){
		super(settings);
		setDefaultState(getStateManager().getDefaultState().with(LEVITATOR_FACING, Direction.UP));
	}
	
	protected MapCodec<? extends BlockWithEntity> getCodec(){
		return CODEC;
	}
	
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder){
		builder.add(LEVITATOR_FACING);
	}
	
	public BlockState getPlacementState(ItemPlacementContext ctx){
		Direction dir = ctx.getPlayerLookDirection().getOpposite();
		if(dir == Direction.DOWN)
			dir = ctx.getHorizontalPlayerFacing();
		return super.getPlacementState(ctx).with(LEVITATOR_FACING, dir);
	}
	
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state){
		return new ArcaneLevitatorBlockEntity(pos, state);
	}
	
	@Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type){
		return validateTicker(type, ArcanaRegistry.ARCANE_LEVITATOR_BE, ArcaneLevitatorBlockEntity::tick);
	}
	
	public BlockRenderType getRenderType(BlockState state){
		return BlockRenderType.MODEL;
	}
}