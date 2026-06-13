package arcana.api;

import arcana.ArcanaRegistry;
import arcana.aspects.Aspect;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public interface Cap{
	
	// statics
	
	BiMap<Identifier, @NotNull Cap> caps = HashBiMap.create();
	
	static @NotNull Cap byName(String name){
		return byName(Identifier.of(name));
	}
	
	static @NotNull Cap byName(Identifier name){
		return caps.getOrDefault(name, ArcanaRegistry.MISSING_CAP);
	}
	
	// members
	
	Identifier id();
	
	int capacity();
	
	int complexity();
	
	default int warping(){
		return 0;
	}
	
	default int percentOff(Aspect aspect){
		return 0;
	}
	
	default int strength(){
		return 0;
	}
	
	default String translationKey(){
		return "wand.cap." + id().getNamespace() + "." + id().getPath();
	}
	
	record Impl(Identifier id, int capacity, int complexity) implements Cap{}
}