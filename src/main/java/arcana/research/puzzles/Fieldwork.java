package arcana.research.puzzles;

import arcana.research.Puzzle;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

import static arcana.Arcana.arcId;

public class Fieldwork extends Puzzle{
	
	public static final Identifier TYPE = arcId("fieldwork");
	
	public Identifier type(){
		return TYPE;
	}
	
	public NbtCompound data(){
		return new NbtCompound();
	}
}