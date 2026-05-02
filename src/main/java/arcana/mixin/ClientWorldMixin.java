package arcana.mixin;

import arcana.ArcanaRegistry;
import arcana.aura.InfestedChunk;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.block.Block;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeParticleConfig;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(ClientWorld.class)
public class ClientWorldMixin{
	
	@ModifyExpressionValue(method = "getSkyColor",
	                       at = @At(value = "INVOKE",
	                                target = "Lnet/minecraft/util/CubicSampler;sampleColor(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/CubicSampler$RgbFetcher;)Lnet/minecraft/util/math/Vec3d;"))
	Vec3d getSkyColor(Vec3d original, Vec3d cameraPos, float tickDelta){
		float infestedDensity = InfestedChunk.infestationDensity((World)(Object)this, cameraPos);
		return original.lerp(new Vec3d(1, 0, 1), infestedDensity);
	}
	
	@ModifyExpressionValue(method = "randomBlockDisplayTick",
	                       at = @At(value = "INVOKE",
	                                target = "Lnet/minecraft/world/biome/Biome;getParticleConfig()Ljava/util/Optional;"))
	Optional<BiomeParticleConfig> getParticleBiomeConfig(Optional<BiomeParticleConfig> original,
	                                                     int centerX,
	                                                     int centerY,
	                                                     int centerZ,
	                                                     int radius,
	                                                     Random random,
	                                                     @Nullable Block block,
	                                                     BlockPos.Mutable pos){
		// `pos` has been updated to the correct position earlier in the method
		float density = InfestedChunk.infestationDensity((World)(Object)this, Vec3d.ofCenter(pos));
		if(random.nextFloat() <= density)
			return Optional.of(new BiomeParticleConfig(ArcanaRegistry.TAINT_SPORE, 0.03f));
		return original;
	}
}