package arcana.mixin.zones;

import arcana.client.ZoneEffects;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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
@Environment(EnvType.CLIENT)
public class ClientWorldMixin{
	
	@ModifyExpressionValue(method = "getSkyColor",
	                       at = @At(value = "INVOKE",
	                                target = "Lnet/minecraft/util/CubicSampler;sampleColor(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/CubicSampler$RgbFetcher;)Lnet/minecraft/util/math/Vec3d;"))
	Vec3d getSkyColor(Vec3d original, Vec3d cameraPos, float tickDelta){
		return ZoneEffects.skyColour(original, (World)(Object)this, cameraPos);
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
		return ZoneEffects.ambientParticles((World)(Object)this, Vec3d.ofCenter(pos)).or(() -> original);
	}
}