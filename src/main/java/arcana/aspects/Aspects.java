package arcana.aspects;

import arcana.Arcana;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

import static arcana.Arcana.arcId;

public final class Aspects{
	
	public static final BiMap<Identifier, Aspect> ASPECTS = HashBiMap.create();
	static final List<Aspect> ORDERED_ASPECTS = new ArrayList<>();
	
	public static final Aspect
			AIR = create("air"),
			FIRE = create("fire"),
			WATER = create("water"),
			EARTH = create("earth"),
			ORDER = create("order"),
			ENTROPY = create("entropy");
	
	public static final Aspect LIFE = create("life", WATER, EARTH);
	public static final Aspect LIGHT = create("light", AIR, FIRE);
	public static final Aspect CRYSTAL = create("crystal", EARTH, ORDER);
	public static final Aspect ENERGY = create("energy", FIRE, ORDER);
	public static final Aspect ICE = create("ice", ENTROPY, FIRE);
	public static final Aspect EXCHANGE = create("exchange", ENTROPY, ORDER);
	public static final Aspect MOVEMENT = create("movement", ENTROPY, AIR);
	
	public static final Aspect VOID = create("void", ENTROPY, LIGHT);
	public static final Aspect PLANT = create("plant", EARTH, LIFE);
	public static final Aspect METAL = create("metal", EARTH, CRYSTAL);
	public static final Aspect SLIME = create("slime", WATER, LIFE);
	public static final Aspect TOOL = create("tool", ENERGY, EXCHANGE);
	
	public static final Aspect FLESH = create("flesh", PLANT, SLIME);
	public static final Aspect FABRIC = create("fabric", TOOL, PLANT);
	public static final Aspect JOURNEY = create("journey", MOVEMENT, ORDER);
	public static final Aspect WEAPON = create("weapon", TOOL, FLESH);
	public static final Aspect ARMOUR = create("armour", TOOL, LIFE);
	public static final Aspect MINING = create("mining", TOOL, METAL);
	
	public static final Aspect MIND = create("mind", FLESH, ENERGY);
	public static final Aspect SENSES = create("senses", FLESH, LIGHT);
	public static final Aspect UNDEAD = create("undead", LIFE, ENTROPY);
	public static final Aspect FLIGHT = create("flight", JOURNEY, AIR);
	
	public static final Aspect MACHINE = create("machine", MIND, TOOL);
	public static final Aspect ENVY = create("envy", MIND, WEAPON);
	public static final Aspect SLOTH = create("sloth", MIND, ICE);
	public static final Aspect GREED = create("greed", MIND, VOID);
	public static final Aspect MAGIC = create("magic", SENSES, ENERGY);
	
	public static final Aspect TAINT = create("taint", MAGIC, ENTROPY);
	public static final Aspect AURA = create("aura", MAGIC, AIR);
	
	public static final List<Aspect> PRIMALS = List.of(AIR, FIRE, WATER, EARTH, ORDER, ENTROPY);
	
	public static Aspect byName(Identifier id){
		return ASPECTS.get(id);
	}
	
	// defaults to the arcana namespace, since minecraft has no aspects
	public static Aspect byName(String name){
		return byName(Arcana.maybeArcId(name));
	}
	
	private static Aspect create(String name){
		return create(name, null, null);
	}
	
	private static Aspect create(String name, Aspect left, Aspect right){
		Identifier id = arcId(name);
		Aspect aspect = new Aspect(id, left, right);
		ASPECTS.put(id, aspect);
		ORDERED_ASPECTS.add(aspect);
		return aspect;
	}
	
	public static List<Aspect> getCompoundAspects(){
		List<Aspect> aspects = new ArrayList<>(ORDERED_ASPECTS);
		aspects.removeAll(PRIMALS);
		return aspects;
	}
}