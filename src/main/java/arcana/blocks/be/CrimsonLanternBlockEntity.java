package arcana.blocks.be;

import arcana.ArcanaRegistry;
import arcana.entities.crimson.CrimsonSpawns;
import arcana.util.SearchUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.Set;

public class CrimsonLanternBlockEntity extends BlockEntity{
	
	public CrimsonLanternBlockEntity(BlockPos pos, BlockState state){
		super(ArcanaRegistry.CRIMSON_LANTERN_BE, pos, state);
	}
	
	public void tick(World world, BlockPos pos, BlockState state){
		// TODO: tie spawned mobs to this lantern
		if(world.isClient || world.getTime() % 20 * 16 != 0)
			return;
		PlayerEntity player = world.getClosestPlayer(pos.getX(), pos.getY(), pos.getZ(), 40, false);
		if(player == null)
			return;
		
		Box box = new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1).expand(10, 6, 10).offset(0, -4, 0);
		Set<EntityType<?>> spawns = CrimsonSpawns.spawnsFor(player);
		int amnt = 0;
		for(EntityType<?> spawn : spawns)
			amnt += world.getEntitiesByType(spawn, box, __ -> true).size();
		if(amnt > 6)
			return;
		
		EntityType<?> type = spawns.toArray(EntityType<?>[]::new)[world.random.nextInt(spawns.size())];
		SearchUtil.vRandomSearch(world, pos, 8, 8, 6, (where, what) -> {
			if(!what.isSolidBlock(world, where))
				return false;
			if(!world.isAir(where.up()) || !world.isAir(where.up(2)))
				return false;
			Entity e = type.create((ServerWorld)world, null, null, null, where.up(), SpawnReason.SPAWNER, true, false);
			world.spawnEntity(e);
			return true;
		});
	}
}