package arcana.mixin;

import arcana.aspects.ItemAspectRegistry;
import arcana.client.AspectsTooltipData;
import net.minecraft.client.item.TooltipData;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(Item.class)
public class ItemMixin{

	// TODO: move to ItemStack so bundle aspects display actually works
	@Inject(at = @At("RETURN"), method = "getTooltipData", cancellable = true)
	private void applyAspectsTooltipData(ItemStack stack, CallbackInfoReturnable<Optional<TooltipData>> cir){
		var aspects = ItemAspectRegistry.get(stack);
		if(aspects.size() > 0)
			cir.setReturnValue(Optional.of(new AspectsTooltipData(aspects, cir.getReturnValue().orElse(null))));
	}
}