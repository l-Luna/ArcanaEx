package arcana.research.puzzles;

import arcana.research.Puzzle;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

import static arcana.Arcana.arcId;

public class AspectChess extends Puzzle{
	
	public static final Identifier TYPE = arcId("aspect_chess");
	
	// opponent, difficulty...
	
	public Identifier type(){
		return TYPE;
	}
	
	public NbtCompound data(){
		return new NbtCompound();
	}
}