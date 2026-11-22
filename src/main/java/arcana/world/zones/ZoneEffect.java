package arcana.world.zones;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.biome.BiomeParticleConfig;

import java.util.Optional;

public record ZoneEffect(
	Optional<PropertyModifier<Vec3d>> skyColor,
	Optional<PropertyModifier<Vec3d>> fogColor,
	Optional<PropertyModifier<Float>> fogDistance,
	Optional<PropertyModifier<Vec3d>> lightColor,
	Optional<PropertyModifier<BiomeParticleConfig>> ambientParticle
){

	public static final Codec<ZoneEffect> CODEC = RecordCodecBuilder.create(i -> i.group(
			PropertyModifier.createCodec(Vec3d.CODEC).optionalFieldOf("sky_color").forGetter(ZoneEffect::skyColor),
			PropertyModifier.createCodec(Vec3d.CODEC).optionalFieldOf("fog_color").forGetter(ZoneEffect::fogColor),
			PropertyModifier.createCodec(Codec.FLOAT).optionalFieldOf("fog_distance").forGetter(ZoneEffect::fogDistance),
			PropertyModifier.createCodec(Vec3d.CODEC).optionalFieldOf("light_color").forGetter(ZoneEffect::lightColor),
			PropertyModifier.createCodec(BiomeParticleConfig.CODEC).optionalFieldOf("ambient_particle").forGetter(ZoneEffect::ambientParticle)
	).apply(i, ZoneEffect::new));
}