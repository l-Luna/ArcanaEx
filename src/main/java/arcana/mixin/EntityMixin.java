package arcana.mixin;

import arcana.ArcanaRegistry;
import arcana.ArcanaTags;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Entity.class)
public class EntityMixin{
	
	// when interacting with Taint Goo, gain the Tainted status effect
	
	@ModifyReturnValue(method = "updateWaterState", at = @At("TAIL"))
	private boolean updateWaterState(boolean original){
		Entity self = (Entity)(Object)this;
		boolean inTaintGoo = self.updateMovementInFluid(ArcanaTags.TAINT_GOO, 0.001f);
		if(inTaintGoo
				&& self instanceof LivingEntity lem
				&& (self.world.getTime() % 80 == 0 || !lem.hasStatusEffect(ArcanaRegistry.TAINTED)))
			lem.addStatusEffect(new StatusEffectInstance(ArcanaRegistry.TAINTED, 5 * 20));
		return original || inTaintGoo;
	}
}