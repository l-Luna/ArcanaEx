package arcana.mixin.aspects;

import arcana.duck.TooltipColourState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.tooltip.TooltipBackgroundRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(TooltipBackgroundRenderer.class)
@Environment(EnvType.CLIENT)
public class TooltipBackgroundRendererMixin{

	@ModifyArgs(method = {
			"renderVerticalLine(Lnet/minecraft/client/gui/DrawContext;IIIII)V",
			"renderHorizontalLine",
			"renderRectangle"
	}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;fill(IIIIII)V"))
	private static void alterFillColours(Args args){
		if(TooltipColourState.renderPinkTooltips)
			args.set(5, conv(args.get(5)));
	}
	
	@ModifyArgs(method = {
			"renderVerticalLine(Lnet/minecraft/client/gui/DrawContext;IIIIII)V"
	}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;fillGradient(IIIIIII)V"))
	private static void alterGradientColours(Args args){
		if(TooltipColourState.renderPinkTooltips){
			args.set(5, conv(args.get(5)));
			args.set(6, conv(args.get(6)));
		}
	}
	
	@Unique
	private static int conv(int orig){
		return switch(orig){
			case 0xf0100010 -> 0xf0140014;
			case 0x5028007f -> 0x507d014b;
			case 0x505000ff -> 0x50ff007b;
			default -> orig;
		};
	}
}