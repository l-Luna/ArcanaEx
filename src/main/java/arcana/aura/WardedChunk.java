package arcana.aura;

import arcana.cca_components.ChunkLayer;
import arcana.util.MathUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistryV3;

import static arcana.Arcana.arcId;

public class WardedChunk extends ChunkLayer{
	
	public static final ComponentKey<WardedChunk> KEY = ComponentRegistryV3.INSTANCE.getOrCreate(arcId("warded_chunk"), WardedChunk.class);
	
	public WardedChunk(Chunk chunk){
		super(chunk);
	}
	
	public static boolean isWarded(World w, BlockPos pos){
		if(!w.isChunkLoaded(pos.getX() >> 4, pos.getZ() >> 4))
			return false;
		WardedChunk there = KEY.getNullable(w.getChunk(pos));
		return there != null && there.isMarkedO(MathUtil.toChunkOffset(pos));
	}
	
	public static boolean setWarded(World w, BlockPos pos, boolean warded){
		Chunk chunk = w.getChunk(ChunkSectionPos.getSectionCoord(pos.getX()), ChunkSectionPos.getSectionCoord(pos.getZ()), ChunkStatus.EMPTY);
		return KEY.get(chunk).setMarkedO(MathUtil.toChunkOffset(pos), warded);
	}
	
	public static void sync(World w, BlockPos pos){
		KEY.sync(w.getChunk(pos));
	}
}