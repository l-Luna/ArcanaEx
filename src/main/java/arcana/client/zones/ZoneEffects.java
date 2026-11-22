package arcana.client.zones;

import arcana.world.zones.PropertyModifier;
import arcana.world.zones.Zone;
import arcana.world.zones.ZoneEffect;
import arcana.world.zones.Zones;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeParticleConfig;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Function;

public final class ZoneEffects{
	
	private static final Object2FloatMap<Zone> cameraZoneDensities = new Object2FloatOpenHashMap<>();
	
	public static void onFrameStart(WorldRenderContext ctx){
		ctx.profiler().push("arcana:gather_zones");
		cameraZoneDensities.clear();
		for(Zone zone : Zone.getRegistry(ctx.world())){
			float density = Zones.getZoneDensity(ctx.world(), zone, ctx.camera().getPos());
			if(density > 0)
				cameraZoneDensities.put(zone, density);
		}
		ctx.profiler().pop();
	}
	
	//
	
	public static Vec3d skyColourAtCamera(Vec3d original){
		return getCameraVec3d(original, ZoneEffect::skyColor);
	}
	
	public static Vec3d fogColourAtCamera(Vec3d original){
		return getCameraVec3d(original, ZoneEffect::fogColor);
	}
	
	public static float fogDistanceAtCamera(){
		return getCameraFloat(1, ZoneEffect::fogDistance);
	}
	
	public static float lightModifierAtCamera(){
		return (float)getCameraVec3d(new Vec3d(0, 0, 0), ZoneEffect::lightColor).x;
	}
	
	public static Optional<BiomeParticleConfig> ambientParticlesAt(World world){
		return sampleCameraValue(world, ZoneEffect::ambientParticle);
	}
	
	//
	
	private static Float getCameraFloat(float original, Function<ZoneEffect, Optional<PropertyModifier<Float>>> property){
		if(cameraZoneDensities.isEmpty())
			return original;
		float totalValue = 0;
		float totalWeight = 0;
		for(Object2FloatMap.Entry<Zone> entry : cameraZoneDensities.object2FloatEntrySet()){
			Optional<PropertyModifier<Float>> modifierO = property.apply(entry.getKey().effects());
			if(modifierO.isPresent()){
				PropertyModifier<Float> modifier = modifierO.get();
				float weight = modifier.factor(entry.getFloatValue());
				totalValue = totalValue + modifier.target() * weight;
				totalWeight += weight;
			}
		}
		if(totalWeight > 0){
			totalValue = totalValue / totalWeight;
			return MathHelper.lerp(Math.min(1, totalWeight), original, totalValue);
		}
		return original;
	}
	
	private static Vec3d getCameraVec3d(Vec3d original, Function<ZoneEffect, Optional<PropertyModifier<Vec3d>>> property){
		if(cameraZoneDensities.isEmpty())
			return original;
		Vec3d totalColor = new Vec3d(0, 0, 0);
		float totalWeight = 0;
		for(Object2FloatMap.Entry<Zone> entry : cameraZoneDensities.object2FloatEntrySet()){
			Optional<PropertyModifier<Vec3d>> modifierO = property.apply(entry.getKey().effects());
			if(modifierO.isPresent()){
				PropertyModifier<Vec3d> modifier = modifierO.get();
				float weight = modifier.factor(entry.getFloatValue());
				totalColor = totalColor.add(modifier.target().multiply(weight));
				totalWeight += weight;
			}
		}
		if(totalWeight > 0){
			totalColor = totalColor.multiply(1 / totalWeight);
			return original.lerp(totalColor, Math.min(1, totalWeight));
		}
		return original;
	}
	
	private static <T> @NotNull Optional<T> sampleCameraValue(World world, Function<ZoneEffect, Optional<PropertyModifier<T>>> property){
		if(cameraZoneDensities.isEmpty())
			return Optional.empty();
		float totalWeight = 0;
		// weighted reservoir sampling
		// for no particular reason other than fun
		T chosen = null;
		for(Object2FloatMap.Entry<Zone> entry : cameraZoneDensities.object2FloatEntrySet()){
			Optional<PropertyModifier<T>> modifierO = property.apply(entry.getKey().effects());
			if(modifierO.isPresent()){
				PropertyModifier<T> modifier = modifierO.get();
				float weight = modifier.factor(entry.getFloatValue());
				totalWeight += weight;
				if(weight > 0){
					float chance = weight / totalWeight;
					if(world.random.nextFloat() < chance)
						chosen = modifier.target();
				}
			}
		}
		return Optional.ofNullable(chosen);
	}
}