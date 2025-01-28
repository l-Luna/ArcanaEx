package arcana.mixin;

import arcana.ArcanaRegistry;
import arcana.ArcanaTags;
import arcana.items.BootsOfTheTravellerItem;
import net.minecraft.block.BlockState;
import net.minecraft.block.LadderBlock;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.Map;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin{
	
	@Shadow public abstract ItemStack getEquippedStack(EquipmentSlot slot);
	
	@Shadow @Final private Map<StatusEffect, StatusEffectInstance> activeStatusEffects;
	
	@Shadow public abstract @Nullable StatusEffectInstance getStatusEffect(StatusEffect effect);
	
	@Shadow public abstract Collection<StatusEffectInstance> getStatusEffects();
	
	@Shadow public abstract boolean hasStatusEffect(StatusEffect effect);
	
	@Inject(method = "hasStatusEffect", at = @At("HEAD"), cancellable = true)
	private void hasStatusEffect(StatusEffect effect, CallbackInfoReturnable<Boolean> cir){
		if(effectiveJumpBoost() > 0 && effect == StatusEffects.JUMP_BOOST)
			cir.setReturnValue(true);
		if(shouldHaveFireImmunity() && effect == StatusEffects.FIRE_RESISTANCE)
			cir.setReturnValue(true);
		if(shouldHaveWaterBreathing() && effect == StatusEffects.WATER_BREATHING)
			cir.setReturnValue(true);
	}
	
	@Inject(method = "getStatusEffect", at = @At("HEAD"), cancellable = true)
	private void getStatusEffect(StatusEffect effect, CallbackInfoReturnable<StatusEffectInstance> cir){
		int boost = effectiveJumpBoost();
		if(boost > 0 && effect == StatusEffects.JUMP_BOOST)
			// use existing higher jump boost if present
			if(!activeStatusEffects.containsKey(StatusEffects.JUMP_BOOST))
				cir.setReturnValue(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 0, boost - 1));
		if(shouldHaveFireImmunity() && effect == StatusEffects.FIRE_RESISTANCE && !activeStatusEffects.containsKey(StatusEffects.FIRE_RESISTANCE))
			cir.setReturnValue(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE));
		if(shouldHaveWaterBreathing() && effect == StatusEffects.WATER_BREATHING && !activeStatusEffects.containsKey(StatusEffects.WATER_BREATHING))
			cir.setReturnValue(new StatusEffectInstance(StatusEffects.WATER_BREATHING));
	}
	
	@Inject(method = "canWalkOnFluid", at = @At("HEAD"), cancellable = true)
	private void canWalkOnFluid(FluidState state, CallbackInfoReturnable<Boolean> cir){
		if(shouldWalkOnWater() && state.isIn(FluidTags.WATER))
			cir.setReturnValue(true);
	}
	
	@Unique
	private int effectiveJumpBoost(){
		boolean boostFromBoots =
				getEquippedStack(EquipmentSlot.FEET).getItem() instanceof BootsOfTheTravellerItem && !((Entity)(Object)this).isSneaky();
		boolean boostFromAir = actuallyHasStatusEffect(ArcanaRegistry.AIR_POWER);
		return (boostFromBoots ? 1 : 0) + (boostFromAir ? 1 : 0);
	}
	
	@Unique
	private boolean shouldWalkOnWater(){
		var entity = (Entity)(Object)this;
		return getEquippedStack(EquipmentSlot.FEET).getItem() == ArcanaRegistry.BOOTS_OF_THE_SAILOR
				&& !entity.isSneaky()
				&& !entity.world.getFluidState(entity.getBlockPos().up()).isIn(FluidTags.WATER);
	}
	
	@Unique
	private boolean shouldHaveFireImmunity(){
		return actuallyHasStatusEffect(ArcanaRegistry.FIRE_POWER);
	}
	
	@Unique
	private boolean shouldHaveWaterBreathing(){
		return actuallyHasStatusEffect(ArcanaRegistry.WATER_POWER);
	}
	
	@Unique
	private boolean actuallyHasStatusEffect(StatusEffect effect){
		return activeStatusEffects.containsKey(effect);
	}
	
	// when interacting with Taint Goo, gain the Tainted status effect
	
	@Inject(method = "tick", at = @At("TAIL"))
	private void tick(CallbackInfo ci){
		LivingEntity self = (LivingEntity)(Object)this;
		boolean inTaintGoo = self.updateMovementInFluid(ArcanaTags.TAINT_GOO, 0.001f);
		if(inTaintGoo)
			if(self.world.getTime() % 80 == 0 || !actuallyHasStatusEffect(ArcanaRegistry.TAINTED))
				self.addStatusEffect(new StatusEffectInstance(ArcanaRegistry.TAINTED, 5 * 20));
	}
	
	// make trapdoors work right with metal ladders
	
	@Inject(method = "canEnterTrapdoor", at = @At("HEAD"), cancellable = true)
	private void canEnterTrapdoor(BlockPos pos, BlockState state, CallbackInfoReturnable<Boolean> cir){
		if(state.get(TrapdoorBlock.OPEN)){
			BlockState ladderState = ((LivingEntity)(Object)this).world.getBlockState(pos.down());
			if(ladderState.isOf(ArcanaRegistry.METAL_LADDER) && ladderState.get(LadderBlock.FACING) == state.get(TrapdoorBlock.FACING))
				cir.setReturnValue(true);
		}
	}
}