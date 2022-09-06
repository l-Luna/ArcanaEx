package arcana.mixin;

import arcana.aspects.ItemAspectRegistry;
import arcana.aspects.ItemAspectsTooltipData;
import net.minecraft.client.item.TooltipData;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(ItemStack.class)
public class ItemStackMixin{
	
	@Inject(at = @At("RETURN"), method = "getTooltipData", cancellable = true)
	private void applyAspectsTooltipData(CallbackInfoReturnable<Optional<TooltipData>> cir){
		var aspects = ItemAspectRegistry.get((ItemStack)(Object)this);
		if(aspects.size() > 0)
			cir.setReturnValue(Optional.of(new ItemAspectsTooltipData(aspects.asStacks(), cir.getReturnValue().orElse(null))));
	}
}