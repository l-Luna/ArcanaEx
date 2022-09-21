package arcana.blocks;

import arcana.ArcanaRegistry;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ArcaneLevitatorBlock extends BlockWithEntity{
	
	public ArcaneLevitatorBlock(Settings settings){
		super(settings);
	}
	
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state){
		return new ArcaneLevitatorBlockEntity(pos, state);
	}
	
	@Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type){
		return checkType(type, ArcanaRegistry.ARCANE_LEVITATOR_BE, ArcaneLevitatorBlockEntity::tick);
	}
	
	public BlockRenderType getRenderType(BlockState state){
		return BlockRenderType.MODEL;
	}
}