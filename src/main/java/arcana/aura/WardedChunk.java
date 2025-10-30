package arcana.aura;

import arcana.components.ChunkLayer;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import static arcana.Arcana.arcId;

public class WardedChunk extends ChunkLayer{
	
	public static final ComponentKey<WardedChunk> KEY = ComponentRegistryV3.INSTANCE.getOrCreate(arcId("warded_chunk"), WardedChunk.class);
	
	public WardedChunk(Chunk chunk){
		super(chunk);
	}
	
	public static void setWarded(World w, BlockPos pos){
		WardedChunk wc = w.getChunk(pos).getComponent(KEY);
		
	}
}