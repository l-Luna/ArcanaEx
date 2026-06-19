package arcana.api;

import arcana.aspects.Aspect;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import static arcana.Arcana.arcId;

public interface Cap{
	
	Codec<Cap> CODEC = Identifier.CODEC.xmap(Cap::byName, Cap::id);
	
	Cap MISSING_CAP = new Cap.Impl(arcId("missing"), 0, 0);
	
	// statics
	
	BiMap<Identifier, @NotNull Cap> caps = HashBiMap.create();
	
	static @NotNull Cap byName(String name){
		return byName(Identifier.of(name));
	}
	
	static @NotNull Cap byName(Identifier name){
		return caps.getOrDefault(name, Cap.MISSING_CAP);
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