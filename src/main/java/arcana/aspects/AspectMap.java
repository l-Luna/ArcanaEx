package arcana.aspects;

import net.minecraft.nbt.NbtCompound;

import java.util.*;
import java.util.function.Function;

public record AspectMap(Map<Aspect, Integer> underlying){
	
	public AspectMap(){
		this(new LinkedHashMap<>());
	}
	
	public int get(Aspect aspect){
		return underlying.getOrDefault(aspect, 0);
	}
	
	public void set(Aspect aspect, int amount){
		if(amount <= 0)
			underlying.remove(aspect);
		else
			underlying.put(aspect, amount);
	}
	
	public Set<Aspect> aspectSet(){
		return underlying.keySet();
	}
	
	public void clear(){
		underlying.clear();
	}
	
	public void add(Aspect aspect, int amount){
		set(aspect, get(aspect) + amount);
	}
	
	public void add(AspectStack stack){
		add(stack.type(), stack.amount());
	}
	
	public void add(AspectMap aspects){
		aspects.asStacks().forEach(this::add);
	}
	
	public void addCapped(Aspect aspect, int amount, int cap){
		set(aspect, Math.min(get(aspect) + amount, cap));
	}
	
	public void addCapped(AspectStack stack, int cap){
		addCapped(stack.type(), stack.amount(), cap);
	}
	
	public void take(Aspect aspect, int amount){
		add(aspect, -amount);
	}
	
	public void take(AspectStack stack){
		add(stack.type(), -stack.amount());
	}
	
	public void take(AspectMap map){
		map.asStacks().forEach(this::take);
	}
	
	public boolean contains(Aspect aspect, int amount){
		return get(aspect) >= amount;
	}
	
	public boolean contains(Aspect aspect){
		return contains(aspect, 1);
	}
	
	public boolean contains(AspectStack stack){
		return contains(stack.type(), stack.amount());
	}
	
	public boolean contains(AspectMap other){
		return other.asStacks().stream().allMatch(this::contains);
	}
	
	public void multiply(Function<Aspect, Float> multiplier){
		for(Aspect aspect : new HashSet<>(aspectSet()))
			set(aspect, (int)(get(aspect) * multiplier.apply(aspect)));
	}
	
	public int indexOf(Aspect aspect){
		int i = 0;
		for(Aspect asp : aspectSet()){
			if(asp.equals(aspect))
				return i;
			i++;
		}
		return -1;
	}
	
	public Aspect aspectByIndex(int idx){
		int i = 0;
		for(Aspect asp : aspectSet()){
			if(i == idx)
				return asp;
			i++;
		}
		return null;
	}
	
	public int size(){
		return underlying.size();
	}
	
	public boolean isEmpty(){
		return size() == 0;
	}
	
	public List<AspectStack> asStacks(){
		var aspects = aspectSet();
		List<AspectStack> ret = new ArrayList<>(aspects.size());
		for(Aspect aspect : aspects)
			ret.add(new AspectStack(aspect, get(aspect)));
		return ret;
	}
	
	public NbtCompound toNbt(){
		NbtCompound nbt = new NbtCompound();
		underlying.forEach((aspect, amount) -> nbt.putInt(aspect.id().toString(), amount));
		return nbt;
	}
	
	public static AspectMap fromNbt(NbtCompound nbt){
		if(nbt == null)
			return new AspectMap();
		Map<Aspect, Integer> map = new LinkedHashMap<>(nbt.getKeys().size());
		for(String key : nbt.getKeys())
			map.put(Aspects.byName(key), nbt.getInt(key));
		return new AspectMap(map);
	}
	
	public static AspectMap fromAspectStacks(List<AspectStack> stacks){
		if(stacks == null)
			return new AspectMap();
		AspectMap map = new AspectMap(new LinkedHashMap<>(stacks.size()));
		for(AspectStack stack : stacks)
			map.add(stack);
		return map;
	}
	
	public static AspectMap fromAspectStack(AspectStack stack){
		return fromAspectStacks(List.of(stack));
	}
	
	public AspectMap copy(){
		return new AspectMap(new LinkedHashMap<>(underlying));
	}
}