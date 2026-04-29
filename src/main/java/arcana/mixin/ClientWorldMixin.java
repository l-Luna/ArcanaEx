package arcana.mixin;

import arcana.aura.InfestedChunk;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientWorld.class)
public class ClientWorldMixin{
	
	@ModifyExpressionValue(method = "getSkyColor",
	                       at = @At(value = "INVOKE",
	                                target = "Lnet/minecraft/util/CubicSampler;sampleColor(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/CubicSampler$RgbFetcher;)Lnet/minecraft/util/math/Vec3d;"))
	Vec3d getSkyColor(Vec3d original, Vec3d cameraPos, float tickDelta){
		float infestedDensity = InfestedChunk.infestationDensity((World)(Object)this, cameraPos);
		return original.lerp(new Vec3d(1, 0, 1), infestedDensity);
	}
}