package arcana.research.requirements;

import arcana.legacy_components.Researcher;
import arcana.research.Requirement;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

import static arcana.Arcana.arcId;

public class FociCastRequirement extends Requirement{
	
	public static final Identifier TYPE = arcId("foci_cast");
	
	public boolean satisfiedBy(PlayerEntity player){
		return Researcher.from(player).getCastFociCount() >= amount;
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