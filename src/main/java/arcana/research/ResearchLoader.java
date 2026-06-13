package arcana.research;

import arcana.Arcana;
import arcana.recipes.XIngredient;
import arcana.research.requirements.ItemRequirement;
import arcana.research.requirements.ItemTagRequirement;
import com.google.gson.*;
import com.mojang.logging.LogUtils;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
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
	
	public void apply(Map<Identifier, JsonElement> prepared, ResourceManager _rm, Profiler _pr){
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
				logger.warn("Non-object found in \"books\" array in research file \"{}\", ignoring", file);
			else{
				JsonObject bookObj = bookElement.getAsJsonObject();
				// expecting key, prefix
				Identifier key = Identifier.of(bookObj.get("key").getAsString());
				Research.books.put(key, new Book(key, new ArrayList<>()));
				logger.info("Loaded book {}", key);
			}
		}
	}
	
	private static void applyCategories(Identifier file, JsonArray categories){
		for(JsonElement categoryElement : categories){
			if(!categoryElement.isJsonObject())
				logger.warn("Non-object found in \"categories\" array in research file \"{}\", ignoring", file);
			else{
				JsonObject categoryObj = categoryElement.getAsJsonObject();
				// expecting key, in, icon, bg, optionally bgs
				Identifier key = Identifier.of(categoryObj.get("key").getAsString());
				Identifier bg = Identifier.of(categoryObj.get("bg").getAsString());
				bg = Identifier.of(bg.getNamespace(), "textures/" + bg.getPath());
				Icon icon = iconFromJson(categoryObj.get("icon"), file);
				String name = categoryObj.get("name").getAsString();
				Identifier requirement = categoryObj.has("requires") ? Identifier.of(categoryObj.get("requires").getAsString()) : null;
				Book in = Research.books.get(Identifier.of(categoryObj.get("in").getAsString()));
				Category category = new Category(key, new ArrayList<>(), icon, bg, requirement, name, in);
				// TODO: background layers
				/*if(categoryObj.has("bgs")){
					JsonArray layers = categoryObj.getAsJsonArray("bgs");
					for(JsonElement layerElem : layers){
						JsonObject layerObj = layerElem.getAsJsonObject();
						BackgroundLayer layer = BackgroundLayer.makeLayer(
								Identifier.of(layerObj.getAsJsonPrimitive("type").getAsString()),
								layerObj,
								file,
								layerObj.getAsJsonPrimitive("speed").getAsFloat(),
								layerObj.has("vanishZoom") ? layerObj.getAsJsonPrimitive("vanishZoom").getAsFloat() : -1);
						if(layer != null)
							category.getBgs().add(layer);
					}
				}*/
				in.categories().add(category);
			}
		}
	}
	
	private static void applyEntries(Identifier file, JsonArray entries){
		for(JsonElement entryElement : entries){
			if(!entryElement.isJsonObject())
				logger.warn("Non-object found in \"entries\" array in research file \"{}\", ignoring", file);
			else{
				JsonObject entry = entryElement.getAsJsonObject();
				
				// expecting key, name, desc, icons, category, x, y, sections
				Identifier key = Identifier.of(entry.get("key").getAsString());
				String name = entry.get("name").getAsString();
				String desc = entry.has("desc") ? entry.get("desc").getAsString() : "";
				List<Icon> icons = iconsFromJsonArray(entry.getAsJsonArray("icons"), file);
				Category category = Research.getCategory(Identifier.of(entry.get("category").getAsString()));
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
				
				List<Addendum> addenda = new ArrayList<>();
				Entry entryObject = new Entry(key, category, name, desc, sections, parents, icons, meta, addenda, x, y);
				
				if(entry.has("addenda")){
					for(JsonElement element : entry.getAsJsonArray("addenda")){
						// TODO: update logging
						JsonObject eObj = element.getAsJsonObject();
						Identifier adKey = Identifier.of(eObj.get("key").getAsString());
						String adName = eObj.get("name").getAsString();
						List<EntrySection> adSections = jsonToSections(eObj.getAsJsonArray("sections"), file);
						List<Requirement> adAutoUnlockReqs = eObj.has("auto_unlock_reqs")
								? jsonToRequirements(eObj.get("auto_unlock_reqs").getAsJsonArray(), file)
								: List.of();
						addenda.add(new Addendum(entryObject, adKey, adName, adSections, adAutoUnlockReqs));
						
						for(EntrySection section : adSections)
							section.in = entryObject.id();
					}
				}
				
				category.entries().add(entryObject);
				sections.forEach(section -> section.in = entryObject.id());
			}
		}
	}
	
	private static void applyPuzzles(Identifier file, JsonArray puzzles){
		for(JsonElement puzzleElement : puzzles){
			if(!puzzleElement.isJsonObject())
				logger.warn("Non-object found in \"puzzles\" array in research file \"{}\", ignoring", file);
			else{
				Puzzle puzzle = Puzzle.makePuzzle(puzzleElement.getAsJsonObject());
				Research.puzzles.put(puzzle.id(), puzzle);
			}
		}
	}
	
	private static List<EntrySection> jsonToSections(JsonArray sections, Identifier file){
		// TODO: update logging
		List<EntrySection> ret = new ArrayList<>();
		for(JsonElement sectionElement : sections)
			if(sectionElement.isJsonObject()){
				JsonObject section = sectionElement.getAsJsonObject();
				String type = section.get("type").getAsString();
				var typeId = Arcana.maybeArcId(type);
				EntrySection es = EntrySection.makeSection(typeId, section);
				if(es != null){
					if(section.has("requirements"))
						if(section.get("requirements").isJsonArray()){
							for(Requirement requirement : jsonToRequirements(section.get("requirements").getAsJsonArray(), file))
								if(requirement != null)
									es.addRequirement(requirement);
						}else
							logger.warn("Non-array named \"requirements\" found in {}", file);
					ret.add(es);
				}else if(!EntrySection.exists(typeId))
					logger.warn("Invalid EntrySection type \"{}\" referenced in {}", type, file);
				else
					logger.warn("Invalid EntrySection content for type \"{}\" used in file {}", type, file);
			}else
				logger.warn("Non-object found in sections array in {}", file);
		return ret;
	}
	
	private static List<Requirement> jsonToRequirements(JsonArray requirements, Identifier file){
		List<Requirement> ret = new ArrayList<>();
		for(JsonElement requirementElement : requirements){
			if(requirementElement.isJsonPrimitive()){
				String desc = requirementElement.getAsString();
				int amount = 1;
				// if it has * in it, then its amount is not one
				if(desc.contains("*")){
					String[] parts = desc.split("\\*");
					if(parts.length != 2)
						logger.warn("Multiple \"*\"s found in requirement in {}", file);
					desc = parts[parts.length - 1];
					amount = Integer.parseInt(parts[0]);
				}
				List<String> params = new ArrayList<>();
				// document this better.
				// If this has a "{" it has parameters; remove those
				if(desc.contains("{") && desc.endsWith("}")){
					String[] param_parts = desc.split("\\{", 2);
					desc = param_parts[0];
					params = Arrays.asList(param_parts[1].substring(0, param_parts[1].length() - 1).split(", *"));
				}
				// If this has "::" it's a custom requirement
				if(desc.contains("::")){
					String[] parts = desc.split("::");
					if(parts.length != 2)
						logger.warn("Multiple \"::\"s found in requirement in {}", file);
					Identifier type = Identifier.of(parts[0], parts[1]);
					Requirement add = Requirement.makeRequirement(type, params);
					if(add != null){
						add.amount = amount;
						ret.add(add);
					}else
						logger.warn("Invalid requirement type {} found in file {}", type, file);
					// if this begins with a hash
				}else if(desc.startsWith("#")){
					// it's a tag
					ItemTagRequirement tagReq = new ItemTagRequirement(Identifier.of(desc.substring(1)));
					tagReq.amount = amount;
					ret.add(tagReq);
				}else{
					// it's an item
					// optional matcher section: `minecraft:wooden_sword & has_enchantment minecraft:sharpness`
					XIngredient.StackMatcher matcher = XIngredient.AnyMatcher.INSTANCE;
					if(desc.contains("&")){
						var split = desc.split("&", 2);
						desc = split[0].trim();
						matcher = XIngredient.matcherFromString(split[1].trim());
					}
					Item item = Registries.ITEM.get(Identifier.of(desc));
					ItemRequirement itemReq = new ItemRequirement(item, matcher);
					itemReq.amount = amount;
					ret.add(itemReq);
				}
			}
		}
		return ret;
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
			logger.error("A research icon in file \"{}\" is not a string, but may be required", file);
			return new Icon(null, null);
		}
		Identifier asId = Identifier.of(json.getAsString());
		RegistryKey<Item> asKey = RegistryKey.of(RegistryKeys.ITEM, asId);
		if(Registries.ITEM.contains(asKey))
			return new Icon(Registries.ITEM.get(asKey));
		return new Icon(asId);
	}
}