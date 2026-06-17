package arcana.blocks;

import arcana.blocks.be.InfusionPillarBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public class InfusionPillarBlock extends BlockWithEntity{
	
	private static final MapCodec<InfusionPillarBlock> CODEC = createCodec(InfusionPillarBlock::new);
	
	public InfusionPillarBlock(Settings settings){
		super(settings);
	}
	
	protected MapCodec<? extends BlockWithEntity> getCodec(){
		return CODEC;
	}
	
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state){
		return new InfusionPillarBlockEntity(pos, state);
	}
}