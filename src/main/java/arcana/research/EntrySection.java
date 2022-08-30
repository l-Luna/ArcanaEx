package arcana.research;

import arcana.research.sections.ImageSection;
import arcana.research.sections.TextSection;
import com.google.gson.JsonObject;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Identifier;

import java.util.*;
import java.util.function.Function;

import static arcana.util.StreamUtil.streamAndApply;

public abstract class EntrySection{
	
	// registry & sync
	
	private static final Map<Identifier, Function<JsonObject, EntrySection>> factories = new LinkedHashMap<>();
	private static final Map<Identifier, Function<NbtCompound, EntrySection>> deserializers = new LinkedHashMap<>();
	
	public static EntrySection makeSection(Identifier type, JsonObject contents){
		if(factories.containsKey(type))
			return factories.get(type).apply(contents);
		else return null;
	}
	
	public static EntrySection deserialize(NbtCompound passData){
		Identifier type = new Identifier(passData.getString("type"));
		NbtCompound data = passData.getCompound("data");
		List<Requirement> requirements = streamAndApply(
				passData.getList("requirements", NbtElement.COMPOUND_TYPE), NbtCompound.class,
				Requirement::deserialize).toList();
		if(deserializers.get(type) != null){
			EntrySection section = deserializers.get(type).apply(data);
			requirements.forEach(section::addRequirement);
			// receiving on client
			section.in = new Identifier(passData.getString("entry"));
			return section;
		}
		return null;
	}
	
	public static void setup(){
		factories.put(TextSection.TYPE, withContentsStr(TextSection::new));
		deserializers.put(TextSection.TYPE, nbt -> new TextSection(nbt.getString("text")));
//		factories.put(CraftingSection.TYPE, withContents(CraftingSection::new));
//		deserializers.put(CraftingSection.TYPE, nbt -> new CraftingSection(nbt.getString("recipe")));
//		factories.put(SmeltingSection.TYPE, withContents(SmeltingSection::new));
//		deserializers.put(SmeltingSection.TYPE, nbt -> new SmeltingSection(nbt.getString("recipe")));
//		factories.put(AlchemySection.TYPE, withContents(AlchemySection::new));
//		deserializers.put(AlchemySection.TYPE, nbt -> new AlchemySection(nbt.getString("recipe")));
//		factories.put(ArcaneCraftingSection.TYPE, withContents(ArcaneCraftingSection::new));
//		deserializers.put(ArcaneCraftingSection.TYPE, nbt -> new ArcaneCraftingSection(nbt.getString("recipe")));
		factories.put(ImageSection.TYPE, withContentsId(ImageSection::new));
		deserializers.put(ImageSection.TYPE, nbt -> new ImageSection(new Identifier(nbt.getString("image"))));
	}
	
	private static <T> Function<JsonObject, T> withContentsStr(Function<String, T> builder){
		return builder.compose(json -> json.getAsJsonPrimitive("content").getAsString());
	}
	
	private static <T> Function<JsonObject, T> withContentsId(Function<Identifier, T> builder){
		// chained composition only works with `(String s) -> new Identifier(s)`, uglier than just doing it manually
		return builder.compose(json -> new Identifier(json.getAsJsonPrimitive("content").getAsString()));
	}
	
	//
	
	protected List<Requirement> requirements = new ArrayList<>();
	protected Identifier in;
	
	public abstract Identifier type();
	public abstract NbtCompound data();
	
	public void addRequirement(Requirement requirement){
		requirements.add(requirement);
	}
	
	public List<Requirement> getRequirements(){
		return Collections.unmodifiableList(requirements);
	}
	
	public Identifier getIn(){
		return in;
	}
	
	public NbtCompound getPassData(){
		NbtCompound nbt = new NbtCompound();
		nbt.putString("type", type().toString());
		nbt.put("data", data());
		nbt.putString("entry", getIn().toString());
		
		NbtList list = new NbtList();
		getRequirements().forEach((requirement) -> list.add(requirement.getPassData()));
		nbt.put("requirements", list);
		
		return nbt;
	}
	
	// addOwnRequirements, getPins
}