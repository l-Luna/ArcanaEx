package arcana.mixin;

import arcana.items.BootsOfTheTravellerItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin{
	
	@Shadow public abstract ItemStack getEquippedStack(EquipmentSlot slot);
	
	@Shadow @Final private Map<StatusEffect, StatusEffectInstance> activeStatusEffects;
	
	@Inject(method = "hasStatusEffect", at = @At("HEAD"), cancellable = true)
	private void hasStatusEffect(StatusEffect effect, CallbackInfoReturnable<Boolean> cir){
		if(shouldBoost() && effect == StatusEffects.JUMP_BOOST)
			cir.setReturnValue(true);
	}
	
	@Inject(method = "getStatusEffect", at = @At("HEAD"), cancellable = true)
	private void getStatusEffect(StatusEffect effect, CallbackInfoReturnable<StatusEffectInstance> cir){
		if(shouldBoost() && effect == StatusEffects.JUMP_BOOST)
			// use existing higher jump boost if present
			if(!activeStatusEffects.containsKey(StatusEffects.JUMP_BOOST))
				cir.setReturnValue(new StatusEffectInstance(StatusEffects.JUMP_BOOST));
	}
	
	@Unique
	private boolean shouldBoost(){
		return getEquippedStack(EquipmentSlot.FEET).getItem() instanceof BootsOfTheTravellerItem && !((Entity)(Object)this).isSneaky();
	}
}