package arcana.world.zones;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;

import static arcana.Arcana.arcId;

public record Zone(
		DensityProvider densityProvider,
		ZoneEffect effects
){
	
	public static final RegistryKey<Registry<Zone>> REGISTRY_KEY = RegistryKey.ofRegistry(arcId("zone"));
	
	public static final Codec<Zone> DIRECT_CODEC = RecordCodecBuilder.create(i -> i.group(
			DensityProvider.TYPE_CODEC.fieldOf("density_provider").forGetter(Zone::densityProvider),
			ZoneEffect.CODEC.fieldOf("effects").forGetter(Zone::effects)
	).apply(i, Zone::new));
	
	public static Registry<Zone> getRegistry(World world){
		return world.getRegistryManager().get(REGISTRY_KEY);
	}
}