package arcana.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public class InfusionMatrixBlock extends BlockWithEntity{
	
	public InfusionMatrixBlock(Settings settings){
		super(settings);
	}
	
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state){
		return new InfusionMatrixBlockEntity(pos, state);
	}
}
