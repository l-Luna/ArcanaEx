package arcana.research.requirements;

import arcana.components.Researcher;
import arcana.network.PkGetNote;
import arcana.research.Entry;
import arcana.research.Puzzle;
import arcana.research.Requirement;
import arcana.research.Research;
import arcana.research.puzzles.Fieldwork;
import arcana.util.NbtUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

import java.util.Map;

import static arcana.Arcana.arcId;

public class PuzzleRequirement extends Requirement{
	
	public static final Identifier TYPE = arcId("puzzle");
	
	private final Identifier puzzleId;
	
	public PuzzleRequirement(Identifier puzzleId){
		this.puzzleId = puzzleId;
	}
	
	public boolean satisfiedBy(PlayerEntity player){
		return Researcher.from(player).isPuzzleComplete(Research.getPuzzle(puzzleId));
	}
	
	public void takeFrom(PlayerEntity player){
		// no-op
	}
	
	public Identifier getPuzzleId(){
		return puzzleId;
	}
	
	public boolean onClick(Entry entry, PlayerEntity player){
		Puzzle puzzle = Research.getPuzzle(puzzleId);
		if(!(puzzle instanceof Fieldwork)){
			new PkGetNote(puzzleId).sendToServer();
			return true;
		}
		return super.onClick(entry, player);
	}
	
	public Identifier type(){
		return TYPE;
	}
	
	public NbtCompound data(){
		return NbtUtil.from(Map.of("puzzle", puzzleId));
	}
}