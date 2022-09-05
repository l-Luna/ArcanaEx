package arcana.items;

import arcana.ArcanaRegistry;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.util.Identifier;

public interface Cap{
	
	// statics
	
	BiMap<Identifier, Cap> caps = HashBiMap.create();
	
	static Cap byName(String name){
		return byName(new Identifier(name));
	}
	
	static Cap byName(Identifier name){
		return caps.getOrDefault(name, ArcanaRegistry.MISSING_CAP);
	}
	
	// members
	
	Identifier id();
	
	default int warping(){
		return 0;
	}
	
	default String translationKey(){
		return "wand.cap." + id().getNamespace() + "." + id().getPath();
	}
	
	// TODO: metrics, come balancing
	
	record Impl(Identifier id) implements Cap{}
}