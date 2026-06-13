package arcana.entities.crimson;

import arcana.ArcanaRegistry;
import arcana.legacy_components.Researcher;
import arcana.research.BuiltinResearch;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.event.GameEvent;

import java.util.Set;

public class CrimsonSpawns{

	public static Set<EntityType<?>> spawnsFor(PlayerEntity player){
		Researcher researcher = Researcher.from(player);
		if(researcher.isPuzzleComplete(BuiltinResearch.bossMilestonePuzzle))
			return Set.of(ArcanaRegistry.CRIMSON_MISSIONARY, ArcanaRegistry.CRIMSON_HEAVY_KNIGHT);
		if(researcher.isPuzzleComplete(BuiltinResearch.infusionMilestonePuzzle))
			return Set.of(ArcanaRegistry.CRIMSON_PROTECTOR, ArcanaRegistry.CRIMSON_MISSIONARY);
		if(researcher.isPuzzleComplete(BuiltinResearch.wandMilestonePuzzle))
			return Set.of(ArcanaRegistry.CRIMSON_ARCHER, ArcanaRegistry.CRIMSON_PROTECTOR);
		return Set.of(ArcanaRegistry.CRIMSON_KNIGHT, ArcanaRegistry.CRIMSON_ARCHER);
	}
	
	public static void trySpawn(World world, BlockPos pos, BlockPos spawnPos){
		PlayerEntity player = world.getClosestPlayer(pos.getX(), pos.getY(), pos.getZ(), 30, false);
		if(player == null)
			return;
		
		Box box = new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1).expand(50, 8, 50).offset(0, -4, 0);
		Set<EntityType<?>> spawns = spawnsFor(player);
		int amnt = 0;
		for(EntityType<?> spawn : spawns)
			amnt += world.getEntitiesByType(spawn, box, __ -> true).size();
		if(amnt > 6)
			return;
		
		Random rng = world.random;
		EntityType<?> type = spawns.toArray(EntityType<?>[]::new)[rng.nextInt(spawns.size())];
		
		for(int tries = 0; tries < 10; tries++){
			BlockPos adj = spawnPos.add(rng.nextBetween(-6, 6), 0, rng.nextBetween(-6, 6));
			if(world.getBlockState(adj).isSolidBlock(world, adj)
					&& world.getBlockState(adj.up()).isAir()
					&& world.getBlockState(adj.up(2)).isAir()){
				doSpawn(world, type, adj.up());
				break;
			}
		}
	}
	
	private static void doSpawn(World world, EntityType<?> type, BlockPos where){
		Entity e = type.create((ServerWorld)world, null, where, SpawnReason.SPAWNER, true, false);
		world.spawnEntity(e);
		world.syncWorldEvent(WorldEvents.SPAWNER_SPAWNS_MOB, where, 0);
		world.emitGameEvent(e, GameEvent.ENTITY_PLACE, where);
		if(e instanceof MobEntity mob)
			mob.playSpawnEffects();
	}
}