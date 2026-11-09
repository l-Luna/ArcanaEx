package arcana.research.sections;

import arcana.items.foci.follow_me.Locator;
import arcana.research.Entry;
import arcana.research.EntrySection;
import arcana.research.Icon;
import arcana.research.Pin;
import arcana.util.NbtUtil;
import com.google.gson.JsonObject;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static arcana.Arcana.arcId;

public class LocatorsSection extends EntrySection{
	
	private static final Identifier PIN_TEXTURE = arcId("textures/gui/research/locators_list.png");
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
	
	public Stream<Pin> pins(int idx, World world, Entry entry){
		return Stream.of(new Pin(new Icon(PIN_TEXTURE), entry, idx, null, List.of(Text.translatable(tag.getTranslationKey()))));
	}
}