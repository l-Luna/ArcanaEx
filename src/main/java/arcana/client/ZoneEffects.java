package arcana.client;

import arcana.ArcanaRegistry;
import arcana.aura.InfestedChunk;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeParticleConfig;

import java.util.Optional;

public final class ZoneEffects{

	// TODO: cache density per tick/frame
	
	public static Vec3d skyColour(Vec3d original, World world, Vec3d pos){
		float infestedDensity = InfestedChunk.infestationDensity(world, pos);
		return original.lerp(new Vec3d(1, 0.4, 1), infestedDensity);
	}
	
	public static Vec3d fogColour(Vec3d original, World world, Vec3d pos){
		float infestedDensity = InfestedChunk.infestationDensity(world, pos);
		return original.lerp(new Vec3d(0.1, 0, 0.1), infestedDensity);
	}
	
	public static float fogDensity(World world, Vec3d pos){
		float infestedDensity = InfestedChunk.infestationDensity(world, pos);
		return (1 - infestedDensity) * 0.9f + 0.1f;
	}
	
	public static float lightModifier(World world, Vec3d pos){
		float infestedDensity = InfestedChunk.infestationDensity(world, pos);
		return infestedDensity * -0.15f;
	}
	
	public static Optional<BiomeParticleConfig> ambientParticles(World world, Vec3d pos){
		float density = InfestedChunk.infestationDensity(world, pos);
		if(world.random.nextFloat() <= density)
			return Optional.of(new BiomeParticleConfig(ArcanaRegistry.TAINT_SPORE, 0.03f));
		return Optional.empty();
	}
}