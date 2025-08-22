package arcana.aura;

import arcana.util.NbtUtil;
import com.unascribed.lib39.tunnel.api.ImmutableMarshallable;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkStatus;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Helper type for serializing references to nodes in-world, via their UUID and chunk.
 */
public record NodeReference(UUID uuid, ChunkPos pos) implements ImmutableMarshallable{
	
	/**
	 * Create a reference to a given node.
	 */
	public static NodeReference ref(Node node){
		return new NodeReference(node.getUuid(), new ChunkPos(node.asBlockPos()));
	}
	
	/**
	 * Try to find a node within loaded chunks. Returns empty if the chunk is unloaded, or if the node doesn't exist.
	 */
	public Optional<Node> deref(World w){
		AuraChunk chunk = AuraChunk.from(w, pos);
		if(chunk != null)
			for(Node node : chunk.nodes())
				if(node.getUuid().equals(uuid))
					return Optional.of(node);
		for(Node node : AuraWorld.from(w).pendingNodes())
			if(node.getUuid().equals(uuid))
				return Optional.of(node);
		return Optional.empty();
	}
	
	/**
	 * Try to find a node, loading the target chunk if necessary. Returns empty if the node doesn't exist.
	 */
	public Optional<Node> derefLoad(World w){
		// `create = false` because a reference can't point to a node that hasn't been generated yet
		w.getChunk(pos.x, pos.z, ChunkStatus.FULL, false);
		return deref(w);
	}
	
	public static NodeReference fromNbt(NbtCompound tag){
		return new NodeReference(tag.getUuid("uuid"), new ChunkPos(tag.getInt("chunkX"), tag.getInt("chunkZ")));
	}
	
	public NbtCompound toNbt(){
		return NbtUtil.from(Map.of(
				"uuid", uuid,
				"chunkX", pos.x,
				"chunkZ", pos.z
		));
	}
	
	public void writeToNetwork(PacketByteBuf buf){
		buf.writeUuid(uuid);
		buf.writeChunkPos(pos);
	}
	
	public static NodeReference readFromNetwork(PacketByteBuf buf){
		return new NodeReference(buf.readUuid(), buf.readChunkPos());
	}
}