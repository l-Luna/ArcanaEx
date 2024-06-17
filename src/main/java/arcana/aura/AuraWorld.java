package arcana.aura;

import arcana.util.NbtUtil;
import com.mojang.logging.LogUtils;
import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.CommonTickingComponent;
import it.unimi.dsi.fastutil.longs.Long2FloatMap;
import it.unimi.dsi.fastutil.longs.Long2FloatOpenHashMap;
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
import org.slf4j.Logger;

import java.util.*;

import static arcana.Arcana.arcId;

public final class AuraWorld implements Component, CommonTickingComponent, AutoSyncedComponent{
	
	private static final Logger logger = LogUtils.getLogger();
	
	public static final ComponentKey<AuraWorld> KEY = ComponentRegistryV3.INSTANCE.getOrCreate(arcId("aura_world"), AuraWorld.class);
	
	private final World world;
	private final List<Node> nodes = new ArrayList<>();
	private final Map<ChunkPos, AuraChunk> chunks = new HashMap<>();
	private final Map<FluxOrigin, Float> fluxStats = new EnumMap<>(FluxOrigin.class);
	
	private final List<Node> nodesToAdd = new ArrayList<>();
	
	public AuraWorld(World world){
		this.world = world;
	}
	
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
	
	public void readFromNbt(NbtCompound tag){
		nodes.clear();
		tag.getList("nodes", NbtElement.COMPOUND_TYPE).forEach(x -> nodes.add(Node.fromNbt((NbtCompound)x, world)));
		
		chunks.clear();
		for(NbtElement cNbt : tag.getList("chunks", NbtElement.COMPOUND_TYPE)){
			AuraChunk y = AuraChunk.fromNbt((NbtCompound)cNbt, this);
			chunks.put(y.pos, y);
		}
		
		fluxStats.clear();
		NbtCompound fluxStatsNbt = tag.getCompound("fluxStats");
		for(String key : fluxStatsNbt.getKeys())
			try{
				fluxStats.put(FluxOrigin.valueOf(key), fluxStatsNbt.getFloat(key));
			}catch(IllegalArgumentException ignored){
				logger.error("Invalid flux origin with name \"{}\", ignoring.", key);
			}
	}
	
	public void writeToNbt(NbtCompound tag){
		tag.put("nodes", nodes.stream().map(Node::toNbt).collect(NbtUtil.toNbtList()));
		tag.put("chunks", chunks.values().stream().map(AuraChunk::toNbt).collect(NbtUtil.toNbtList()));
		
		NbtCompound fluxStatsNbt = new NbtCompound();
		for(Map.Entry<FluxOrigin, Float> entry : fluxStats.entrySet())
			fluxStatsNbt.putFloat(entry.getKey().name(), entry.getValue());
		tag.put("fluxStats", fluxStatsNbt);
	}
	
	public void tick(){
		boolean client = world.isClient;
		for(Node node : getNodes())
			if(client || (world instanceof ServerWorld sw && sw.isChunkLoaded(node.asBlockPos())))
				node.tick();
		
		nodes.addAll(nodesToAdd);
		if(!nodesToAdd.isEmpty() && !world.isClient)
			sync();
		nodesToAdd.clear();
		
		// so, the general vibe of flux spreading is that:
		// - if a chunk has "significantly" more flux than its neighbors, they should pick up some fraction of the difference
		// - a completely unpolluted chunk requires a much larger difference to become polluted, and picks up significantly more initially
		// - flux spreading should be tick order independent
		// that means we cannot mutate the aura chunks as we iterate, and instead want a difference buffer applied after all computation
		Long2FloatMap diff = new Long2FloatOpenHashMap(chunks.size());
		for(ChunkPos pos : chunks.keySet()){
			var mHere = getChunk(pos);
			if(mHere.isPresent()){
				AuraChunk here = mHere.get();
				if(here.getFlux() > 10 && world.random.nextInt(5) == 0){
					ChunkPos towards = world.random.nextBoolean()
							? new ChunkPos(pos.x + (world.random.nextBoolean() ? 1 : -1), pos.z)
							: new ChunkPos(pos.x, pos.z + (world.random.nextBoolean() ? 1 : -1));
					float fluxThere = getChunk(towards).map(AuraChunk::getFlux).orElse(0f);
					// if we pass the arbitrary threshold...
					if((fluxThere > 0 && here.getFlux() > fluxThere + 10) || (here.getFlux() > 20)){
						// pass along 1/10 of the difference, floored to the nearest 0.01
						float passRaw = (here.getFlux() - fluxThere) / 10;
						float pass = (int)(passRaw * 100) / 100f;
						diff.put(pos.toLong(), diff.get(pos.toLong()) - pass);
						diff.put(towards.toLong(), diff.get(towards.toLong()) + pass);
					}
				}
			}
		}
		for(long l : diff.keySet())
			getOrCreateChunk(new ChunkPos(l)).incrementFlux(diff.get(l), null);
		
		// clean up chunks with no flux
		for(ChunkPos pos : chunks.keySet()./* allow mutation */toArray(ChunkPos[]::new))
			if(chunks.containsKey(pos) && chunks.get(pos).getFlux() <= 0)
				chunks.remove(pos);
	}
	
