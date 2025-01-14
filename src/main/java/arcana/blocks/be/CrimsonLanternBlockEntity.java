package arcana.blocks.be;

import arcana.ArcanaRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CrimsonLanternBlockEntity extends BlockEntity{
	
	public CrimsonLanternBlockEntity(BlockPos pos, BlockState state){
		super(ArcanaRegistry.CRIMSON_LANTERN_BE, pos, state);
	}
	
	public void tick(World world, BlockPos pos, BlockState state){
		// check which enemies are in range
		// decide which enemy set should spawn
		// if not present, find a valid spawn point
		// if possible, spawn them nearby, and set their home location so they don't wander
	}
}