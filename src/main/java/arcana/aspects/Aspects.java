package arcana.aspects;

import arcana.Arcana;
import arcana.blocks.CrystalClusterBlock;
import arcana.items.ClusterSeedItem;
import arcana.items.CrystalItem;
import arcana.items.PhialItem;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.util.Identifier;

import java.util.*;

import static arcana.Arcana.arcId;

public final class Aspects{
	
	public static final BiMap<Identifier, Aspect> aspects = HashBiMap.create();
	public static final Map<Aspect, CrystalItem> crystals = new HashMap<>();
	public static final Map<Aspect, CrystalClusterBlock> clusters = new HashMap<>();
	public static final Map<Aspect, ClusterSeedItem> clusterSeeds = new HashMap<>();
	public static final Map<Aspect, PhialItem> phials = new HashMap<>(); // excluding empty phial
	static final List<Aspect> orderedAspects = new ArrayList<>();
	
	public static final Aspect
			AIR = create("air", 0xf4f0c4),
			FIRE = create("fire", 0xf2c66c),
			WATER = create("water", 0xa0e2cb),
			EARTH = create("earth", 0xb4f87e),
			ORDER = create("order", 0xd9d9d4),
			ENTROPY = create("entropy", 0x757377);
	
	public static final Aspect LIFE = create("life", WATER, EARTH, 0xd2243a);
	public static final Aspect LIGHT = create("light", AIR, FIRE, 0xffffff);
	public static final Aspect CRYSTAL = create("crystal", EARTH, ORDER, 0xbeedf6);
	public static final Aspect ENERGY = create("energy", FIRE, ORDER, 0xb22053);
	public static final Aspect ICE = create("ice", WATER, ORDER, 0xd3f6fb);
	public static final Aspect EXCHANGE = create("exchange", ENTROPY, ORDER, 0xeb9164);
	public static final Aspect MOVEMENT = create("movement", ENTROPY, AIR, 0xd0b9ac);
	
	public static final Aspect VOID = create("void", ENTROPY, LIGHT, 0x262036);
	public static final Aspect PLANT = create("plant", EARTH, LIFE, 0xeef88d);
	public static final Aspect METAL = create("metal", EARTH, CRYSTAL, 0x97939a);
	public static final Aspect SLIME = create("slime", WATER, LIFE, 0xc9f181);
	public static final Aspect TOOL = create("tool", ENERGY, EXCHANGE, 0x4c6497);
	
	public static final Aspect FLESH = create("flesh", PLANT, SLIME, 0xa9404d);
	public static final Aspect FABRIC = create("fabric", TOOL, PLANT, 0xae59b7);
	public static final Aspect JOURNEY = create("journey", MOVEMENT, ORDER, 0xd6b68a);
	public static final Aspect WEAPON = create("weapon", TOOL, FLESH, 0xd94023);
	public static final Aspect ARMOUR = create("armour", TOOL, LIFE, 0xa589cb);
	public static final Aspect MINING = create("mining", TOOL, METAL, 0xfaeaa8);
	
	public static final Aspect MIND = create("mind", FLESH, ENERGY, 0x74fab8);
	public static final Aspect SENSES = create("senses", FLESH, LIGHT, 0x73b0f2);
	public static final Aspect UNDEAD = create("undead", LIFE, ENTROPY, 0x6d8e37);
	public static final Aspect FLIGHT = create("flight", JOURNEY, AIR, 0xdee9e8);
	
	public static final Aspect MACHINE = create("machine", MIND, TOOL, 0x404e6f);
	public static final Aspect ENVY = create("envy", MIND, WEAPON, 0x6e2b71);
	public static final Aspect SLOTH = create("sloth", MIND, ICE, 0x3dcbcb);
	public static final Aspect GREED = create("greed", MIND, VOID, 0xf4f62d);
	public static final Aspect MAGIC = create("magic", SENSES, ENERGY, 0xe242b3);
	
	public static final Aspect TAINT = create("taint", MAGIC, ENTROPY, 0x903e8e);
	public static final Aspect AURA = create("aura", MAGIC, AIR, 0xa0e2cb);
	
	public static final List<Aspect> primals = List.of(AIR, FIRE, WATER, EARTH, ORDER, ENTROPY);
	public static final List<Aspect> hasCluster = List.of(AIR, FIRE, WATER, EARTH, ORDER, ENTROPY, AURA);
	
	public static Aspect byName(Identifier id){
		return aspects.get(id);
	}
	
	// defaults to the arcana namespace, since minecraft has no aspects
	public static Aspect byName(String name){
		return byName(Arcana.maybeArcId(name));
	}
	
	private static Aspect create(String name, int colour){
		return create(name, null, null, colour);
	}
	
	private static Aspect create(String name, Aspect left, Aspect right, int colour){
		Identifier id = arcId(name);
		Aspect aspect = new Aspect(id, left, right, colour);
		aspects.put(id, aspect);
		orderedAspects.add(aspect);
		return aspect;
	}
	
	public static List<Aspect> getCompoundAspects(){
		List<Aspect> aspects = new ArrayList<>(orderedAspects);
		aspects.removeAll(primals);
		return aspects;
	}
	
	public static Optional<Aspect> combined(Aspect left, Aspect right){
		for(Aspect value : aspects.values())
			if((left.equals(value.left()) && right.equals(value.right())) || (left.equals(value.right()) && right.equals(value.left())))
				return Optional.of(value);
		return Optional.empty();
	}
	
	public static List<Aspect> getOrderedAspects(){
		return orderedAspects;
	}
}