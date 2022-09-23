package arcana.research;

import arcana.research.sections.*;
import com.google.gson.JsonObject;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

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
	
	public static boolean exists(Identifier type){
		return factories.containsKey(type);
	}
	
	public static void setup(){
		factories.put(TextSection.TYPE, withContentsStr(TextSection::new));
		deserializers.put(TextSection.TYPE, nbt -> new TextSection(nbt.getString("text")));
		
		factories.put(ImageSection.TYPE, withContentsId(ImageSection::new));
		deserializers.put(ImageSection.TYPE, nbt -> new ImageSection(new Identifier(nbt.getString("image"))));
		
		factories.put(CraftingRecipeSection.TYPE, withContentsId(CraftingRecipeSection::new));
		deserializers.put(CraftingRecipeSection.TYPE, nbt -> new CraftingRecipeSection(new Identifier(nbt.getString("recipe"))));
		
		factories.put(ArcaneCraftingRecipeSection.TYPE, withContentsId(ArcaneCraftingRecipeSection::new));
		deserializers.put(ArcaneCraftingRecipeSection.TYPE, nbt -> new ArcaneCraftingRecipeSection(new Identifier(nbt.getString("recipe"))));
		
		factories.put(CookingRecipeSection.TYPE, withContentsId(CookingRecipeSection::new));
		deserializers.put(CookingRecipeSection.TYPE, nbt -> new CookingRecipeSection(new Identifier(nbt.getString("recipe"))));
		
		factories.put(AlchemyRecipeSection.TYPE, withContentsId(AlchemyRecipeSection::new));
		deserializers.put(AlchemyRecipeSection.TYPE, nbt -> new AlchemyRecipeSection(new Identifier(nbt.getString("recipe"))));
		
		factories.put(InfusionRecipeSection.TYPE, withContentsId(InfusionRecipeSection::new));
		deserializers.put(InfusionRecipeSection.TYPE, nbt -> new InfusionRecipeSection(new Identifier(nbt.getString("recipe"))));
		
		factories.put(WandInteractionSection.TYPE, WandInteractionSection::new);
		deserializers.put(WandInteractionSection.TYPE, WandInteractionSection::new);
		
		factories.put(AspectCombosSection.TYPE, __ -> new AspectCombosSection());
		deserializers.put(AspectCombosSection.TYPE, __ -> new AspectCombosSection());
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
	
	public Stream<Pin> pins(int idx, World world, Entry entry){
		return Stream.empty();
	}
}