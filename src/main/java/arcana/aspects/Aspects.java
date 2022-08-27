package arcana.aspects;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static arcana.Arcana.arcId;

public final class Aspects{
	
	public static final BiMap<Identifier, Aspect> ASPECTS = HashBiMap.create();
	protected static final List<Aspect> ORDERED_ASPECTS = new ArrayList<>();
	
	public static final Aspect
			AIR = create("air"),
			FIRE = create("fire"),
			WATER = create("water"),
			EARTH = create("earth"),
			ORDER = create("order"),
			ENTROPY = create("entropy");
	
	public static final Aspect LIFE = create("life");
	public static final Aspect LIGHT = create("light");
	public static final Aspect CRYSTAL = create("crystal");
	public static final Aspect ENERGY = create("energy");
	public static final Aspect VOID = create("void");
	public static final Aspect ICE = create("ice");
	public static final Aspect EXCHANGE = create("exchange");
	public static final Aspect MOVEMENT = create("movement");
	
	public static final Aspect PLANT = create("plant");
	public static final Aspect METAL = create("metal");
	public static final Aspect SLIME = create("slime");
	public static final Aspect TOOL = create("tool");
	
	public static final Aspect FLESH = create("flesh");
	public static final Aspect FABRIC = create("fabric");
	public static final Aspect JOURNEY = create("journey");
	public static final Aspect WEAPON = create("weapon");
	public static final Aspect ARMOUR = create("armour");
	public static final Aspect MINING = create("mining");
	
	public static final Aspect MACHINE = create("machine");
	public static final Aspect GREED = create("greed");
	public static final Aspect ENVY = create("envy");
	public static final Aspect SLOTH = create("sloth");
	public static final Aspect MIND = create("mind");
	public static final Aspect SENSES = create("senses");
	public static final Aspect UNDEAD = create("undead");
	public static final Aspect FLIGHT = create("flight");
	
	public static final Aspect MAGIC = create("magic");
	public static final Aspect TAINT = create("taint");
	public static final Aspect AURA = create("aura");
	
	public static final List<Aspect> PRIMALS = List.of(AIR, FIRE, WATER, EARTH, ORDER, ENTROPY);
	
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
		ORDERED_ASPECTS.add(aspect);
		return aspect;
	}
}