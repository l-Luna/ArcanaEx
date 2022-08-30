package arcana.research.requirements;

import arcana.research.Requirement;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

import static arcana.Arcana.arcId;

public class XpRequirement extends Requirement{
	
	public static final Identifier TYPE = arcId("xp");
	
	public boolean satisfiedBy(PlayerEntity player){
		return player.experienceLevel >= amount;
	}
	
	public void takeFrom(PlayerEntity player){
		player.experienceLevel -= amount;
	}
	
	public Identifier type(){
		return TYPE;
	}
	
	public NbtCompound data(){
		return new NbtCompound();
	}
}