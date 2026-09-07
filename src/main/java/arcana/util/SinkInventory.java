package arcana.util;

import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Predicate;

// one-way item insertion "inventory", similar to ComposterBlock.ComposterInventory
public class SinkInventory extends ArrayInventory implements SidedInventory{
	
	private final Consumer<ItemStack> sink;
	private final @Nullable Predicate<ItemStack> accepts;
	private boolean spent;
	
	public SinkInventory(Consumer<ItemStack> sink){
		this(sink, null);
	}
	
	public SinkInventory(Consumer<ItemStack> sink, @Nullable Predicate<ItemStack> accepts){
		super(1);
		this.sink = sink;
		this.accepts = accepts;
	}
	
	public int[] getAvailableSlots(Direction side){
		return new int[]{0};
	}
	
	public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir){
		return !spent && (accepts == null || accepts.test(stack));
	}
	
	public boolean canExtract(int slot, ItemStack stack, Direction dir){
		return false;
	}
	
	public void markDirty(){
		ItemStack it = getStack(0);
		if(!it.isEmpty()){
			spent = true;
			sink.accept(it);
			removeStack(0);
		}
	}
}