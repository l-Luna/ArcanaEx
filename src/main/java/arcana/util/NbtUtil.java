package arcana.util;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class NbtUtil{
	
	public static NbtCompound from(Map<String, Object> data){
		NbtCompound compound = new NbtCompound();
		for(Map.Entry<String, Object> entry : data.entrySet()){
			var key = entry.getKey();
			var value = entry.getValue();
			if(value instanceof Integer i)
				compound.putInt(key, i);
			else if(value instanceof String s)
				compound.putString(key, s);
			else if(value instanceof Identifier i)
				compound.putString(key, i.toString());
			else if(value instanceof NbtElement e)
				compound.put(key, e);
			// ...
		}
		return compound;
	}
	
	public static Collector<NbtElement, ?, NbtList> toNbtList(){
		return Collectors.toCollection(NbtList::new);
	}
}