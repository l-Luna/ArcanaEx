package arcana.mixin.enchantments;

import arcana.ArcanaRegistry;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin{

	//
	
	@ModifyReturnValue(method = "getFireAspect", at = @At("RETURN"))
	private static int getFireAspect(int original, LivingEntity entity){
		return entity.hasStatusEffect(ArcanaRegistry.FIRE_POWER) ? (Math.max(original, 1)) : original;
	}
}