	// "public" API
	
	// TODO: methods for only syncing one node, nodes in a chunk...
	public void sync(){
		world.syncComponent(KEY);
	}
	
	public World getWorld(){
		return world;
	}
	
	// nodes
	
	public void addNode(Node node){
		nodesToAdd.add(node);
	}
	
	public List<Node> getNodes(){
		return nodes;
	}
	
	public List<Node> getNodesInBounds(Box bounds){
		return filterBounds(getNodes(), bounds);
	}
	
	public static List<Node> filterBounds(List<Node> nodes, Box bounds){
		List<Node> ret = new ArrayList<>();
		for(Node node : nodes)
			if(bounds.contains(node.asVec3d()))
				ret.add(node);
		return ret;
	}
	
	public Optional<Node> raycastNodes(Position from, double length, boolean ignoreBlocks, Entity viewer){
		return raycastNodes(getNodes(), from, length, ignoreBlocks, viewer);
	}
	
	public static Optional<Node> raycastNodes(List<Node> nodes, Position fromPos, double length, boolean ignoreBlocks, Entity viewer){
		Vec3d from = new Vec3d(fromPos.getX(), fromPos.getY(), fromPos.getZ());
		Vec3d to = from.add(viewer.getRotationVector().multiply(length));
		BlockHitResult bhr = null;
		if(!ignoreBlocks)
			bhr = viewer.world.raycast(new RaycastContext(from, to, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, viewer));
		Box bounds = new Box(from, to);
		Node ret = null;
		double curDist = length;
		for(Node node : filterBounds(nodes, bounds)){
			Optional<Vec3d> hit = node.bounds().raycast(from, to);
			if(hit.isPresent()){ // TODO: use squared distance in comparisons? skip nodes based on block hit?
				double dist = from.distanceTo(hit.get());
				if(dist < curDist){
					ret = node;
					curDist = dist;
				}
			}
		}
		if(!ignoreBlocks)
			if(bhr.getPos().distanceTo(from) < curDist)
				return Optional.empty(); // blocked by a block
		return Optional.ofNullable(ret);
	}
	
	// aura chunks
	
	public Optional<AuraChunk> getChunk(ChunkPos pos){
		return Optional.ofNullable(chunks.get(pos));
	}
	
	public Optional<AuraChunk> getChunk(BlockPos pos){
		return getChunk(new ChunkPos(pos));
	}
	
	public AuraChunk getOrCreateChunk(ChunkPos pos){
		return chunks.computeIfAbsent(pos, x -> new AuraChunk(x, this));
	}
	
	public AuraChunk getOrCreateChunk(BlockPos pos){
		return getOrCreateChunk(new ChunkPos(pos));
	}
	
	public Map<FluxOrigin, Float> getFluxStats(){
		return fluxStats;
	}
	
	public void addFluxStat(FluxOrigin origin, float amount){
		fluxStats.merge(origin, amount, Float::sum);
	}
}