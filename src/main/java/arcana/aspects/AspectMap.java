package arcana.aspects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.Encoder;
import com.unascribed.lib39.tunnel.api.Marshallable;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

/**
 * Represents a quantity of aspects. The physical meaning of aspects is left undefined and varies
 * (e.g. essentia in a jar, impure essentia in a crucible, vis in a node).
 */
public record AspectMap(Map<Aspect, Integer> underlying) implements Iterable<AspectStack>, Marshallable{
	
	private static final Decoder<AspectMap> SHORTHAND_DECODER = Codec.STRING.listOf().flatMap(list -> {
		AspectMap map = new AspectMap();
		for(String s : list){
			int count = 1;
			if(s.contains("*")){
				String[] split = s.split("\\*", 2);
				try{
					count = Integer.parseInt(split[0]);
				}catch(NumberFormatException e){
					return DataResult.error(e::getMessage, map);
				}
				s = split[1];
			}
			Aspect aspect = Aspects.byName(s);
			if(aspect == null){
				String tmp = s;
				return DataResult.error(() -> "Invalid aspect: \"" + tmp + "\"", map);
			}
			map.add(aspect, count);
		}
		return DataResult.success(map);
	});
	private static final Codec<AspectMap> BASE_CODEC = Codec.unboundedMap(Aspect.CODEC, Codec.INT).xmap(AspectMap::new, AspectMap::underlying);
	
	public static final Codec<AspectMap> CODEC = Codec.withAlternative(BASE_CODEC, Codec.of(Encoder.error("unreachable"), SHORTHAND_DECODER));
	public static final PacketCodec<ByteBuf, AspectMap> PACKET_CODEC = PacketCodecs.codec(BASE_CODEC);
	
	public AspectMap(){
		this(new LinkedHashMap<>());
	}
	
	public AspectMap{
		underlying = new LinkedHashMap<>(underlying);
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
		// don't reduce the amount we have
		set(aspect, Math.max(Math.min(get(aspect) + amount, cap), get(aspect)));
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
	
	public void multiply(int multiplier){
		for(Aspect aspect : new HashSet<>(aspectSet()))
			set(aspect, get(aspect) * multiplier);
	}
	
	public void multiply(float multiplier){
		for(Aspect aspect : new HashSet<>(aspectSet()))
			set(aspect, (int)(get(aspect) * multiplier));
	}
	
	public void multiply(Function<Aspect, Float> multiplier){
		for(Aspect aspect : new HashSet<>(aspectSet()))
			set(aspect, (int)(get(aspect) * multiplier.apply(aspect)));
	}
	
	public int total(){
		int sum = 0;
		for(int u : underlying.values())
			sum += u;
		return sum;
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
	
	public static AspectMap fromAspectStacks(AspectStack... stacks){
		if(stacks == null)
			return new AspectMap();
		AspectMap map = new AspectMap(new LinkedHashMap<>(stacks.length));
		for(AspectStack stack : stacks)
			map.add(stack);
		return map;
	}
	
	public static AspectMap fromAspectStack(AspectStack stack){
		return fromAspectStacks(stack);
	}
	
	public AspectMap copy(){
		return new AspectMap(new LinkedHashMap<>(underlying));
	}
	
	@NotNull
	public Iterator<AspectStack> iterator(){
		return asStacks().iterator();
	}
	
	public void writeToNetwork(RegistryByteBuf buf){
		buf.writeVarInt(size());
		for(AspectStack stack : this){
			buf.writeIdentifier(stack.type().id());
			buf.writeVarInt(stack.amount());
		}
	}
	
	public void readFromNetwork(RegistryByteBuf buf){
		clear();
		int count = buf.readVarInt();
		for(int i = 0; i < count; i++)
			add(Aspects.byName(buf.readIdentifier()), buf.readVarInt());
	}
}