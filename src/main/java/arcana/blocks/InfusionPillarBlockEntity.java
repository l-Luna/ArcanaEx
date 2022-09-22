package arcana.blocks;

import arcana.ArcanaRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public class InfusionPillarBlockEntity extends BlockEntity{
	
	public InfusionPillarBlockEntity(BlockPos pos, BlockState state){
		super(ArcanaRegistry.INFUSION_PILLAR_BE, pos, state);
	}
}