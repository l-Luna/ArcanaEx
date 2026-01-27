package arcana.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

public class RotatedSidedInventoryView implements SidedInventory{
	
	private final SidedInventory inner;
	private final Direction rotation;
	
	public RotatedSidedInventoryView(SidedInventory inner, Direction rotation){
		this.inner = inner;
		this.rotation = rotation;
	}
	
	private Direction rotate(Direction original){
		if(original == null || original.getAxis() == Direction.Axis.Y)
			return original;
		return Direction.fromHorizontal(((rotation.getHorizontal() - original.getHorizontal() + 2) + 4) % 4);
	}
	
	public int[] getAvailableSlots(Direction side){
		return inner.getAvailableSlots(rotate(side));
	}
	
	public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir){
		return inner.canInsert(slot, stack, rotate(dir));
	}
	
	public boolean canExtract(int slot, ItemStack stack, Direction dir){
		return inner.canExtract(slot, stack, rotate(dir));
	}
	
	public int size(){
		return inner.size();
	}
	
	public boolean isEmpty(){
		return inner.isEmpty();
	}
	
	public ItemStack getStack(int slot){
		return inner.getStack(slot);
	}
	
	public ItemStack removeStack(int slot, int amount){
		return inner.removeStack(slot, amount);
	}
	
	public ItemStack removeStack(int slot){
		return inner.removeStack(slot);
	}
	
	public void setStack(int slot, ItemStack stack){
		inner.setStack(slot, stack);
	}
	
	public void markDirty(){
		inner.markDirty();
	}
	
	public boolean canPlayerUse(PlayerEntity player){
		return inner.canPlayerUse(player);
	}
	
	public void clear(){
		inner.clear();
	}
}