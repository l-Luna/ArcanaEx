package arcana.mixin.aspects;

import arcana.aspects.AspectMap;
import arcana.aspects.ItemAspectRegistry;
import arcana.aspects.ItemAspectsTooltipData;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(ItemStack.class)
public class ItemStackMixin{
	
	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	@Environment(EnvType.CLIENT)
	@ModifyReturnValue(method = "getTooltipData", at = @At("RETURN"))
	private Optional<TooltipData> applyAspectsTooltipData(Optional<TooltipData> original){
		if(Screen.hasShiftDown()){
			AspectMap aspects = ItemAspectRegistry.get((ItemStack)(Object)this);
			if(!aspects.isEmpty())
				return Optional.of(new ItemAspectsTooltipData(aspects.asStacks(), original.orElse(null)));
		}
		return original;
	}
}