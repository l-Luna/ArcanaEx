package arcana.mixin;

import arcana.client.ZoneEffects;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BackgroundRenderer.class)
public class BackgroundRendererMixin{

	@ModifyExpressionValue(method = "render",
	                       at = @At(value = "INVOKE",
	                                target = "Lnet/minecraft/util/CubicSampler;sampleColor(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/CubicSampler$RgbFetcher;)Lnet/minecraft/util/math/Vec3d;"))
	private static Vec3d applyZoneFogColour(Vec3d original, Camera camera, float tickDelta, ClientWorld world, int viewDistance, float skyDarkness){
		return ZoneEffects.applyZoneFogColour(original, world, camera.getPos());
	}
	
	@Inject(method = "applyFog",
	        at = @At("TAIL"))
	private static void applyZoneFogDensity(Camera camera, BackgroundRenderer.FogType fogType, float viewDistance, boolean thickFog, float tickDelta, CallbackInfo ci){
		if(camera.getFocusedEntity() != null){
			float density = ZoneEffects.applyZoneFogDensity(camera.getFocusedEntity().world, camera.getPos());
			RenderSystem.setShaderFogStart(RenderSystem.getShaderFogStart() * density);
			RenderSystem.setShaderFogEnd(RenderSystem.getShaderFogEnd() * density);
		}
	}
}