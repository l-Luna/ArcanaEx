package arcana.mixin;

import arcana.ArcanaRegistry;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.minecraft.enchantment.EnchantmentHelper.getEquipmentLevel;

@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin{

	@Inject(method = "getFireAspect", at = @At("HEAD"), cancellable = true)
	private static void getFireAspect(LivingEntity entity, CallbackInfoReturnable<Integer> cir){
		int orig = getEquipmentLevel(Enchantments.FIRE_ASPECT, entity);
		if(entity.hasStatusEffect(ArcanaRegistry.FIRE_POWER))
			cir.setReturnValue(Math.max(orig, 1));
	}
}