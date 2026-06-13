package arcana.research.sections;

import arcana.research.Entry;
import arcana.research.EntrySection;
import arcana.research.Icon;
import arcana.research.Pin;
import arcana.util.NbtUtil;
import com.google.gson.JsonObject;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.Map;
import java.util.stream.Stream;

import static arcana.Arcana.arcId;

public class WandInteractionSection extends EntrySection{
	
	public static final Identifier TYPE = arcId("wand_interaction");
	
	// TODO: handle multiblocks, tags
	
	private final Item input, result;
	
	public WandInteractionSection(Item input, Item result){
		this.input = input;
		this.result = result;
	}
	
	public WandInteractionSection(JsonObject obj){
		this(
				Registries.ITEM.get(Identifier.of(obj.get("input").getAsString())),
				Registries.ITEM.get(Identifier.of(obj.get("result").getAsString()))
		);
	}
	
	public WandInteractionSection(NbtCompound compound){
		this(
				Registries.ITEM.get(Identifier.of(compound.getString("input"))),
				Registries.ITEM.get(Identifier.of(compound.getString("result")))
		);
	}
	
	public Item getInput(){
		return input;
	}
	
	public Item getResult(){
		return result;
	}
	
	public Stream<Pin> pins(int idx, World world, Entry entry){
		return Stream.of(new Pin(new Icon(result), entry, idx, result));
	}
	
	public Identifier type(){
		return TYPE;
	}
	
	public NbtCompound data(){
		return NbtUtil.from(Map.of("input", Registries.ITEM.getId(input), "result", Registries.ITEM.getId(result)));
	}
}