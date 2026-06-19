package arcana.util;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

public class SidedArrayInventory extends ArrayInventory implements SidedInventory{
	
	private final Map<Direction, int[]> slotsBySide = new EnumMap<>(Direction.class);
	private final IntSet nonInsertableSlots = new IntOpenHashSet();
	
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
	
	public SidedArrayInventory withSidedSlots(Direction direction, int... slots){
		slotsBySide.put(direction, slots);
		return this;
	}
	
	public SidedArrayInventory withNonInsertableSlots(int... slots){
		for(int slot : slots)
			nonInsertableSlots.add(slot);
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
		return true;
	}
	
	public boolean canExtract(int slot, ItemStack stack, Direction dir){
		return true;
	}
}