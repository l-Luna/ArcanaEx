package arcana.research;

import com.google.gson.*;
import com.mojang.logging.LogUtils;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.item.Item;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import org.slf4j.Logger;

import java.util.*;

import static arcana.Arcana.arcId;

public final class ResearchLoader extends JsonDataLoader implements IdentifiableResourceReloadListener{
	
	private static final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	private static final Logger logger = LogUtils.getLogger();
	
	private static final Map<Identifier, JsonArray> bookQueue = new HashMap<>();
	private static final Map<Identifier, JsonArray> categoryQueue = new HashMap<>();
	private static final Map<Identifier, JsonArray> entryQueue = new HashMap<>();
	private static final Map<Identifier, JsonArray> puzzleQueue = new HashMap<>();
	
	public ResearchLoader(){
		super(gson, "arcana/research");
	}
	
	public Identifier getFabricId(){
		return arcId("research");
	}
	
	protected void apply(Map<Identifier, JsonElement> prepared, ResourceManager manager, Profiler profiler){
		bookQueue.clear();
		categoryQueue.clear();
		entryQueue.clear();
		puzzleQueue.clear();
		
		Research.books.clear();
		
		// collect book/category/entry/puzzle entries (which may be in any order) so they can be processed in correct order
		prepared.forEach((filename, json) -> {
			if(json.isJsonObject()){
				var object = json.getAsJsonObject();
				if(object.has("books"))
					bookQueue.put(filename, object.getAsJsonArray("books"));
				if(object.has("categories"))
					categoryQueue.put(filename, object.getAsJsonArray("categories"));
				if(object.has("entries"))
					entryQueue.put(filename, object.getAsJsonArray("entries"));
				if(object.has("puzzles"))
					puzzleQueue.put(filename, object.getAsJsonArray("puzzles"));
			}
		});
		
		bookQueue.forEach(ResearchLoader::applyBooks);
		categoryQueue.forEach(ResearchLoader::applyCategories);
		entryQueue.forEach(ResearchLoader::applyEntries);
		puzzleQueue.forEach(ResearchLoader::applyPuzzles);
	}
	
	private static void applyBooks(Identifier file, JsonArray books){
		for(JsonElement bookElement : books){
			if(!bookElement.isJsonObject())
				logger.warn("Non-object found in \"books\" array in research file \"" + file + "\", ignoring");
			else{
				JsonObject bookObj = bookElement.getAsJsonObject();
				// expecting key, prefix
				Identifier key = new Identifier(bookObj.get("key").getAsString());
				Research.books.put(key, new Book(key, new LinkedHashMap<>()));
				logger.info("Loaded book " + key);
			}
		}
	}
	
	private static void applyCategories(Identifier file, JsonArray categories){
		for(JsonElement categoryElement : categories){
			if(!categoryElement.isJsonObject())
				logger.warn("Non-object found in \"categories\" array in research file \"" + file + "\", ignoring");
			else{
				JsonObject categoryObj = categoryElement.getAsJsonObject();
				// expecting key, in, icon, bg, optionally bgs
				Identifier key = new Identifier(categoryObj.get("key").getAsString());
				Identifier bg = new Identifier(categoryObj.get("bg").getAsString());
				bg = new Identifier(bg.getNamespace(), "textures/" + bg.getPath());
				Icon icon = iconFromJson(categoryObj.get("icon"), file);
				String name = categoryObj.get("name").getAsString();
				Identifier requirement = categoryObj.has("requires") ? new Identifier(categoryObj.get("requires").getAsString()) : null;
				Book in = Research.books.get(new Identifier(categoryObj.get("in").getAsString()));
				Category category = new Category(key, new LinkedHashMap<>(), icon, bg, requirement, name, in);
				// TODO: background layers
				/*if(categoryObj.has("bgs")){
					JsonArray layers = categoryObj.getAsJsonArray("bgs");
					for(JsonElement layerElem : layers){
						JsonObject layerObj = layerElem.getAsJsonObject();
						BackgroundLayer layer = BackgroundLayer.makeLayer(
								new Identifier(layerObj.getAsJsonPrimitive("type").getAsString()),
								layerObj,
								file,
								layerObj.getAsJsonPrimitive("speed").getAsFloat(),
								layerObj.has("vanishZoom") ? layerObj.getAsJsonPrimitive("vanishZoom").getAsFloat() : -1);
						if(layer != null)
							category.getBgs().add(layer);
					}
				}*/
				in.categories().putIfAbsent(key, category);
			}
		}
	}
	
	private static void applyEntries(Identifier file, JsonArray entries){
		for(JsonElement entryElement : entries){
			if(!entryElement.isJsonObject())
				logger.warn("Non-object found in \"entries\" array in research file \"" + file + "\", ignoring");
			else{
				JsonObject entry = entryElement.getAsJsonObject();
				
				// expecting key, name, desc, icons, category, x, y, sections
				Identifier key = new Identifier(entry.get("key").getAsString());
				String name = entry.get("name").getAsString();
				String desc = entry.has("desc") ? entry.get("desc").getAsString() : "";
				List<Icon> icons = iconsFromJsonArray(entry.getAsJsonArray("icons"), file);
				Category category = Research.getCategory(new Identifier(entry.get("category").getAsString()));
				int x = entry.get("x").getAsInt();
				int y = entry.get("y").getAsInt();
				List<EntrySection> sections = jsonToSections(entry.getAsJsonArray("sections"), file);
				
				// optionally parents, meta
				List<Parent> parents = new ArrayList<>();
				if(entry.has("parents")){
					List<Parent> list = new ArrayList<>();
					for(JsonElement element : entry.getAsJsonArray("parents"))
						list.add(Parent.parse(element.getAsString()));
					parents = list;
				}
				
				List<String> meta = new ArrayList<>();
				if(entry.has("meta")){
					List<String> list = new ArrayList<>();
					for(JsonElement element : entry.getAsJsonArray("meta"))
						list.add(element.getAsString());
					meta = list;
				}
				
				Entry entryObject = new Entry(key, category, name, desc, sections, parents, icons, meta, x, y);
				category.entries().putIfAbsent(key, entryObject);
				sections.forEach(section -> section.in = entryObject.id());
			}
		}
	}
	
	private static void applyPuzzles(Identifier identifier, JsonArray puzzles){
		// TODO: puzzles
	}
	
	private static List<EntrySection> jsonToSections(JsonArray sections, Identifier file){
		// TODO: sections
		return List.of();
	}
	
	private static List<Icon> iconsFromJsonArray(JsonArray array, Identifier file){
		List<Icon> ret = new ArrayList<>(array.size());
		for(JsonElement element : array)
			ret.add(iconFromJson(element, file));
		return ret;
	}
	
	private static Icon iconFromJson(JsonElement json, Identifier file){
		// TODO: item stacks with NBT...
		if(!json.isJsonPrimitive()){
			logger.error("A research icon in file \"" + file + "\" is not a string, but may be required");
			return new Icon(null, null);
		}
		Identifier asId = new Identifier(json.getAsString());
		RegistryKey<Item> asKey = RegistryKey.of(Registry.ITEM_KEY, asId);
		if(Registry.ITEM.contains(asKey))
			return new Icon(Registry.ITEM.get(asKey));
		return new Icon(asId);
	}
}