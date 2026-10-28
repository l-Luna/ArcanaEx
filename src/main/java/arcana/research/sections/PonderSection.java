package arcana.research.sections;

import arcana.research.EntrySection;
import arcana.util.NbtUtil;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

import java.util.Map;

import static arcana.Arcana.arcId;

public class PonderSection extends EntrySection{
	
	public static final Identifier TYPE = arcId("ponder");
	
	private final Identifier scene;
	
	public PonderSection(Identifier scene){
		this.scene = scene;
	}
	
	public Identifier getScene(){
		return scene;
	}
	
	public Identifier type(){
		return TYPE;
	}
	
	public NbtCompound data(){
		return NbtUtil.from(Map.of("scene", scene));
	}
}