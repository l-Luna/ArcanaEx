package arcana.datagen;

import arcana.aspects.Aspect;
import arcana.aspects.AspectMap;
import arcana.aspects.AspectStack;
import arcana.aspects.Aspects;
import arcana.items.CrystalItem;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.item.Item;
import net.minecraft.util.registry.Registry;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static arcana.Arcana.arcId;

// tfw aspect crystals
public class AspectsProvider implements DataProvider{
	
	private final DataGenerator.PathResolver aspectsResolver;
	private final Map<Item, AspectMap> aspects = new HashMap<>();
	
	public AspectsProvider(DataGenerator gen){
		aspectsResolver = gen.createPathResolver(DataGenerator.OutputType.DATA_PACK, "arcana/aspects");
	}
	
	public final void run(DataWriter writer) throws IOException{
		generateAspects();
		writeJsons(writer);
	}
	
	private void writeJsons(DataWriter writer) throws IOException{
		JsonObject obj = new JsonObject();
		aspects.forEach((item, map) -> {
			JsonArray arr = new JsonArray();
			for(AspectStack stack : map.asStacks())
				if(stack.amount() == 1)
					arr.add(stack.type().id().toString());
				else
					arr.add(stack.amount() + "*" + stack.type().id().toString());
			obj.add(Registry.ITEM.getId(item).toString(), arr);
		});
		DataProvider.writeToPath(writer, obj, aspectsResolver.resolveJson(arcId("generated")));
	}
	
	public void generateAspects(){
		for(CrystalItem crystal : Aspects.crystals.values())
			assign(crystal, crystal.getAspect());
	}
	
	public final void assign(Item item, AspectMap aspects){
		this.aspects.put(item, aspects);
	}
	
	public final void assign(Item item, Aspect aspect){
		assign(item, AspectMap.fromAspectStack(new AspectStack(aspect, 1)));
	}
	
	public String getName(){
		return "Arcana Aspects";
	}
}