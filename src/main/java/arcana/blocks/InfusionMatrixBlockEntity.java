package arcana.blocks;

import arcana.ArcanaRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public class InfusionMatrixBlockEntity extends BlockEntity{
	
	public InfusionMatrixBlockEntity(BlockPos pos, BlockState state){
		super(ArcanaRegistry.INFUSION_MATRIX_BE, pos, state);
	}
}
