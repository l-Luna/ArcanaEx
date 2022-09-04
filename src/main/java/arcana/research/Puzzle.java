package arcana.research;

import arcana.Arcana;
import arcana.research.puzzles.Chemistry;
import arcana.research.puzzles.Fieldwork;
import arcana.util.NbtUtil;
import com.google.gson.JsonObject;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public abstract class Puzzle{
	
	// registry & sync
	
	private static final Map<Identifier, Function<JsonObject, Puzzle>> factories = new HashMap<>();
	private static final Map<Identifier, Function<NbtCompound, Puzzle>> deserializers = new HashMap<>();
	
	public static Puzzle makePuzzle(JsonObject contents){
		return makePuzzle(Arcana.maybeArcId(contents.get("type").getAsString()), contents);
	}
	
	public static Puzzle makePuzzle(Identifier type, JsonObject contents){
		if(factories.containsKey(type)){
			var puzzle = factories.get(type).apply(contents);
			puzzle.id = new Identifier(contents.get("key").getAsString());
			puzzle.desc = JsonHelper.getString(contents, "desc", "");
			return puzzle;
		}else
			return null;
	}
	
	public static Puzzle deserialize(NbtCompound passData){
		Identifier id = new Identifier(passData.getString("id"));
		Identifier type = new Identifier(passData.getString("type"));
		NbtCompound data = passData.getCompound("data");
		String desc = passData.getString("desc");
		if(deserializers.containsKey(type)){
			var puzzle = deserializers.get(type).apply(data);
			puzzle.id = id;
			puzzle.desc = desc;
			return puzzle;
		}
		return null;
	}
	
	public static boolean exists(Identifier type){
		return factories.containsKey(type);
	}
	
	public static void setup(){
		factories.put(Chemistry.TYPE, Chemistry::new);
		deserializers.put(Chemistry.TYPE, Chemistry::new);
		
		factories.put(Fieldwork.TYPE, __ -> new Fieldwork());
		deserializers.put(Fieldwork.TYPE, __ -> new Fieldwork());
	}
	
	//
	
	protected Identifier id = null;
	protected String desc = "";
	
	public abstract Identifier type();
	public abstract NbtCompound data();
	
	public Identifier id(){
		return id;
	}
	
	public String desc(){
		return desc;
	}
	
	public NbtCompound getPassData(){
		return NbtUtil.from(Map.of("type", type(), "data", data(), "id", id().toString(), "desc", desc()));
	}
	
	public NbtCompound getInitialNoteTag(){
		return new NbtCompound();
	}
}