package arcana.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;

import java.util.ArrayList;
import java.util.List;

// SimpleInventory with fixed NBT (de)serialization; the name means "obvious inventory"
public class ArrayInventory extends SimpleInventory{
	
	public static final Codec<ArrayInventory> CODEC = Codec.compoundList(Codec.INT, ItemStack.CODEC).xmap(
			x -> {
				ArrayInventory inventory = new ArrayInventory(x.size());
				for(Pair<Integer, ItemStack> pair : x)
					inventory.setStack(pair.getFirst(), pair.getSecond());
				return inventory;
			},
			x -> {
				List<Pair<Integer, ItemStack>> ret = new ArrayList<>();
				for(int i = 0; i < x.size(); i++){
					ItemStack there = x.getStack(i);
					if(!there.isEmpty())
						ret.add(Pair.of(i, there));
				}
				return ret;
			}
	);
	
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