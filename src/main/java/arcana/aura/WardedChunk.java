package arcana.aura;

import arcana.components.ChunkLayer;
import arcana.util.MathUtil;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;

import static arcana.Arcana.arcId;

public class WardedChunk extends ChunkLayer{
	
	public static final ComponentKey<WardedChunk> KEY = ComponentRegistryV3.INSTANCE.getOrCreate(arcId("warded_chunk"), WardedChunk.class);
	
	public WardedChunk(Chunk chunk){
		super(chunk);
	}
	
	public static boolean isWarded(World w, BlockPos pos){
		if(!w.isChunkLoaded(pos.getX() >> 4, pos.getZ() >> 4))
			return false;
		return w.getChunk(pos).getComponent(KEY).isMarkedO(MathUtil.toChunkOffset(pos));
	}
	
	public static boolean setWarded(World w, BlockPos pos, boolean warded){
		return w.getChunk(ChunkSectionPos.getSectionCoord(pos.getX()), ChunkSectionPos.getSectionCoord(pos.getZ()), ChunkStatus.EMPTY).getComponent(KEY).setMarkedO(MathUtil.toChunkOffset(pos), warded);
	}
	
	public static void sync(World w, BlockPos pos){
		w.getChunk(pos).syncComponent(KEY);
	}
}