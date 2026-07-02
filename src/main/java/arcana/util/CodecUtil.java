package arcana.util;

import com.mojang.serialization.Codec;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
}