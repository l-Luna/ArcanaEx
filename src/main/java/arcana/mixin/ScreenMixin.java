package arcana.mixin;

import arcana.client.AspectRenderer;
import arcana.client.PinkMarkerComponent;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.List;

@Mixin(Screen.class)
public abstract class ScreenMixin{
	
	@ModifyArgs(method = "renderTooltipFromComponents",
	            at = @At(value = "INVOKE",
	                     target = "Lnet/minecraft/client/gui/screen/Screen;fillGradient(Lnet/minecraft/util/math/Matrix4f;Lnet/minecraft/client/render/BufferBuilder;IIIIIII)V"))
	private void changeTooltipBackgroundColour(Args args, MatrixStack matrices, List<TooltipComponent> components, int x, int y){
		if(!AspectRenderer.useAspectTooltipColours && components.stream().noneMatch(PinkMarkerComponent.class::isInstance))
			return;
		args.set(7, conv(args.get(7)));
		args.set(8, conv(args.get(8)));
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