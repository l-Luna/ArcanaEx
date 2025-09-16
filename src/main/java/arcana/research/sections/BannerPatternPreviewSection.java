package arcana.research.sections;

import arcana.research.EntrySection;
import arcana.util.NbtUtil;
import com.google.gson.JsonObject;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.Map;

import static arcana.Arcana.arcId;

// TODO: use tag from item, multi-page span
public class BannerPatternPreviewSection extends EntrySection{
	
	public static final Identifier TYPE = arcId("banner_pattern_preview");
	
	protected final Item patternItem;
	protected final Identifier pattern;
	
	public BannerPatternPreviewSection(Item patternItem, Identifier pattern){
		this.patternItem = patternItem;
		this.pattern = pattern;
	}
	
	public BannerPatternPreviewSection(JsonObject obj){
		this(
				Registry.ITEM.get(new Identifier(obj.get("pattern_item").getAsString())),
				new Identifier(obj.get("pattern").getAsString())
		);
	}
	
	public BannerPatternPreviewSection(NbtCompound compound){
		this(
				Registry.ITEM.get(new Identifier(compound.getString("pattern_item"))),
				new Identifier(compound.getString("pattern"))
		);
	}
	
	public Item getPatternItem(){
		return patternItem;
	}
	
	public Identifier getPattern(){
		return pattern;
	}
	
	public Identifier type(){
		return TYPE;
	}
	
	public NbtCompound data(){
		return NbtUtil.from(Map.of("pattern_item", Registry.ITEM.getId(patternItem), "pattern", pattern));
	}
}