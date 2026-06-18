package arcana.mixin.aspects;

import arcana.client.tooltip.PinkMarkerComponent;
import arcana.duck.TooltipColourState;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipPositioner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(DrawContext.class)
public class DrawContextMixin{

	@WrapOperation(method = "drawTooltip(Lnet/minecraft/client/font/TextRenderer;Ljava/util/List;IILnet/minecraft/client/gui/tooltip/TooltipPositioner;)V",
	               at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;draw(Ljava/lang/Runnable;)V"))
	void drawTooltip(DrawContext instance, Runnable drawCallback, Operation<Void> original, TextRenderer textRenderer, List<TooltipComponent> components, int x, int y, TooltipPositioner positioner){
		if(components.stream().anyMatch(PinkMarkerComponent.class::isInstance))
			TooltipColourState.renderPinkTooltips = true;
		try{
			original.call(instance, drawCallback);
		}finally{
			TooltipColourState.renderPinkTooltips = false;
		}
	}
}