package arcana.research.sections;

import arcana.research.EntrySection;
import arcana.util.NbtUtil;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.Objects;

import static arcana.Arcana.arcId;

public class TextSection extends EntrySection{
	
	public static final Identifier TYPE = arcId("text");
	
	private final String text;
	
	public TextSection(String text){
		this.text = text;
	}
	
	public String getText(){
		return text;
	}
	
	public Identifier type(){
		return TYPE;
	}
	
	public NbtCompound data(){
		return NbtUtil.from(Map.of("text", text));
	}
	
	// for TextSectionRenderer#textCache
	
	public boolean equals(Object obj){
		return obj instanceof TextSection section && section.text.equals(text);
	}
	
	public int hashCode(){
		return Objects.hash(text);
	}
}