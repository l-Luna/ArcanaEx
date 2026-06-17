package arcana.blocks.tubes;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.BlockState;
import net.minecraft.block.ConnectingBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class EssentiaRouterBlock extends EssentiaTubeBlock{
	
	private static final MapCodec<EssentiaRouterBlock> CODEC = createCodec(EssentiaRouterBlock::new);
	
	public EssentiaRouterBlock(Settings settings){
		super(settings);
	}
	
	protected MapCodec<? extends ConnectingBlock> getCodec(){
		return CODEC;
	}
	
	public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state){
		return new EssentiaRouterBlockEntity(pos, state);
	}
}