package arcana.util;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.inventory.InventoryChangedListener;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.stream.IntStream;

public class SidedArrayInventory extends ArrayInventory implements SidedInventory{
	
	private final Map<Direction, int[]> slotsBySide = new EnumMap<>(Direction.class);
	private final IntSet nonInsertableSlots = new IntOpenHashSet();
	private @Nullable BiPredicate<Integer, ItemStack> insertionFilter;
	
	public SidedArrayInventory(int size){
		super(size);
	}
	
	public SidedArrayInventory(ItemStack... items){
		super(items);
	}
	
	public SidedArrayInventory withDefaultSidedSlots(int... slots){
		for(Direction value : Direction.values())
			slotsBySide.put(value, slots);
		return this;
	}
	
	public SidedArrayInventory withDefaultSidedRange(int from, int amount){
		return withDefaultSidedSlots(IntStream.range(from, from + amount).toArray());
	}
	
	public SidedArrayInventory withSidedSlots(Direction direction, int... slots){
		slotsBySide.put(direction, slots);
		return this;
	}
	
	public SidedArrayInventory withNonInsertableSlots(int... slots){
		for(int slot : slots)
			nonInsertableSlots.add(slot);
		return this;
	}
	
	public SidedArrayInventory withListener(InventoryChangedListener listener){
		addListener(listener);
		return this;
	}
	
	public SidedArrayInventory withInsertionFilter(BiPredicate<Integer, ItemStack> filter){
		insertionFilter = filter;
		return this;
	}
	
	public SidedInventory rotatedView(Direction direction){
		return new RotatedSidedInventoryView(this, direction);
	}
	
	public int[] getAvailableSlots(Direction side){
		int[] slots = slotsBySide.get(side);
		return slots != null ? slots : new int[0];
	}
	
	public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir){
		return !nonInsertableSlots.contains(slot) && (insertionFilter == null || insertionFilter.test(slot, stack));
	}
	
	public boolean canExtract(int slot, ItemStack stack, Direction dir){
		return true;
	}
}