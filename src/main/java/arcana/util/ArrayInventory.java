package arcana.util;

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
	
	// i really do apologise
	public static final Codec<ArrayInventory> CODEC = Both.codec(Codec.INT, Both.codec(Codec.INT, ItemStack.CODEC).listOf()).xmap(
			x -> {
				ArrayInventory inventory = new ArrayInventory(x.fst());
				for(Both<Integer, ItemStack> pair : x.snd())
					inventory.setStack(pair.fst(), pair.snd());
				return inventory;
			},
			x -> {
				List<Both<Integer, ItemStack>> ret = new ArrayList<>();
				for(int i = 0; i < x.size(); i++){
					ItemStack there = x.getStack(i);
					if(!there.isEmpty())
						ret.add(Both.of(i, there));
				}
				return Both.of(x.size(), ret);
			}
	);
	
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
	
	public ArrayInventory copy(){
		ArrayInventory ret = new ArrayInventory(size());
		for(int i = 0; i < heldStacks.size(); i++)
			ret.setStack(i, heldStacks.get(i));
		return ret;
	}
}