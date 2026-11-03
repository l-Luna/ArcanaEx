package arcana.blocks.courier;

import arcana.blocks.courier.symbols.FabricBackedSymbolStorage;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;

public class ControlSymbolBlock extends Block{
	
	public ControlSymbolBlock(Settings settings){
		super(settings);
	}
	
	public static class BE extends BlockEntity{
		
		FabricBackedSymbolStorage.StorageProvider symType;
		BlockBox relativeVolume;
		// linked courier...
		
		public BE(BlockEntityType<?> type, BlockPos pos, BlockState state){
			super(type, pos, state);
		}
	}
}