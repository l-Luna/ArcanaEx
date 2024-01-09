package arcana.blocks;

import arcana.ArcanaRegistry;
import arcana.mixin.BlockEntityAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.util.math.BlockPos;

public class WardedCampfireBlockEntity extends CampfireBlockEntity{
	
	public WardedCampfireBlockEntity(BlockPos pos, BlockState state){
		super(pos, state);
		// muahaha
		((BlockEntityAccessor)this).setType(ArcanaRegistry.WARDED_CAMPFIRE_BE);
	}
}
