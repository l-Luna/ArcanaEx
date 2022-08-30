package arcana.research.sections;

import arcana.research.EntrySection;
import arcana.util.NbtUtil;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

import java.util.Map;

import static arcana.Arcana.arcId;

public class ImageSection extends EntrySection{
	
	public static final Identifier TYPE = arcId("image");
	
	private final Identifier image;
	
	public ImageSection(Identifier image){
		this.image = image;
	}
	
	public Identifier getImage(){
		return image;
	}
	
	public Identifier type(){
		return TYPE;
	}
	
	public NbtCompound data(){
		return NbtUtil.from(Map.of("image", getImage()));
	}
}