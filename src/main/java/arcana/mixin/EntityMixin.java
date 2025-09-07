package arcana.mixin;

import arcana.ArcanaRegistry;
import arcana.duck.ArcanaFluidEntity;
import arcana.fluids.ArcanaFluid;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluid;
import net.minecraft.tag.TagKey;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Entity.class)
public class EntityMixin implements ArcanaFluidEntity{
	
	@Shadow
	protected Object2DoubleMap<TagKey<Fluid>> fluidHeight;
	@Unique
	private ArcanaFluid maxSubmergedFluid;
	@Unique
	private double maxFluidHeight;
	
	@ModifyReturnValue(method = "updateWaterState", at = @At("TAIL"))
	private boolean updateWaterState(boolean original){
		Entity self = (Entity)(Object)this;
		maxSubmergedFluid = null;
		maxFluidHeight = 0;
		boolean inAny = false;
		for(ArcanaFluid fluid : ArcanaRegistry.stillFluids){
			TagKey<Fluid> tag = fluid.getTag();
			boolean submerged = self.updateMovementInFluid(tag, fluid.getEntityPushStrength(self));
			if(submerged){
				fluid.onEntityInteractTick(self);
				double thisHeight = fluidHeight.getDouble(tag);
				if(thisHeight > maxFluidHeight){
					maxSubmergedFluid = fluid;
					maxFluidHeight = thisHeight;
				}
				inAny = true;
			}
		}
		return original || inAny;
	}
	
	public @Nullable ArcanaFluid arcana$getMaxSubmergedFluid(){
		return maxSubmergedFluid;
	}
	
	public double arcana$getMaxFluidHeight(){
		return maxFluidHeight;
	}
}