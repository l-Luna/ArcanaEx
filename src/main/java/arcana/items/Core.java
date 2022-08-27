package arcana.items;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.util.Identifier;

public interface Core{
	
	BiMap<Identifier, Core> CORES = HashBiMap.create();
	
	Identifier id();
	
	default String translationKey(){
		return "wand.core." + id().getNamespace() + "." + id().getPath();
	}
	
	// TODO: metrics, come balancing
	
	record Impl(Identifier id) implements Core{}
}