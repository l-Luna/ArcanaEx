package arcana.research.puzzles;

import arcana.aspects.Aspect;
import arcana.aspects.Aspects;
import arcana.research.Puzzle;
import arcana.util.StreamUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import java.util.ArrayList;
import java.util.List;

import static arcana.Arcana.arcId;

public class Chemistry extends Puzzle{

	public static final Identifier TYPE = arcId("chemistry");
	
	private final List<Aspect> nodes;
	private final int size, flux;
	
	public Chemistry(List<Aspect> nodes, int size, int flux){
		this.nodes = nodes;
		this.size = size;
		this.flux = flux;
	}
	
	public Chemistry(NbtCompound data){
		nodes = StreamUtil.streamAndApply(
				data.getList("nodes", NbtElement.STRING_TYPE), NbtString.class,
				x -> Aspects.byName(x.asString())).toList();
		size = data.getInt("size");
		flux = data.getInt("flux");
	}
	
	public Chemistry(JsonObject obj){
		nodes = new ArrayList<>();
		for(JsonElement nodeElem : obj.getAsJsonArray("nodes"))
			nodes.add(Aspects.byName(nodeElem.getAsString()));
		
		size = JsonHelper.getInt(obj, "size", 3);
		flux = JsonHelper.getInt(obj, "flux", 0);
	}
	
	public Identifier type(){
		return TYPE;
	}
	
	public NbtCompound data(){
		NbtCompound data = new NbtCompound();
		
		NbtList nodeList = new NbtList();
		for(Aspect node : nodes)
			nodeList.add(NbtString.of(node.id().toString()));
		data.put("nodes", nodeList);
		
		data.putInt("size", size);
		data.putInt("flux", flux);
		
		return data;
	}
}