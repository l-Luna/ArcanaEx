package arcana.research.sections;

import arcana.research.EntrySection;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

import static arcana.Arcana.arcId;

public class AspectCombosSection extends EntrySection{
	
	public static final Identifier TYPE = arcId("aspect_combos");
	
	public Identifier type(){
		return TYPE;
	}
	
	public NbtCompound data(){
		return new NbtCompound();
	}
}