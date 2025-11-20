package arcana.components;

import arcana.util.NbtUtil;
import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtLongArray;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;

import java.util.Arrays;
import java.util.BitSet;

public abstract class ChunkLayer implements Component, AutoSyncedComponent{
	
	protected final Chunk chunk;
	
	// chunk section -> blocks, chunk section may be null if all blocks are unset
	private final BitSet[] markedBlocks;
	private final int[] count;
	
	public ChunkLayer(Chunk chunk){
		this.chunk = chunk;
		int slices = chunk.getHeight() / 16;
		markedBlocks = new BitSet[slices];
		count = new int[slices];
	}
	
	//
	
	private int getSliceOffsetIndex(int x, int y, int z){
		return x + z*16 + (y%16)*16*16;
	}
	
	protected boolean isMarkedO(BlockPos offset){
		int yp = offset.getY() - chunk.getBottomY();
		int slice = yp / 16;
		return markedBlocks[slice] != null && markedBlocks[slice].get(getSliceOffsetIndex(offset.getX(), yp, offset.getZ()));
	}
	
	protected boolean markO(BlockPos offset){
		int yp = offset.getY() - chunk.getBottomY();
		int slice = yp / 16;
		if(markedBlocks[slice] == null)
			markedBlocks[slice] = new BitSet(16*16*16);
		int index = getSliceOffsetIndex(offset.getX(), yp, offset.getZ());
		if(markedBlocks[slice].get(index))
			return false;
		else{
			markedBlocks[slice].set(index);
			count[slice]++;
			return true;
		}
	}
	
	protected boolean unmarkO(BlockPos offset){
		int yp = offset.getY() - chunk.getBottomY();
		int slice = yp / 16;
		if(markedBlocks[slice] == null)
			return false;
		int index = getSliceOffsetIndex(offset.getX(), yp, offset.getZ());
		if(!markedBlocks[slice].get(index))
			return false;
		markedBlocks[slice].clear(index);
		if(--count[slice] == 0)
			markedBlocks[slice] = null;
		return true;
	}
	
	protected boolean setMarkedO(BlockPos offset, boolean marked){
		return marked ? markO(offset) : unmarkO(offset);
	}
	
	//
	
	public void writeToNbt(NbtCompound tag){
		tag.put("slices", Arrays.stream(markedBlocks)
				.map(x -> x != null ? x.toLongArray() : new long[0])
				.map(NbtLongArray::new)
				.collect(NbtUtil.toNbtList()));
	}
	
	public void readFromNbt(NbtCompound tag){
		int i = 0;
		for(NbtElement sliceE : tag.getList("slices", NbtElement.LONG_ARRAY_TYPE)){
			NbtLongArray slice = (NbtLongArray)sliceE;
			BitSet b = BitSet.valueOf(slice.getLongArray());
			// TODO: should this be serialized to avoid recounting? only for network sync?
			int card = b.cardinality();
			count[i] = card;
			markedBlocks[i] = card > 0 ? b : null;
			i++;
		}
	}
}