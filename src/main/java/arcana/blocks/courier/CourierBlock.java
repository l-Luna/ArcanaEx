package arcana.blocks.courier;

import arcana.ArcanaRegistry;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class CourierBlock extends BlockWithEntity{
	
	private static final MapCodec<CourierBlock> CODEC = createCodec(CourierBlock::new);
	
	public CourierBlock(Settings settings){
		super(settings);
	}
	
	protected MapCodec<? extends BlockWithEntity> getCodec(){
		return CODEC;
	}
	
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state){
		return new BE(pos, state);
	}
	
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type){
		return BlockWithEntity.validateTicker(type, ArcanaRegistry.COURIER_BE, (_1, _2, _3, be) -> be.tick());
	}
	
	public static class BE extends BlockEntity{
		
		// linked symbols instances...
		// pending transfers...
		
		public BE(BlockPos pos, BlockState state){
			super(ArcanaRegistry.COURIER_BE, pos, state);
		}
		
		private void tick(){
			List<SymbolInstance> symbols = new ArrayList<>();
			// assume the former is filled... what does the logic look like?
			for(SymbolInstance symbol : symbols){
				long newVersion = symbol.storage.version();
				if(newVersion != symbol.lastVersion){
					symbol.lastVersion = newVersion;
					// scan other {sources,sinks} to check if a new transfer is possible
					// if forceful, scan *all* others, otherwise just scan forceful others
					// ...
					// to check if a transfer from A to B is possible, first attempt to transfer the full amount,
					// if that fails, attempt to pull and transfer the remainder
					// esp with fluids, some sources cannot be split!
				}
			}
		}
	}
}