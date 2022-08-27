package arcana.aspects;

import net.minecraft.nbt.NbtCompound;

import java.util.HashMap;
import java.util.Map;

public record AspectMap(Map<Aspect, Integer> underlying){
	
	public AspectMap(){
		this(new HashMap<>());
	}
	
	public void add(Aspect aspect, int amount){
		underlying.put(aspect, underlying.getOrDefault(aspect, 0) + amount);
	}
	
	public void add(AspectStack stack){
		add(stack.type(), stack.amount());
	}
	
	public void addCapped(Aspect aspect, int amount, int cap){
		underlying.put(aspect, Math.min(underlying.getOrDefault(aspect, 0) + amount, cap));
	}
	
	public void addCapped(AspectStack stack, int cap){
		addCapped(stack.type(), stack.amount(), cap);
	}
	
	public boolean contains(Aspect aspect){
		return underlying.containsKey(aspect);
	}
	
	public int indexOf(Aspect aspect){
		int i = 0;
		for(Aspect asp : underlying.keySet()){
			if(asp.equals(aspect))
				return i;
			i++;
		}
		return -1;
	}
	
	public int size(){
		return underlying().size();
	}
	
	public NbtCompound toNbt(){
		NbtCompound nbt = new NbtCompound();
		underlying.forEach((aspect, amount) -> nbt.putInt(aspect.id().toString(), amount));
		return nbt;
	}
	
	public static AspectMap fromNbt(NbtCompound nbt){
		Map<Aspect, Integer> map = new HashMap<>(nbt.getKeys().size());
		for(String key : nbt.getKeys())
			map.put(Aspects.byName(key), nbt.getInt(key));
		return new AspectMap(map);
	}
	
	public AspectMap copy(){
		return new AspectMap(new HashMap<>(underlying));
	}
}