package arcana.aura;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class AuraChunk{
	public final ChunkPos pos;
	public final AuraWorld world;
	
	private float flux;
	
	public AuraChunk(ChunkPos pos, AuraWorld world){
		this.pos = pos;
		this.world = world;
	}
	
	public static AuraChunk at(World world, BlockPos pos){
		return AuraWorld.from(world).getOrCreateChunk(pos);
	}
	
	public static AuraChunk fromNbt(NbtCompound tag, AuraWorld world){
		AuraChunk chunk = new AuraChunk(new ChunkPos(tag.getLong("pos")), world);
		chunk.flux = tag.getFloat("flux");
		return chunk;
	}
	
	public NbtCompound toNbt(){
		NbtCompound tag = new NbtCompound();
		tag.putLong("pos", pos.toLong());
		tag.putFloat("flux", flux);
		return tag;
	}
	
	public float getFlux(){
		return flux;
	}
	
	public void setFlux(float flux){
		this.flux = flux;
		// sync...?
	}
	
	public void incrementFlux(float inc, @Nullable FluxOrigin origin){
		flux += inc;
		if(origin != null)
			world.addFluxStat(origin, inc);
		// sync...?
	}
}