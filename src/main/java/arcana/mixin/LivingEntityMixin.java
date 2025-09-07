package arcana.mixin;

import arcana.ArcanaRegistry;
import arcana.duck.ArcanaFluidEntity;
import arcana.fluids.ArcanaFluid;
import arcana.items.BootsOfTheTravellerItem;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.block.BlockState;
import net.minecraft.block.LadderBlock;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.entity.*;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity{
	
	public LivingEntityMixin(EntityType<?> type, World world){
		super(type, world);
		throw new UnsupportedOperationException();
	}
	
	@Shadow
	public abstract ItemStack getEquippedStack(EquipmentSlot slot);
	
	@Shadow
	@Final
	private Map<StatusEffect, StatusEffectInstance> activeStatusEffects;
	
	@Inject(method = "hasStatusEffect", at = @At("HEAD"), cancellable = true)
	private void hasStatusEffect(StatusEffect effect, CallbackInfoReturnable<Boolean> cir){
		if(effect == StatusEffects.JUMP_BOOST && effectiveJumpBoost() > 0)
			cir.setReturnValue(true);
		if(effect == StatusEffects.FIRE_RESISTANCE && shouldHaveFireImmunity())
			cir.setReturnValue(true);
		if(effect == StatusEffects.WATER_BREATHING && shouldHaveWaterBreathing())
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
				getEquippedStack(EquipmentSlot.FEET).getItem() instanceof BootsOfTheTravellerItem && !isSneaky();
		boolean boostFromAir = actuallyHasStatusEffect(ArcanaRegistry.AIR_POWER);
		return (boostFromBoots ? 1 : 0) + (boostFromAir ? 1 : 0);
	}
	
	@Unique
	private boolean shouldWalkOnWater(){
		return getEquippedStack(EquipmentSlot.FEET).getItem() == ArcanaRegistry.BOOTS_OF_THE_SAILOR
				&& !isSneaky()
				&& !world.getFluidState(getBlockPos().up()).isIn(FluidTags.WATER);
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
	
	// handle fluid movement for arcana fluids
	
	@ModifyExpressionValue(method = "tickMovement",
	                       at = @At(value = "INVOKE",
	                                target = "Lnet/minecraft/entity/LivingEntity;getFluidHeight(Lnet/minecraft/tag/TagKey;)D",
	                                ordinal = 1))
	private double tickFluidSwimmingHeight(double original){
		return Math.max(original, ((ArcanaFluidEntity)this).arcana$getMaxFluidHeight());
	}
	
	@ModifyExpressionValue(method = "tickMovement",
	                       at = @At(value = "INVOKE",
	                                target = "Lnet/minecraft/entity/LivingEntity;isInLava()Z",
	                                ordinal = 1))
	private boolean tickFluidSwimmingKind(boolean original){
		return original || isInArcanaFluid();
	}
	
	@ModifyExpressionValue(method = "travel",
	                       at = @At(value = "INVOKE",
	                                target = "Lnet/minecraft/entity/LivingEntity;isInLava()Z"))
	private boolean travelFluidCheck(boolean original, @Share("override") LocalRef<ArcanaFluid> override){
		ArcanaFluid fluid = ((ArcanaFluidEntity)this).arcana$getMaxSubmergedFluid();
		if(fluid != null)
			override.set(fluid);
		return original || (fluid != null);
	}
	
	@Inject(method = "travel",
	        at = @At(value = "INVOKE",
	                 target = "Lnet/minecraft/entity/LivingEntity;updateVelocity(FLnet/minecraft/util/math/Vec3d;)V",
	                 ordinal = 1),
	        cancellable = true)
	private void travelFluidImpl(Vec3d movementInput, CallbackInfo ci, @Share("override") LocalRef<ArcanaFluid> override){
		ArcanaFluid fluid = override.get();
		if(fluid != null){
			ci.cancel();
			LivingEntity lem = (LivingEntity)(Object)this;
			double gravity = 0.08;
			boolean falling = getVelocity().y <= 0.0;
			if(falling && lem.hasStatusEffect(StatusEffects.SLOW_FALLING))
				gravity = 0.01;
			// just reimplement this segment to tweak the numbers
			float dragFactor = fluid.getEntityDragFactor(this);
			float speedFactor = fluid.getEntitySpeedFactor(this);
			
			double y = getY();
			updateVelocity(speedFactor, movementInput);
			move(MovementType.SELF, getVelocity());
			if(getFluidHeight(fluid.getTag()) <= getSwimHeight()){
				setVelocity(getVelocity().multiply(dragFactor, 0.8, dragFactor));
				setVelocity(lem.applyFluidMovingSpeed(gravity, falling, getVelocity()));
			}else
				setVelocity(getVelocity().multiply(dragFactor));
			
			if(!hasNoGravity())
				setVelocity(getVelocity().add(0, -gravity / 6, 0));
			
			Vec3d vel = getVelocity();
			if(horizontalCollision && doesNotCollide(vel.x, vel.y + 0.6f - getY() + y, vel.z))
				setVelocity(vel.x, 0.3f, vel.z);
			
			// and the method epilogue
			lem.updateLimbs(lem, this instanceof Flutterer);
		}
	}
	
	@Unique
	private boolean isInArcanaFluid(){
		return !firstUpdate && ((ArcanaFluidEntity)this).arcana$getMaxFluidHeight() > 0;
	}
	
	// make trapdoors work right with metal ladders
	
	@Inject(method = "canEnterTrapdoor", at = @At("HEAD"), cancellable = true)
	private void canEnterTrapdoor(BlockPos pos, BlockState state, CallbackInfoReturnable<Boolean> cir){
		if(state.get(TrapdoorBlock.OPEN)){
			BlockState ladderState = world.getBlockState(pos.down());
			if(ladderState.isOf(ArcanaRegistry.METAL_LADDER) && ladderState.get(LadderBlock.FACING) == state.get(TrapdoorBlock.FACING))
				cir.setReturnValue(true);
		}
	}
}