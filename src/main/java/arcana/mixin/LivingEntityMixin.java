package arcana.mixin;

import arcana.ArcanaRegistry;
import arcana.items.BootsOfTheTravellerItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.tag.FluidTags;
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
	
	@Inject(method = "canWalkOnFluid", at = @At("HEAD"), cancellable = true)
	private void canWalkOnFluid(FluidState state, CallbackInfoReturnable<Boolean> cir){
		if(shouldWalkOnWater() && state.isIn(FluidTags.WATER))
			cir.setReturnValue(true);
	}
	
	@Unique
	private boolean shouldBoost(){
		return getEquippedStack(EquipmentSlot.FEET).getItem() instanceof BootsOfTheTravellerItem && !((Entity)(Object)this).isSneaky();
	}
	
	@Unique
	private boolean shouldWalkOnWater(){
		var entity = (Entity)(Object)this;
		return getEquippedStack(EquipmentSlot.FEET).getItem() == ArcanaRegistry.BOOTS_OF_THE_SAILOR
				&& !entity.isSneaky()
				&& !entity.world.getFluidState(entity.getBlockPos().up()).isIn(FluidTags.WATER);
	}
}