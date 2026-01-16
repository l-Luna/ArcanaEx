package arcana.blocks.be;

import arcana.ArcanaRegistry;
import arcana.entities.crimson.CrimsonSpawns;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class CrimsonLanternBlockEntity extends BlockEntity{
	
	public CrimsonLanternBlockEntity(BlockPos pos, BlockState state){
		super(ArcanaRegistry.CRIMSON_LANTERN_BE, pos, state);
	}
	
	public void tick(World world, BlockPos pos){
		// TODO: tie spawned mobs to this lantern
		if(world.isClient){
			clientTick(world, pos);
			return;
		}else if(world.getTime() % (20 * 16) != 0)
			return;
		
		int height;
		for(height = 0; height < 11; height++){
			BlockPos there = pos.down(height);
			if(world.getBlockState(there).isSolidBlock(world, there))
				break;
		}
		if(height == 11)
			return;
		CrimsonSpawns.trySpawn(world, pos, pos.down(height));
	}
	
	private void clientTick(World world, BlockPos pos){
		if(world.getTime() % 8 != 0 || world.getClosestPlayer(pos.getX(), pos.getY(), pos.getZ(), 30, false) == null)
			return;
		
		Random rng = world.getRandom();
		double x = pos.getX() + rng.nextDouble()/2 + .25,
				y = pos.getY() + rng.nextDouble()/2 + .25,
				z = pos.getZ() + rng.nextDouble()/2 + .25;
		world.addParticle(ParticleTypes.SMOKE, x, y, z, 0, 0, 0);
		world.addParticle(ArcanaRegistry.FLAME, x, y, z, 0, 0, 0);
	}
	
}