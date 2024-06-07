package arcana.blocks.tubes;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class EssentiaRouterBlock extends EssentiaTubeBlock{
	
	public EssentiaRouterBlock(Settings settings){
		super(settings);
	}
	
	public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state){
		return new EssentiaRouterBlockEntity(pos, state);
	}
}