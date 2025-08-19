package arcana.aura;

import arcana.util.NbtUtil;
import arcana.util.StreamUtil;
import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
		this.flux = flux;
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
	
	// accessors
	
	public static AuraChunk from(Chunk chunk){
		return chunk.getComponent(KEY);
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
		if(!w.isChunkLoaded(pos.getX() / 16, pos.getZ() / 16))
			return null;
		Chunk c = w.getChunk(pos.getX() / 16, pos.getZ() / 16, ChunkStatus.EMPTY, false);
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
	
	public void writeToNbt(NbtCompound tag){
		tag.putFloat("flux", flux);
		tag.put("nodes", nodes.stream().map(Node::toNbt).collect(NbtUtil.toNbtList()));
	}
	
	public void readFromNbt(NbtCompound tag){
		flux = tag.getFloat("flux");
		nodes = StreamUtil.streamAndApply(tag.getList("nodes", NbtElement.COMPOUND_TYPE), NbtCompound.class, Node::fromNbt)
				.peek(x -> x.setChunk(this))
				.collect(Collectors.toCollection(ArrayList::new));
	}
	
	public void markDirty(){
		dirty = true;
	}
	
	public void sync(){
		chunk.syncComponent(KEY);
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