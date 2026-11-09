package arcana.research.sections;

import arcana.items.foci.follow_me.Locator;
import arcana.research.EntrySection;
import arcana.util.NbtUtil;
import com.google.gson.JsonObject;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import java.util.Map;

import static arcana.Arcana.arcId;

public class LocatorsSection extends EntrySection{
	
	public static final Identifier TYPE = arcId("locators");
	
	private final TagKey<Locator> tag;
	
	public LocatorsSection(JsonObject obj){
		this(Identifier.of(obj.get("tag").getAsString()));
	}
	
	public LocatorsSection(NbtCompound compound){
		this(Identifier.of(compound.getString("tag")));
	}
	
	public LocatorsSection(Identifier tag){
		this.tag = TagKey.of(Locator.REGISTRY_KEY, tag);
	}
	
	public TagKey<Locator> getTag(){
		return tag;
	}
	
	public Identifier type(){
		return TYPE;
	}
	
	public NbtCompound data(){
		return NbtUtil.from(Map.of("tag", getTag().id()));
	}
}