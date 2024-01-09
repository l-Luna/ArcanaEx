package arcana.blocks;

import arcana.ArcanaRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WardedCampfireBlock extends CampfireBlock{
	
	public WardedCampfireBlock(Settings settings){
		super(false, 0, settings);
	}
	
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state){
		return new WardedCampfireBlockEntity(pos, state);
	}
	
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type){
		if(world.isClient)
			return state.get(LIT) ? checkType(type, ArcanaRegistry.WARDED_CAMPFIRE_BE, CampfireBlockEntity::clientTick) : null;
		else
			return state.get(LIT)
					? checkType(type, ArcanaRegistry.WARDED_CAMPFIRE_BE, CampfireBlockEntity::litServerTick)
					: checkType(type, ArcanaRegistry.WARDED_CAMPFIRE_BE, CampfireBlockEntity::unlitServerTick);
	}
}