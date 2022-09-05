package arcana.warp.events;

import arcana.warp.WarpEvent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

public class NauseaEvent extends WarpEvent{
	
	public void perform(PlayerEntity player, boolean hadPrecursor){
		player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 20 * player.world.random.nextBetween(4, 7)));
		player.sendMessage(Text.translatable("message.arcana.warp.nausea"), true);
	}
	
	public boolean requiresPrecursor(){
		return true;
	}
	
	public boolean isPrecursor(){
		return false;
	}
	
	public int minWarp(){
		return 1;
	}
}