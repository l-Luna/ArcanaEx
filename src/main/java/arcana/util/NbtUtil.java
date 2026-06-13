package arcana.util;

import arcana.aura.NodeReference;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public final class NbtUtil{
	
	public static NbtCompound from(Map<String, Object> data){
		NbtCompound compound = new NbtCompound();
		for(Map.Entry<String, Object> entry : data.entrySet()){
			var key = entry.getKey();
			var value = entry.getValue();
			if(value instanceof Integer i)
				compound.putInt(key, i);
			else if(value instanceof Boolean b)
				compound.putBoolean(key, b);
			else if(value instanceof String s)
				compound.putString(key, s);
			else if(value instanceof Identifier i)
				compound.putString(key, i.toString());
			else if(value instanceof UUID uuid)
				compound.putUuid(key, uuid);
			else if(value instanceof NbtElement e)
				compound.put(key, e);
			else if(value instanceof HexPos pos)
				compound.putLong(key, pos.toLong());
			else if(value instanceof NodeReference nodeRef)
				compound.put(key, nodeRef.toNbt());
			else if(value instanceof ItemStack stack)
				compound.put(key, stack.encode());
			// ...
		}
		return compound;
	}
	
	public static Collector<NbtElement, ?, NbtList> toNbtList(){
		return Collectors.toCollection(NbtList::new);
	}
	
	public static <T> List<T> readList(NbtCompound compound, String name, Function<NbtCompound, T> reader){
		return compound.getList(name, NbtElement.COMPOUND_TYPE)
				.stream()
				.filter(NbtCompound.class::isInstance)
				.map(NbtCompound.class::cast)
				.map(reader)
				.toList();
	}
	
	public static <T> List<T> readMutList(NbtCompound compound, String name, Function<NbtCompound, T> reader){
		return new ArrayList<>(readList(compound, name, reader));
	}
}