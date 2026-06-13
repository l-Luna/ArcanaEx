package arcana.util;

import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;

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
	public void readNbtList(NbtList list, RegistryWrapper.WrapperLookup registries){
		for(int i = 0; i < size(); i++)
			setStack(i, ItemStack.EMPTY);
		
		for(int i = 0; i < list.size(); i++){
			NbtCompound tag = list.getCompound(i);
			int idx = tag.getByte("slot");
			if(idx < size())
				ItemStack.fromNbt(registries, tag.get("stack")).ifPresent(itemStack -> setStack(idx, itemStack));
		}
	}
	
	@Override
	public NbtList toNbtList(RegistryWrapper.WrapperLookup registries){
		NbtList list = new NbtList();
		
		for(int i = 0; i < size(); i++){
			ItemStack stack = getStack(i);
			if(!stack.isEmpty()){
				NbtCompound tag = new NbtCompound();
				tag.putByte("slot", (byte)i);
				tag.put("stack", stack.encode(registries));
				list.add(tag);
			}
		}
		
		return list;
	}
}