package arcana.aura;

import arcana.Arcana;
import arcana.ArcanaConfig;
import arcana.util.NbtUtil;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.Long2FloatMap;
import it.unimi.dsi.fastutil.longs.Long2FloatOpenHashMap;
import net.fabricmc.fabric.impl.event.lifecycle.LoadedChunksCache;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.*;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.cca.api.v3.component.Component;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistryV3;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;
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
	
	public List<Node> pendingNodes(){
		return Collections.unmodifiableList(pendingNodes);
	}
	
	// accessors
	
	public static AuraWorld from(World world){
		return KEY.get(world);
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
	
	public Optional<Node> raycastNodes(LivingEntity viewer, boolean ignoreBlocks){
		return raycastNodes(viewer, viewer.getAttributeValue(EntityAttributes.PLAYER_BLOCK_INTERACTION_RANGE), ignoreBlocks);
	}
	
	public Optional<Node> raycastNodes(Entity viewer, double length, boolean ignoreBlocks){
		return raycastNodes(viewer.getEyePos(), length, ignoreBlocks, viewer);
	}
	
	public Optional<Node> raycastNodes(Position fromPos, double length, boolean ignoreBlocks, Entity viewer){
		Vec3d from = new Vec3d(fromPos.getX(), fromPos.getY(), fromPos.getZ());
		Vec3d to = from.add(viewer.getRotationVector().multiply(length));
		BlockHitResult bhr = null;
		if(!ignoreBlocks)
			bhr = viewer.getWorld().raycast(new RaycastContext(from, to, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, viewer));
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
	
	public void writeToNbt(NbtCompound tag, RegistryWrapper.WrapperLookup lookup){
		NbtCompound fluxStatsNbt = new NbtCompound();
		for(Map.Entry<FluxOrigin, Float> entry : globalFluxStats.entrySet())
			fluxStatsNbt.putFloat(entry.getKey().name(), entry.getValue());
		tag.put("fluxStats", fluxStatsNbt);
		
		synchronized(this){
			tag.put("pendingNodes", pendingNodes.stream().map(Node::toNbt).collect(NbtUtil.toNbtList()));
		}
	}
	
	public void readFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup lookup){
		globalFluxStats.clear();
		NbtCompound fluxStatsNbt = tag.getCompound("fluxStats");
		for(String key : fluxStatsNbt.getKeys())
			try{
				globalFluxStats.put(FluxOrigin.valueOf(key), fluxStatsNbt.getFloat(key));
			}catch(IllegalArgumentException ignored){
				logger.error("Invalid flux origin with name \"{}\", ignoring.", key);
			}
		
		synchronized(this){
			pendingNodes = NbtUtil.readMutList(tag, "pendingNodes", Node::fromNbt);
		}
	}
	
	public void sync(){
		KEY.sync(world);
	}
	
	// ticking
	
	public void serverTick(){
		// bring in pending nodes as soon as possible
		synchronized(this){
			for(int i = pendingNodes.size() - 1; i >= 0; i--){
				Node node = pendingNodes.get(i);
				AuraChunk there = AuraChunk.from(world, node.asBlockPos());
				if(there != null){
					pendingNodes.remove(i);
					there.addNode(node);
					sync();
				}
			}
		}
		
		// so, the general vibe of flux spreading is that:
		// - if a chunk has "significantly" more flux than its neighbors, they should pick up some fraction of the difference
		// - a completely unpolluted chunk requires a much larger difference to become polluted, and picks up significantly more initially
		// - flux spreading should be tick order independent
		// that means we cannot mutate the aura chunks as we iterate, and instead want a difference buffer applied after all computation
		ArcanaConfig.TaintConfig config = Arcana.CONFIG.taintConfig;
		
		Long2FloatMap diff = new Long2FloatOpenHashMap(world.getChunkManager().getLoadedChunkCount());
		Set<WorldChunk> chunks = ((LoadedChunksCache)world).fabric_getLoadedChunks();
		for(Chunk chunk : chunks){
			AuraChunk here = AuraChunk.from(chunk);
			var pos = chunk.getPos();
			if(here.flux() > config.fluxSpreadThreshold && world.random.nextInt(5) == 0){
				ChunkPos towards = world.random.nextBoolean()
						? new ChunkPos(pos.x + (world.random.nextBoolean() ? 1 : -1), pos.z)
						: new ChunkPos(pos.x, pos.z + (world.random.nextBoolean() ? 1 : -1));
				AuraChunk there = AuraChunk.from(world, towards);
				// if we pass the arbitrary threshold...
				if(there != null && here.flux() > there.flux() + config.fluxSpreadRelativeThreshold){
					// pass along 1/10 of the difference, floored to the nearest 0.01
					float passRaw = (here.flux() - there.flux()) / 10;
					float pass = (int)(passRaw * 100) / 100f;
					diff.put(pos.toLong(), diff.get(pos.toLong()) - pass);
					diff.put(towards.toLong(), diff.get(towards.toLong()) + pass);
				}
			}
		}
		
		for(long l : diff.keySet()){
			ChunkPos pos = new ChunkPos(l);
			AuraChunk there = AuraChunk.from(world, pos);
			float flux = there.flux() + diff.get(l);
			there.setFlux(flux);
			if(flux > config.taintedNodeSpawnCost)
				trySpawnTaintedNode(pos, there);
		}
	}
	
	private void trySpawnTaintedNode(ChunkPos pos, AuraChunk there){
		boolean valid = true;
		for(int xO = 0; xO < 5; xO++)
			for(int zO = 0; zO < 5; zO++){
				AuraChunk from = AuraChunk.from(world, new ChunkPos(xO + pos.x - 2, zO + pos.z - 2));
				valid &= from != null && from.nodes().stream().noneMatch(x -> x.getType() == NodeTypes.TAINTED);
			}
		
		if(valid){
			there.setFlux(there.flux() - Arcana.CONFIG.taintConfig.taintedNodeSpawnCost);
			Random rng = world.random;
			int x = rng.nextInt(15), z = rng.nextInt(15), y = there.chunk().sampleHeightmap(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z);
			addNode(new Node(NodeTypes.TAINTED, new Vec3d(pos.getStartX() + x + rng.nextFloat(), y + 4 + rng.nextFloat() * 6, pos.getStartZ() + z + rng.nextFloat()), NodeTypes.TAINTED.randomCap(rng)));
		}
	}
}