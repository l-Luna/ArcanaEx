package arcana.research.sections;

import arcana.research.EntrySection;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

import static arcana.Arcana.arcId;

public class FluxStatsSection extends EntrySection{
	
	public static final Identifier TYPE = arcId("flux_stats");
	
	public Identifier type(){
		return TYPE;
	}
	
	public NbtCompound data(){
		return new NbtCompound();
	}
}