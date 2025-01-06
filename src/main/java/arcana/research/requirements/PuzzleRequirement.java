package arcana.research.requirements;

import arcana.ArcanaRegistry;
import arcana.components.Researcher;
import arcana.network.PkGetNote;
import arcana.research.*;
import arcana.research.puzzles.Fieldwork;
import arcana.util.NbtUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
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
		// don't try to *strictly* forbid getting notes in edge cases, those aren't cheating or anything
		// (otherwise these would be in PkGetNote)
		Puzzle puzzle = Research.getPuzzle(puzzleId);
		Researcher researcher = Researcher.from(player);
		if(!(puzzle instanceof Fieldwork)
				&& PkGetNote.canGetNote(player)
				&& !researcher.isPuzzleComplete(puzzle)
				&& !alreadyHasNote(puzzle, player)
				&& (researcher.isEntryComplete(Research.getEntry(BuiltinResearch.researchTutorialEntry))
					|| puzzle.id().equals(BuiltinResearch.researchTutorialPuzzle))){
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
	
	public static boolean alreadyHasNote(Puzzle puzzle, PlayerEntity player){
		for(int i = 0; i < player.getInventory().size(); i++){
			ItemStack stack = player.getInventory().getStack(i);
			if(stack.isOf(ArcanaRegistry.RESEARCH_NOTES)
					&& stack.hasNbt()
					&& stack.getNbt().getString("puzzle_id").equals(puzzle.id().toString()))
				return true;
		}
		return false;
	}
}