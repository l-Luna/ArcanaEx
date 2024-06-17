package arcana.aura;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

public class AuraChunk{
	public final ChunkPos pos;
	
	private float flux;
	
	public AuraChunk(ChunkPos pos){
		this.pos = pos;
	}
	
	public static AuraChunk at(World world, BlockPos pos){
		return AuraWorld.from(world).getOrCreateChunk(pos);
	}
	
	public static AuraChunk fromNbt(NbtCompound tag){
		AuraChunk chunk = new AuraChunk(new ChunkPos(tag.getLong("pos")));
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
	
	public void incrementFlux(float inc){
		flux += inc;
		// sync...?
	}
}
