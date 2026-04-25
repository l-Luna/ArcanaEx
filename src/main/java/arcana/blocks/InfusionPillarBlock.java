package arcana.blocks;

import arcana.blocks.be.InfusionPillarBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public class InfusionPillarBlock extends BlockWithEntity{
	
	public InfusionPillarBlock(Settings settings){
		super(settings);
	}
	
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state){
		return new InfusionPillarBlockEntity(pos, state);
	}
}