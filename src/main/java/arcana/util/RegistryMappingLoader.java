package arcana.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceReloadListenerKeys;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static arcana.Arcana.arcId;

public class RegistryMappingLoader<T> extends JsonDataLoader implements IdentifiableResourceReloadListener{
	
	private static final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	private static final Logger logger = LogUtils.getLogger();
	
	private final String subtype;
	private final RegistryMapping<T> mapping;
	
	public RegistryMappingLoader(String subtype, RegistryMapping<T> mapping){
		super(gson, "arcana/" + subtype);
		this.subtype = subtype;
		this.mapping = mapping;
	}
	
	public Identifier getFabricId(){
		return arcId(subtype);
	}
	
	public Collection<Identifier> getFabricDependencies(){
		return Set.of(ResourceReloadListenerKeys.TAGS);
	}
	
	protected void apply(Map<Identifier, JsonElement> prepared, ResourceManager manager, Profiler profiler){
		mapping.clear();
		prepared.forEach((filename, element) -> {
			if(!(element instanceof JsonObject obj)){
				logger.warn("Mapping at {} is not a JSON object, ignoring", filename);
				return;
			}
			
			for(String key : obj.keySet()){
				String to = obj.get(key).getAsString();
				Identifier toId = Identifier.tryParse(to);
				if(toId == null){
					logger.warn("Mapping at {} has invalid target identifier '{}', ignoring", filename, to);
					return;
				}
				
				boolean isTag = key.startsWith("#");
				Identifier fromId = Identifier.tryParse(isTag ? key.substring(1) : key);
				if(fromId == null){
					logger.warn("Mapping at {} has invalid source identifier '{}', ignoring", filename, key);
					return;
				}
				
				if(isTag){
					if(!mapping.addTagEntry(fromId, toId))
						logger.warn("Mapping at {} refers to nonexistent entries: \"#{}\": \"{}\"", filename, key, to);
				}else{
					if(!mapping.addIdentifierEntry(fromId, toId))
						logger.warn("Mapping at {} refers to nonexistent entries: \"{}\": \"{}\"", filename, key, to);
				}
			}
		});
	}
}