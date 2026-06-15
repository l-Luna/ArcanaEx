package arcana.aura;

import arcana.util.NbtUtil;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.cca.api.v3.component.Component;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistryV3;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

import java.util.*;
import java.util.stream.Collectors;

import static arcana.Arcana.arcId;

public class AuraChunk implements Component, AutoSyncedComponent, ServerTickingComponent{
	
	public static final ComponentKey<AuraChunk> KEY = ComponentRegistryV3.INSTANCE.getOrCreate(arcId("aura_chunk"), AuraChunk.class);
	
	private final Chunk chunk;
	private boolean dirty = false;
	
	private float flux;
	private List<Node> nodes = new ArrayList<>();
	
	public AuraChunk(Chunk chunk){
		this.chunk = chunk;
	}
	
	// getters
	
	public float flux(){
		return flux;
	}
	
	public void setFlux(float flux){
		this.flux = Math.max(0, flux);
		markDirty();
	}
	
	public List<Node> nodes(){
		return Collections.unmodifiableList(nodes);
	}
	
	public void addNode(Node node){
		nodes.add(node);
		node.setChunk(this);
		markDirty();
	}
	
	public void removeNode(Node node){
		nodes.remove(node);
		markDirty();
	}
	
	// would like to use this in AuraWorld, but generally keep this as an impl detail
	/* package-private */ Chunk chunk(){
		return chunk;
	}
	
	// accessors
	
	public static AuraChunk from(Chunk chunk){
		return KEY.get(chunk);
	}
	
	@Nullable
	public static AuraChunk from(World w, ChunkPos pos){
		if(!w.isChunkLoaded(pos.x, pos.z))
			return null;
		Chunk c = w.getChunk(pos.x, pos.z, ChunkStatus.EMPTY, false);
		return c != null ? from(c) : null;
	}
	
	@Nullable
	public static AuraChunk from(World w, BlockPos pos){
		if(!w.isChunkLoaded(pos.getX() >> 4, pos.getZ() >> 4))
			return null;
		Chunk c = w.getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.EMPTY, false);
		return c != null ? from(c) : null;
	}
	
	public static List<AuraChunk> chunksCovering(World w, Box box){
		int minX = (int)Math.floor(box.minX / 16.0), minZ = (int)Math.floor(box.minZ / 16.0);
		int maxX = (int)Math.ceil(box.maxX / 16.0), maxZ = (int)Math.ceil(box.maxZ / 16.0);
		List<AuraChunk> ret = new ArrayList<>((maxX - minX) * (maxZ - minZ));
		for(int x = minX; x <= maxX; x++)
			for(int z = minZ; z <= maxZ; z++)
				ret.add(from(w, new ChunkPos(x, z)));
		return ret;
	}
	
	// serialization
	
	public void writeToNbt(NbtCompound tag, RegistryWrapper.WrapperLookup lookup){
		tag.putFloat("flux", flux);
		tag.put("nodes", nodes.stream().map(Node::toNbt).collect(NbtUtil.toNbtList()));
	}
	
	public void readFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup lookup){
		flux = tag.getFloat("flux");
		Map<UUID, Node> oldNodes = nodes.stream().collect(Collectors.toMap(Node::getUuid, x -> x));
		nodes = new ArrayList<>(oldNodes.size());
		// try to keep Node objects stable
		NbtUtil.readList(tag, "nodes", Node::fromNbt).forEach(n -> {
			n.setChunk(this);
			UUID uuid = n.getUuid();
			if(oldNodes.containsKey(uuid)){
				Node oldNode = oldNodes.get(uuid);
				oldNode.copyFrom(n);
				nodes.add(oldNode);
			}else
				nodes.add(n);
		});
	}
	
	public void markDirty(){
		dirty = true;
		chunk.setNeedsSaving(true);
	}
	
	public void sync(){
		KEY.sync(chunk);
	}
	
	// ticking
	
	public void serverTick(){
		if(chunk instanceof WorldChunk wc){
			World world = wc.getWorld();
			for(Node node : nodes)
				node.tick(world);
		}
		
		if(dirty)
			sync();
		dirty = false;
	}
}