package arcana.datagen;

import arcana.aspects.Aspect;
import arcana.aspects.AspectMap;
import arcana.aspects.AspectStack;
import arcana.aspects.Aspects;
import arcana.blocks.CrystalClusterBlock;
import arcana.items.CrystalItem;
import arcana.items.PhialItem;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.NotNull;

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
		
		for(PhialItem phial : Aspects.phials.values())
			if(phial.getAspect() != null)
				assign(phial, phial.getAspect(), 8);
		
		for(CrystalClusterBlock cluster : Aspects.clusters.values())
			assign(cluster, cluster.getAspect(), 4);
	}
	
	public final void assign(ItemConvertible item, AspectMap aspects){
		this.aspects.put(item.asItem(), aspects);
	}
	
	public final void assign(ItemConvertible item, @NotNull Aspect aspect){
		assign(item, AspectMap.fromAspectStack(new AspectStack(aspect, 1)));
	}
	
	public final void assign(ItemConvertible item, @NotNull Aspect aspect, int amount){
		assign(item, AspectMap.fromAspectStack(new AspectStack(aspect, amount)));
	}
	
	public String getName(){
		return "Arcana Aspects";
	}
}