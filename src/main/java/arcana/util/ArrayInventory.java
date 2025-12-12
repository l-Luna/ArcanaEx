package arcana.util;

import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

// SimpleInventory with fixed NBT (de)serialization; the name means "obvious inventory"
public class ArrayInventory extends SimpleInventory{
	
	public ArrayInventory(){
		super();
	}
	
	public ArrayInventory(int size){
		super(size);
	}
	
	public ArrayInventory(ItemStack... items){
		super(items);
	}
	
	@Override
	public void readNbtList(NbtList nbtList){
		for(int i = 0; i < size(); i++)
			setStack(i, ItemStack.EMPTY);
		
		for(int i = 0; i < nbtList.size(); i++){
			NbtCompound tag = nbtList.getCompound(i);
			int idx = tag.getByte("Slot");
			if(idx < size())
				setStack(idx, ItemStack.fromNbt(tag));
		}
	}
	
	@Override
	public NbtList toNbtList(){
		NbtList list = new NbtList();
		
		for(int i = 0; i < size(); i++){
			ItemStack stack = getStack(i);
			if(!stack.isEmpty()){
				NbtCompound tag = new NbtCompound();
				tag.putByte("Slot", (byte)i);
				stack.writeNbt(tag);
				list.add(tag);
			}
		}
		
		return list;
	}
}