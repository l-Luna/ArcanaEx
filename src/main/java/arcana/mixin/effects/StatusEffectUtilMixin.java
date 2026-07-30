package arcana.mixin.effects;

import arcana.effects.SetBonusStatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StatusEffectUtil.class)
public class StatusEffectUtilMixin{
	
	@Inject(method = "getDurationText", at = @At("HEAD"), cancellable = true)
	private static void durationToString(StatusEffectInstance effect, float multiplier, float tickRate, CallbackInfoReturnable<Text> cir){
		if(effect.getEffectType().value() instanceof SetBonusStatusEffect)
			cir.setReturnValue(Text.translatable("effect.arcana.set_bonus"));
	}
}