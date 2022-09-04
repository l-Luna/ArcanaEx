package arcana.research;

import arcana.Arcana;
import arcana.research.requirements.ItemRequirement;
import arcana.research.requirements.ItemTagRequirement;
import arcana.research.requirements.PuzzleRequirement;
import arcana.research.requirements.XpRequirement;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public abstract class Requirement{
	
	// registry & sync
	
	private static final Map<Identifier, Function<List<String>, Requirement>> factories = new LinkedHashMap<>();
	private static final Map<Identifier, Function<NbtCompound, Requirement>> deserializers = new LinkedHashMap<>();
	
	public static Requirement makeRequirement(Identifier type, List<String> content){
		if(factories.containsKey(type))
			return factories.get(type).apply(content);
		else return null;
	}
	
	public static Requirement deserialize(NbtCompound passData){
		Identifier type = new Identifier(passData.getString("type"));
		NbtCompound data = passData.getCompound("data");
		int amount = passData.getInt("amount");
		if(deserializers.get(type) != null){
			Requirement requirement = deserializers.get(type).apply(data);
			requirement.amount = amount;
			return requirement;
		}
		return null;
	}
	
	public static void setup(){
		// item (tag) requirement construction is handled by ResearchLoader
		deserializers.put(ItemRequirement.TYPE, compound -> new ItemRequirement(Registry.ITEM.get(new Identifier(compound.getString("item")))));
		deserializers.put(ItemTagRequirement.TYPE, compound -> new ItemTagRequirement(new Identifier(compound.getString("tag"))));
		
		factories.put(XpRequirement.TYPE, __ -> new XpRequirement());
		deserializers.put(XpRequirement.TYPE, __ -> new XpRequirement());
		
		factories.put(PuzzleRequirement.TYPE, args -> new PuzzleRequirement(Arcana.maybeArcId(args.get(0))));
		deserializers.put(PuzzleRequirement.TYPE, compound -> new PuzzleRequirement(new Identifier(compound.getString("puzzle"))));
	}
	
	//
	
	protected int amount;
	
	public abstract boolean satisfiedBy(PlayerEntity player);
	public abstract void takeFrom(PlayerEntity player);
	
	public abstract Identifier type();
	public abstract NbtCompound data();
	
	/** Called on the client side. */
	public boolean onClick(Entry entry, PlayerEntity player){
		return false;
	}
	
	public NbtCompound getPassData(){
		NbtCompound nbt = new NbtCompound();
		nbt.putString("type", type().toString());
		nbt.put("data", data());
		nbt.putInt("amount", getAmount());
		return nbt;
	}
	
	public int getAmount(){
		return amount;
	}
	
	public Requirement setAmount(int amount){
		this.amount = amount;
		return this;
	}
}