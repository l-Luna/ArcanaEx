package arcana.aura;

import arcana.components.ChunkLayer;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import net.minecraft.world.chunk.Chunk;

import static arcana.Arcana.arcId;

public class TaintChunk extends ChunkLayer implements ServerTickingComponent{
	
	public static final ComponentKey<TaintChunk> KEY = ComponentRegistryV3.INSTANCE.getOrCreate(arcId("taint_chunk"), TaintChunk.class);
	
	public TaintChunk(Chunk chunk){
		super(chunk);
	}
	
	public void serverTick(){
	
	}
}