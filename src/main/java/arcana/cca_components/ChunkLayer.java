package arcana.cca_components;

import arcana.util.MathUtil;
import arcana.util.NbtUtil;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtLongArray;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.cca.api.v3.component.Component;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

import java.util.Arrays;
import java.util.BitSet;

public abstract class ChunkLayer implements Component, AutoSyncedComponent{
	
	protected final Chunk chunk;
	
	// chunk section -> blocks, chunk section may be null if all blocks are unset
	private final BitSet[] markedBlocks;
	private final int[] count;
	private int total;
	
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
	
	private BlockPos fromSliceOffsetIndex(int soi){
		return new BlockPos(soi % 16, soi / 256, (soi / 16) % 16);
	}
	
	protected boolean isMarkedO(BlockPos offset){
		if(offset.getY() < chunk.getBottomY() || offset.getY() >= chunk.getTopY())
			return false;
		int yp = offset.getY() - chunk.getBottomY();
		int slice = yp / 16;
		return markedBlocks[slice] != null && markedBlocks[slice].get(getSliceOffsetIndex(offset.getX(), yp, offset.getZ()));
	}
	
	protected boolean markO(BlockPos offset){
		if(offset.getY() < chunk.getBottomY() || offset.getY() >= chunk.getTopY())
			return false;
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
			total++;
			chunk.setNeedsSaving(true);
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
		total--;
		chunk.setNeedsSaving(true);
		return true;
	}
	
	protected boolean setMarkedO(BlockPos offset, boolean marked){
		return marked ? markO(offset) : unmarkO(offset);
	}
	
	protected @Nullable BlockPos sampleO(Random rng){
		if(total == 0)
			return null;
		int idx = rng.nextInt(total);
		// find the chunk section that contains this value
		int ci;
		for(ci = 0; ci < count.length; ci++){
			if(idx < count[ci])
				break;
			idx -= count[ci];
		}
		return fromSliceOffsetIndex(MathUtil.indexOfNthBit(markedBlocks[ci], idx)).up(chunk.getBottomY() + ci*16);
	}
	
	protected int countO(int sectionIdx){
		if(sectionIdx < 0 || sectionIdx >= count.length)
			return 0;
		return count[sectionIdx];
	}
	
	//
	
	public void writeToNbt(NbtCompound tag, RegistryWrapper.WrapperLookup lookup){
		tag.put("slices", Arrays.stream(markedBlocks)
				.map(x -> x != null ? x.toLongArray() : new long[0])
				.map(NbtLongArray::new)
				.collect(NbtUtil.toNbtList()));
	}
	
	public void readFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup lookup){
		int i = 0;
		total = 0;
		for(NbtElement sliceE : tag.getList("slices", NbtElement.LONG_ARRAY_TYPE)){
			NbtLongArray slice = (NbtLongArray)sliceE;
			BitSet b = BitSet.valueOf(slice.getLongArray());
			// TODO: should this be serialized to avoid recounting? only for network sync?
			int card = b.cardinality();
			count[i] = card;
			total += card;
			markedBlocks[i] = card > 0 ? b : null;
			i++;
		}
	}
}