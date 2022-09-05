package arcana.warp.events;

import arcana.warp.WarpEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

public class PassEvent extends WarpEvent{
	
	@Environment(EnvType.CLIENT)
	public void performOnClient(PlayerEntity player, boolean hadPrecursor){
		if(hadPrecursor)
			player.sendMessage(Text.translatable("message.arcana.warp.pass"), true);
	}
	
	public boolean isPrecursor(){
		return false;
	}
	
	public int minWarp(){
		return 1;
	}
}