package arcana.aura;

import arcana.util.NbtUtil;
import arcana.util.StreamUtil;
import com.mojang.logging.LogUtils;
import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import it.unimi.dsi.fastutil.longs.Long2FloatMap;
import it.unimi.dsi.fastutil.longs.Long2FloatOpenHashMap;
import net.fabricmc.fabric.impl.event.lifecycle.LoadedChunksCache;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

import static arcana.Arcana.arcId;

public final class AuraWorld implements Component, ServerTickingComponent, AutoSyncedComponent{
	
	private static final Logger logger = LogUtils.getLogger();
	
	public static final ComponentKey<AuraWorld> KEY = ComponentRegistryV3.INSTANCE.getOrCreate(arcId("aura_world"), AuraWorld.class);
	
	private final World world;
	
	private final Map<FluxOrigin, Float> globalFluxStats = new EnumMap<>(FluxOrigin.class);
	private List<Node> pendingNodes = new ArrayList<>();
	
	public AuraWorld(World world){
		this.world = world;
	}
	
	// getters
	
	public World world(){
		return world;
	}
	
	public Map<FluxOrigin, Float> globalFluxStats(){
		return globalFluxStats;
	}
	
	// accessors
	
	public static AuraWorld from(World world){
		return world.getComponent(KEY);
	}
	
	public static AuraWorld from(StructureWorldAccess swa){
		if(swa instanceof ServerWorld sw)
			return from((World)sw);
		else if(swa instanceof ChunkRegion cr)
			return from((World)cr.toServerWorld());
		throw new IncompatibleClassChangeError();
	}
	
	// API
	
	public void incrementFlux(float amount, @Nullable FluxOrigin type, BlockPos where){
		AuraChunk there = AuraChunk.from(world, where);
		there.setFlux(there.flux() + amount);
		if(type != null){
			globalFluxStats.merge(type, amount, Float::sum);
			sync();
		}
	}
	
	public void addNode(Node node){
		AuraChunk there = AuraChunk.from(world, node.asBlockPos());
		if(there != null)
			there.addNode(node);
		else{
			logger.info("pendingNodes added");
			pendingNodes.add(node);
			sync();
		}
	}
	
	public List<Node> getNodesInBounds(Box bounds){
		return AuraChunk.chunksCovering(world, bounds).stream()
				.filter(Objects::nonNull)
				.flatMap(chunk -> chunk.nodes().stream())
				.filter(node -> bounds.contains(node.asVec3d()))
				.collect(Collectors.toList());
	}
	
	public Optional<Node> raycastNodes(Position fromPos, double length, boolean ignoreBlocks, Entity viewer){
		Vec3d from = new Vec3d(fromPos.getX(), fromPos.getY(), fromPos.getZ());
		Vec3d to = from.add(viewer.getRotationVector().multiply(length));
		BlockHitResult bhr = null;
		if(!ignoreBlocks)
			bhr = viewer.world.raycast(new RaycastContext(from, to, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, viewer));
		Box bounds = new Box(from, to).expand(Node.HALF_NODE);
		Node ret = null;
		double curSqrDist = length * length;
		for(Node node : getNodesInBounds(bounds)){
			Optional<Vec3d> hit = node.bounds().raycast(from, to);
			if(hit.isPresent()){
				double sqrDist = from.squaredDistanceTo(hit.get());
				if(sqrDist < curSqrDist){
					ret = node;
					curSqrDist = sqrDist;
				}
			}
		}
		if(!ignoreBlocks)
			if(bhr.getPos().squaredDistanceTo(from) < curSqrDist)
				return Optional.empty(); // blocked by a block
		return Optional.ofNullable(ret);
	}
	
	// serialization
	
	public void writeToNbt(NbtCompound tag){
		NbtCompound fluxStatsNbt = new NbtCompound();
		for(Map.Entry<FluxOrigin, Float> entry : globalFluxStats.entrySet())
			fluxStatsNbt.putFloat(entry.getKey().name(), entry.getValue());
		tag.put("fluxStats", fluxStatsNbt);
		
		tag.put("pendingNodes", pendingNodes.stream().map(Node::toNbt).collect(NbtUtil.toNbtList()));
	}
	
	public void readFromNbt(NbtCompound tag){
		globalFluxStats.clear();
		NbtCompound fluxStatsNbt = tag.getCompound("fluxStats");
		for(String key : fluxStatsNbt.getKeys())
			try{
				globalFluxStats.put(FluxOrigin.valueOf(key), fluxStatsNbt.getFloat(key));
			}catch(IllegalArgumentException ignored){
				logger.error("Invalid flux origin with name \"{}\", ignoring.", key);
			}
		
		pendingNodes = StreamUtil.streamAndApply(tag.getList("pendingNodes", NbtElement.COMPOUND_TYPE), NbtCompound.class, Node::fromNbt)
				.collect(Collectors.toCollection(ArrayList::new));
	}
	
	public void sync(){
		world.syncComponent(KEY);
	}
	
	// ticking
	
	public void serverTick(){
		// bring in pending nodes as soon as possible
		for(int i = pendingNodes.size() - 1; i >= 0; i--){
			Node node = pendingNodes.get(i);
			AuraChunk there = AuraChunk.from(world, node.asBlockPos());
			if(there != null){
				logger.info("pendingNodes used");
				pendingNodes.remove(i);
				there.addNode(node);
				sync();
			}
		}
		
		// so, the general vibe of flux spreading is that:
		// - if a chunk has "significantly" more flux than its neighbors, they should pick up some fraction of the difference
		// - a completely unpolluted chunk requires a much larger difference to become polluted, and picks up significantly more initially
		// - flux spreading should be tick order independent
		// that means we cannot mutate the aura chunks as we iterate, and instead want a difference buffer applied after all computation
		
		Long2FloatMap diff = new Long2FloatOpenHashMap(world.getChunkManager().getLoadedChunkCount());
		var chunks = ((LoadedChunksCache)world).fabric_getLoadedChunks();
		for(Chunk chunk : chunks){
			AuraChunk here = AuraChunk.from(chunk);
			var pos = chunk.getPos();
			if(here.flux() > 10 && world.random.nextInt(5) == 0){
				ChunkPos towards = world.random.nextBoolean()
						? new ChunkPos(pos.x + (world.random.nextBoolean() ? 1 : -1), pos.z)
						: new ChunkPos(pos.x, pos.z + (world.random.nextBoolean() ? 1 : -1));
				AuraChunk there = AuraChunk.from(world, towards);
				// if we pass the arbitrary threshold...
				if(there != null && ((there.flux() > 0 && here.flux() > there.flux() + 10) || (here.flux() > 20))){
					// pass along 1/10 of the difference, floored to the nearest 0.01
					float passRaw = (here.flux() - there.flux()) / 10;
					float pass = (int)(passRaw * 100) / 100f;
					diff.put(pos.toLong(), diff.get(pos.toLong()) - pass);
					diff.put(towards.toLong(), diff.get(towards.toLong()) + pass);
				}
			}
		}
		
		for(long l : diff.keySet()){
			AuraChunk there = AuraChunk.from(world, new ChunkPos(l));
			there.setFlux(there.flux() + diff.get(l));
		}
	}
}