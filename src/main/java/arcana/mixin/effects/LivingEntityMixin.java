package arcana.mixin.effects;

import arcana.ArcanaRegistry;
import arcana.effects.PressureStatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(LivingEntity.class)
public class LivingEntityMixin{
	
	@Shadow
	@Final
	private Map<RegistryEntry<StatusEffect>, StatusEffectInstance> activeStatusEffects;
	
	@Inject(method = "hasStatusEffect", at = @At("HEAD"), cancellable = true)
	private void hasStatusEffect(RegistryEntry<StatusEffect> effect, CallbackInfoReturnable<Boolean> cir){
		if(effect == StatusEffects.JUMP_BOOST && actuallyHasStatusEffect(ArcanaRegistry.AIR_POWER.entry())){
			cir.setReturnValue(true);
			return;
		}
		if(effect == StatusEffects.FIRE_RESISTANCE && actuallyHasStatusEffect(ArcanaRegistry.FIRE_POWER.entry())){
			cir.setReturnValue(true);
			return;
		}
		if(effect == StatusEffects.WATER_BREATHING && actuallyHasStatusEffect(ArcanaRegistry.WATER_POWER.entry())){
			cir.setReturnValue(true);
			return;
		}
		
		if(PressureStatusEffect.suppresses((LivingEntity)(Object)this, effect))
			cir.setReturnValue(false);
	}
	
	@Inject(method = "getStatusEffect", at = @At("HEAD"), cancellable = true)
	private void getStatusEffect(RegistryEntry<StatusEffect> effect, CallbackInfoReturnable<StatusEffectInstance> cir){
		if(actuallyHasStatusEffect(ArcanaRegistry.AIR_POWER.entry()) && effect == StatusEffects.JUMP_BOOST && !actuallyHasStatusEffect(StatusEffects.JUMP_BOOST)){
			cir.setReturnValue(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 0, 0));
			return;
		}
		if(actuallyHasStatusEffect(ArcanaRegistry.FIRE_POWER.entry()) && effect == StatusEffects.FIRE_RESISTANCE && !actuallyHasStatusEffect(StatusEffects.FIRE_RESISTANCE)){
			cir.setReturnValue(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE));
			return;
		}
		if(actuallyHasStatusEffect(ArcanaRegistry.WATER_POWER.entry()) && effect == StatusEffects.WATER_BREATHING && !actuallyHasStatusEffect(StatusEffects.WATER_BREATHING)){
			cir.setReturnValue(new StatusEffectInstance(StatusEffects.WATER_BREATHING));
			return;
		}
		
		if(PressureStatusEffect.suppresses((LivingEntity)(Object)this, effect))
			cir.setReturnValue(null);
	}
	
	@Unique
	private boolean actuallyHasStatusEffect(RegistryEntry<StatusEffect> effect){
		return activeStatusEffects.containsKey(effect);
	}
}