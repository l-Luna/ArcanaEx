package arcana.research.sections;

import arcana.research.EntrySection;
import arcana.util.NbtUtil;
import com.google.gson.JsonObject;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

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
				Registries.ITEM.get(Identifier.of(obj.get("pattern_item").getAsString())),
				Identifier.of(obj.get("pattern").getAsString())
		);
	}
	
	public BannerPatternPreviewSection(NbtCompound compound){
		this(
				Registries.ITEM.get(Identifier.of(compound.getString("pattern_item"))),
				Identifier.of(compound.getString("pattern"))
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
		return NbtUtil.from(Map.of("pattern_item", Registries.ITEM.getId(patternItem), "pattern", pattern));
	}
}