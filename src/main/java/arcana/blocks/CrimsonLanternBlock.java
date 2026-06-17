package arcana.blocks;

import arcana.blocks.be.CrimsonLanternBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.LanternBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class CrimsonLanternBlock extends LanternBlock implements BlockEntityProvider{
	
	private static final MapCodec<LanternBlock> CODEC = createCodec(CrimsonLanternBlock::new);
	
	public CrimsonLanternBlock(Settings settings){
		super(settings);
	}
	
	public MapCodec<LanternBlock> getCodec(){
		return CODEC;
	}
	
	public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state){
		return new CrimsonLanternBlockEntity(pos, state);
	}
	
	public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type){
		return (w, p, s, be) -> {
			if(be instanceof CrimsonLanternBlockEntity cl)
				cl.tick(w, p);
		};
	}
}