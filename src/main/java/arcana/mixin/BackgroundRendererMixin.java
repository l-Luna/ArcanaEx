package arcana.mixin;

import arcana.aura.InfestedChunk;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BackgroundRenderer.class)
public class BackgroundRendererMixin{

	@ModifyExpressionValue(method = "render",
	                       at = @At(value = "INVOKE",
	                                target = "Lnet/minecraft/util/CubicSampler;sampleColor(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/CubicSampler$RgbFetcher;)Lnet/minecraft/util/math/Vec3d;"))
	private static Vec3d u(Vec3d original, Camera camera, float tickDelta, ClientWorld world, int viewDistance, float skyDarkness){
		float infestedDensity = InfestedChunk.infestationDensity(world, camera.getPos());
		return original.lerp(new Vec3d(0.8, 0, 0.8), infestedDensity);
	}
}