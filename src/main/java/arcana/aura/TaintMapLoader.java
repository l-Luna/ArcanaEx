package arcana.aura;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceReloadListenerKeys;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.Registry;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

import static arcana.Arcana.arcId;

public class TaintMapLoader extends JsonDataLoader implements IdentifiableResourceReloadListener{
	
	private static final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	private static final Logger logger = LogUtils.getLogger();
	
	public TaintMapLoader(){
		super(gson, "arcana/taint_maps");
	}
	
	public Identifier getFabricId(){
		return arcId("taint_maps");
	}
	
	public Collection<Identifier> getFabricDependencies(){
		return Set.of(ResourceReloadListenerKeys.TAGS);
	}
	
	protected void apply(Map<Identifier, JsonElement> prepared, ResourceManager manager, Profiler profiler){
		Taint.resetMappings();
		prepared.forEach((filename, elem) -> {
			if(!(elem instanceof JsonObject obj)){
				logger.warn("Taint mapping at {} is not a JSON object, ignoring", filename);
				return;
			}
			if(obj.has("tainting"))
				if(obj.get("tainting") instanceof JsonObject m)
					applyMapping(filename, m, Taint.TAINT_MAP::put, (k, v) -> Taint.TAINT_TAGS.add(new Pair<>(k, v)));
				else
					logger.warn("Taint mapping at {} has non-object 'tainting' key, ignoring", filename);
			if(obj.has("untainting"))
				if(obj.get("untainting") instanceof JsonObject m)
					applyMapping(filename, m, Taint.UNTAINT_MAP::put, (k, v) -> Taint.UNTAINT_TAGS.add(new Pair<>(k, v)));
				else
					logger.warn("Taint mapping at {} has non-object 'untainting' key, ignoring", filename);
		});
		// automatically add untainting for blocks that don't have explicit entries or tags
		Taint.TAINT_MAP.forEach((untainted, tainted) -> {
			if(!Taint.UNTAINT_MAP.containsKey(tainted) && Taint.UNTAINT_TAGS.stream().noneMatch(x -> tainted.getRegistryEntry().isIn(x.getLeft())))
				Taint.UNTAINT_MAP.put(tainted, untainted);
		});
	}
	
	protected void applyMapping(Identifier filename, JsonObject mapping, BiConsumer<Block, Block> blockMapper, BiConsumer<TagKey<Block>, Block> tagMapper){
		for(String keyName : mapping.keySet()){
			String outputName = mapping.get(keyName).getAsString();
			Identifier outputId = Identifier.tryParse(outputName);
			if(outputId == null){
				logger.warn("Taint mapping at {} has invalid identifier '{}', ignoring", filename, outputName);
				return;
			}
			Block output = Registry.BLOCK.get(outputId);
			if(output == Blocks.AIR){
				logger.warn("Taint mapping at {} has invalid block ID '{}', ignoring", filename, outputName);
				return;
			}
			
			if(keyName.startsWith("#")){
				Identifier inId = Identifier.tryParse(keyName.substring(1));
				if(inId == null)
					logger.warn("Taint mapping at {} has invalid identifier '{}', ignoring", filename, keyName);
				tagMapper.accept(TagKey.of(Registry.BLOCK_KEY, inId), output);
			}else{
				Identifier inId = Identifier.tryParse(keyName);
				if(inId == null)
					logger.warn("Taint mapping at {} has invalid identifier '{}', ignoring", filename, keyName);
				Block input = Registry.BLOCK.get(inId);
				if(input == Blocks.AIR){
					logger.warn("Taint mapping at {} has invalid block ID '{}', ignoring", filename, keyName);
					return;
				}
				blockMapper.accept(input, output);
			}
		}
	}
}