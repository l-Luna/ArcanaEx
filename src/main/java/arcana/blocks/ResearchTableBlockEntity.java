package arcana.blocks;

import arcana.ArcanaRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public class ResearchTableBlockEntity extends BlockEntity{
	
	public ResearchTableBlockEntity(BlockPos pos, BlockState state){
		super(ArcanaRegistry.RESEARCH_TABLE_BE, pos, state);
	}
}