package arcana.items;

import arcana.ArcanaRegistry;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

public interface Core{
	
	BiMap<Identifier, Core> CORES = HashBiMap.create();
	
	Identifier id();
	
	default String translationKey(){
		return "wand.core." + id().getNamespace() + "." + id().getPath();
	}
	
	// TODO: metrics, come balancing
	
	static Core asCore(Item is){
		return is instanceof Core core ? core : is == Items.STICK ? ArcanaRegistry.STICK_CORE : null;
	}
	
	record Impl(Identifier id) implements Core{}
}