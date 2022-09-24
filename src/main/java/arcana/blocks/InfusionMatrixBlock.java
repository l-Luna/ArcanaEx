package arcana.blocks;

import arcana.ArcanaRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class InfusionMatrixBlock extends BlockWithEntity{
	
	public InfusionMatrixBlock(Settings settings){
		super(settings);
	}
	
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state){
		return new InfusionMatrixBlockEntity(pos, state);
	}
	
	@Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type){
		return checkType(type, ArcanaRegistry.INFUSION_MATRIX_BE, (_1, _2, _3, be) -> be.tick());
	}
	
	public boolean onSyncedBlockEvent(BlockState state, World world, BlockPos pos, int type, int data){
		super.onSyncedBlockEvent(state, world, pos, type, data);
		BlockEntity be = world.getBlockEntity(pos);
		return be != null && be.onSyncedBlockEvent(type, data);
	}
}