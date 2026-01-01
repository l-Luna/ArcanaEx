package arcana.mixin;

import arcana.items.ScalpelItem;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class EnchantmentTargetsMixin{
	
	@SuppressWarnings("unused")
	@Mixin(targets = "net.minecraft.enchantment.EnchantmentTarget$11")
	public static class EtWeaponMixin{
		
		@Inject(method = "isAcceptableItem", at = @At("HEAD"), cancellable = true)
		void isAcceptableItem(Item item, CallbackInfoReturnable<Boolean> cir){
			if(item instanceof ScalpelItem)
				cir.setReturnValue(true);
		}
	}
}