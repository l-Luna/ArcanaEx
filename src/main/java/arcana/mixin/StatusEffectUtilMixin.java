package arcana.mixin;

import arcana.effects.SetBonusStatusEffect;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StatusEffectUtil.class)
public class StatusEffectUtilMixin{
	
	@Inject(method = "durationToString", at = @At("HEAD"), cancellable = true)
	@Environment(EnvType.CLIENT)
	private static void durationToString(StatusEffectInstance effect, float multiplier, CallbackInfoReturnable<String> cir){
		if(effect.getEffectType() instanceof SetBonusStatusEffect)
			cir.setReturnValue(I18n.translate("effect.arcana.set_bonus"));
	}
}