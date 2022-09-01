package arcana.research.sections;

import arcana.research.Entry;
import arcana.research.EntrySection;
import arcana.research.Icon;
import arcana.research.Pin;
import arcana.util.NbtUtil;
import com.google.gson.JsonObject;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
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
				Registry.ITEM.get(new Identifier(obj.get("input").getAsString())),
				Registry.ITEM.get(new Identifier(obj.get("result").getAsString()))
		);
	}
	
	public WandInteractionSection(NbtCompound compound){
		this(
				Registry.ITEM.get(new Identifier(compound.getString("input"))),
				Registry.ITEM.get(new Identifier(compound.getString("result")))
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
		return NbtUtil.from(Map.of("input", Registry.ITEM.getId(input), "result", Registry.ITEM.getId(result)));
	}
}