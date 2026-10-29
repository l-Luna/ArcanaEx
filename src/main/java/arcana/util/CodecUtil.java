package arcana.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import java.util.*;
import java.util.stream.Collectors;

public class CodecUtil{
	
	public static <K, V> Codec<Map<K, V>> assocListCodec(Codec<K> keyCodec, Codec<V> valueCodec){
		return Both.codec(keyCodec, valueCodec).listOf().xmap(
				xs -> {
					Map<K, V> map = new LinkedHashMap<>();
					for(Both<K, V> x : xs)
						map.put(x.fst(), x.snd());
					return map;
				},
				map -> {
					List<Both<K, V>> list = new ArrayList<>();
					for(Map.Entry<K, V> entry : map.entrySet())
						list.add(Both.of(entry.getKey(), entry.getValue()));
					return list;
				}
		);
	}
	
	public static <E extends Enum<E>> Codec<E> enumCodec(E[] values){
		Map<String, E> map = Arrays.stream(values).collect(Collectors.toMap(x -> x.name().toLowerCase(Locale.ROOT), x -> x));
		return Codec.STRING.flatXmap(
				name -> {
					String lower = name.toLowerCase(Locale.ROOT);
					return map.containsKey(lower) ? DataResult.success(map.get(lower)) : DataResult.error(() -> "Invalid constant name \"" + lower + "\"");
				},
				value -> DataResult.success(value.name().toLowerCase(Locale.ROOT))
		);
	}
}