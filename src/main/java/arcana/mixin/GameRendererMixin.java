package arcana.mixin;

import arcana.ArcanaTags;
import arcana.entities.WispEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin{

	@Inject(method = "method_18144", at = @At("HEAD"), cancellable = true)
	private static void handleWispAttackValidity(Entity entity, CallbackInfoReturnable<Boolean> cir){
		if(entity instanceof WispEntity)
			cir.setReturnValue(MinecraftClient.getInstance().player.getMainHandStack().isIn(ArcanaTags.WISP_WEAPONS));
	}
}