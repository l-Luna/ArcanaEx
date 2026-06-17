package arcana.blocks.deco;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

public class CrystalPillarBlock extends Block{
	
	private static final MapCodec<CrystalPillarBlock> CODEC = createCodec(CrystalPillarBlock::new);
	
	private static final VoxelShape SHAPE = Block.createCuboidShape(7, 0, 7, 9, 16, 9);
	
	public CrystalPillarBlock(Settings settings){
		super(settings);
	}
	
	protected MapCodec<? extends Block> getCodec(){
		return CODEC;
	}
	
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context){
		return SHAPE;
	}
}