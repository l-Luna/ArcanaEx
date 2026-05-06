package arcana.mixin.fluids;

import arcana.duck.ArcanaFluidEntity;
import arcana.fluids.ArcanaFluid;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.entity.*;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity{
	
	public LivingEntityMixin(EntityType<?> type, World world){
		super(type, world);
		throw new UnsupportedOperationException();
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
}