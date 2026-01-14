package arcana.blocks.be;

import arcana.ArcanaRegistry;
import arcana.entities.crimson.CrimsonSpawns;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.event.GameEvent;

import java.util.Set;

public class CrimsonLanternBlockEntity extends BlockEntity{
	
	public CrimsonLanternBlockEntity(BlockPos pos, BlockState state){
		super(ArcanaRegistry.CRIMSON_LANTERN_BE, pos, state);
	}
	
	public void tick(World world, BlockPos pos, BlockState state){
		// TODO: tie spawned mobs to this lantern
		if(world.isClient){
			clientTick(world, pos);
			return;
		}else if(world.getTime() % (20 * 16) != 0)
			return;
		PlayerEntity player = world.getClosestPlayer(pos.getX(), pos.getY(), pos.getZ(), 30, false);
		if(player == null)
			return;
		
		Box box = new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1).expand(50, 8, 50).offset(0, -4, 0);
		Set<EntityType<?>> spawns = CrimsonSpawns.spawnsFor(player);
		int amnt = 0;
		for(EntityType<?> spawn : spawns)
			amnt += world.getEntitiesByType(spawn, box, __ -> true).size();
		if(amnt > 6)
			return;
		
		Random rng = world.random;
		EntityType<?> type = spawns.toArray(EntityType<?>[]::new)[rng.nextInt(spawns.size())];
		// choosing a spawn location: find the ground under this lantern, add an X/Y offset, then check for solid ground and spawn there
		int height;
		for(height = 0; height < 11; height++){
			BlockPos there = pos.down(height);
			if(world.getBlockState(there).isSolidBlock(world, there))
				break;
		}
		if(height == 11)
			return;
		BlockPos ground = pos.down(height);
		for(int tries = 0; tries < 10; tries++){
			BlockPos adj = ground.add(rng.nextBetween(-6, 6), 0, rng.nextBetween(-6, 6));
			if(world.getBlockState(adj).isSolidBlock(world, adj)
					&& world.getBlockState(adj.up()).isAir()
					&& world.getBlockState(adj.up(2)).isAir()){
				doSpawn(world, type, adj.up());
				break;
			}
		}
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
	
	private static void doSpawn(World world, EntityType<?> type, BlockPos where){
		Entity e = type.create((ServerWorld)world, null, null, null, where, SpawnReason.SPAWNER, true, false);
		world.spawnEntity(e);
		world.syncWorldEvent(WorldEvents.SPAWNER_SPAWNS_MOB, where, 0);
		world.emitGameEvent(e, GameEvent.ENTITY_PLACE, where);
		if(e instanceof MobEntity mob)
			mob.playSpawnEffects();
	}
}