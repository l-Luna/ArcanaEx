package arcana.util;

import arcana.ArcanaTags;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class RegistryMapping<T>{

	private final Registry<T> registry;
	private final Map<T, T> entryMap = new HashMap<>();
	private final Map<TagKey<T>, T> tagMap = new HashMap<>();
	
	public RegistryMapping(Registry<T> registry){
		this.registry = registry;
	}
	
	public Optional<T> apply(T entry){
		if(entryMap.containsKey(entry))
			return Optional.of(entryMap.get(entry));
		for(Map.Entry<TagKey<T>, T> tagEntry : tagMap.entrySet())
			if(ArcanaTags.isOf(entry, tagEntry.getKey(), registry))
				return Optional.of(tagEntry.getValue());
		return Optional.empty();
	}
	
	public void clear(){
		entryMap.clear();
		tagMap.clear();
	}
	
	public boolean addIdentifierEntry(Identifier from, Identifier to){
		T a = registry.get(from), b = registry.get(to);
		if(a == null || b == null)
			return false;
		entryMap.put(a, b);
		return true;
	}
	
	public boolean addTagEntry(Identifier from, Identifier to){
		T b = registry.get(to);
		if(b == null)
			return false;
		tagMap.put(TagKey.of(registry.getKey(), from), b);
		return true;
	}
	
	public Registry<T> getRegistry(){
		return registry;
	}
	
	public Map<T, T> getEntryMap(){
		return Collections.unmodifiableMap(entryMap);
	}
	
	public Map<TagKey<T>, T> getTagMap(){
		return Collections.unmodifiableMap(tagMap);
	}
}