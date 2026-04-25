package arcana.blocks.be;

import arcana.ArcanaRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class InfusionPillarBlockEntity extends BlockEntity{
	
	private @Nullable BlockPos relativeMatrixPosition = null;
	
	public InfusionPillarBlockEntity(BlockPos pos, BlockState state){
		super(ArcanaRegistry.INFUSION_PILLAR_BE, pos, state);
	}
	
	public @Nullable BlockPos getMatrixPosition(){
		return relativeMatrixPosition != null ? pos.add(relativeMatrixPosition) : null;
	}
	
	public void setMatrixPosition(@Nullable BlockPos matrixPos){
		relativeMatrixPosition = matrixPos != null ? matrixPos.subtract(pos) : null;
	}
}