package arcana.api;

import arcana.ArcanaRegistry;
import arcana.aspects.Aspect;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import static arcana.Arcana.arcId;

public interface Core{
	
	Codec<Core> CODEC = Identifier.CODEC.xmap(Core::byName, Core::id);
	
	Core MISSING_CORE = new Core.Impl(arcId("missing"), 0, 0);
	
	// statics
	
	BiMap<Identifier, @NotNull Core> cores = HashBiMap.create();
	
	static @NotNull Core byName(String name){
		return byName(Identifier.of(name));
	}
	
	static @NotNull Core byName(Identifier name){
		return cores.getOrDefault(name, MISSING_CORE);
	}
	
	// members
	
	Identifier id();
	
	int capacity();
	
	int strength();
	
	default String translationKey(){
		return "wand.core." + id().getNamespace() + "." + id().getPath();
	}
	
	default int warping(){
		return 0;
	}
	
	default int percentOff(Aspect aspect){
		return 0;
	}
	
	default int complexity(){
		return 0;
	}
	
	static Core asCore(Item is){
		return is instanceof Core core ? core : is == Items.STICK ? ArcanaRegistry.STICK_CORE : null;
	}
	
	record Impl(Identifier id, int capacity, int strength) implements Core{}
}