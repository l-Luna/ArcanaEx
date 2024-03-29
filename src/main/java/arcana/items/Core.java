package arcana.items;

import arcana.ArcanaRegistry;
import arcana.aspects.Aspect;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

public interface Core{
	
	// statics
	
	BiMap<Identifier, Core> cores = HashBiMap.create();
	
	static Core byName(String name){
		return byName(new Identifier(name));
	}
	
	static Core byName(Identifier name){
		return cores.getOrDefault(name, ArcanaRegistry.MISSING_CORE);
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