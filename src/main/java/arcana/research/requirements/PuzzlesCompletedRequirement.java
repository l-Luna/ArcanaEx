package arcana.research.requirements;

import arcana.components.Researcher;
import arcana.research.Requirement;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

import static arcana.Arcana.arcId;

public class PuzzlesCompletedRequirement extends Requirement{
	
	public static final Identifier TYPE = arcId("puzzles_completed");
	
	public boolean satisfiedBy(PlayerEntity player){
		return Researcher.from(player).getCompletedPuzzleCount() >= amount;
	}
	
	public void takeFrom(PlayerEntity player){
		// no-op
	}
	
	public Identifier type(){
		return TYPE;
	}
	
	public NbtCompound data(){
		return new NbtCompound();
	}
}