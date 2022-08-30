package arcana.research;

import net.minecraft.entity.player.PlayerEntity;

public abstract class Requirement{
	
	public abstract boolean satisfiedBy(PlayerEntity player);
	
	public abstract void takeFrom(PlayerEntity player);
}