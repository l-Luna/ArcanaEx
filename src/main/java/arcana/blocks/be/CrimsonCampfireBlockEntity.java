package arcana.blocks.be;

import arcana.ArcanaRegistry;
import arcana.entities.crimson.CrimsonSpawns;
import arcana.mixin.BlockEntityAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CrimsonCampfireBlockEntity extends CampfireBlockEntity{
	
	public CrimsonCampfireBlockEntity(BlockPos pos, BlockState state){
		super(pos, state);
		((BlockEntityAccessor)this).setType(ArcanaRegistry.CRIMSON_CAMPFIRE_BE);
	}
	
	public static void litServerTick(World world, BlockPos pos, BlockState state, CrimsonCampfireBlockEntity campfire){
		CampfireBlockEntity.litServerTick(world, pos, state, campfire);
		if(world.getTime() % (20 * 14) != 0)
			return;
		CrimsonSpawns.trySpawn(world, pos, pos);
	}
}