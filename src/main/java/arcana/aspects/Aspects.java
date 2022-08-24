package arcana.aspects;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.util.Identifier;

import static arcana.Arcana.arcId;

public final class Aspects{
	
	public static final BiMap<Identifier, Aspect> ASPECTS = HashBiMap.create(52);
	
	public static final Aspect
			AIR = create("air"),
			FIRE = create("fire"),
			WATER = create("water"),
			EARTH = create("earth"),
			ORDER = create("order"),
			ENTROPY = create("entropy");
	
	public static final Aspect LIFE = create("life");
	public static final Aspect VOID = create("void");
	public static final Aspect METAL = create("metal");
	
	public static final Aspect MAGIC = create("magic");
	
	public static Aspect byName(Identifier id){
		return ASPECTS.get(id);
	}
	
	// defaults to the arcana namespace, since minecraft has no aspects
	public static Aspect byName(String name){
		if(!name.contains(":"))
			return byName(arcId(name));
		else
			return byName(new Identifier(name));
	}
	
	private static Aspect create(String name){
		Identifier id = arcId(name);
		Aspect aspect = new Aspect(id);
		ASPECTS.put(id, aspect);
		return aspect;
	}
}