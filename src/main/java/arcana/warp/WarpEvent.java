package arcana.warp;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

public abstract class WarpEvent{
	
	public void perform(PlayerEntity player, boolean hadPrecursor){
		// spawn mind spiders, give status effects, torment the player in server-side ways...
	}
	
	@Environment(EnvType.CLIENT)
	public void performOnClient(PlayerEntity player, boolean hadPrecursor){
		// show odd messages, make things look wrong, torment the player in client-side ways...
	}
	
	public boolean requiresPrecursor(){
		return false;
	}
	
	// "precursor" events are harmless, but followed by more significant events
	public abstract boolean isPrecursor();
	public abstract int minWarp();
	
	public Identifier id(){
		return WarpEvents.events.inverse().get(this);
	}
}