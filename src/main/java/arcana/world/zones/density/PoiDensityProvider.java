package arcana.world.zones.density;

import arcana.world.zones.DensityProvider;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Map;

public class PoiDensityProvider implements DensityProvider{
	
	public static final MapCodec<PoiDensityProvider> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			Codec.unboundedMap(Identifier.CODEC, Codec.FLOAT).fieldOf("weights").forGetter(it -> it.poiWeights)
	).apply(i, PoiDensityProvider::new));
	
	private final Object2FloatMap<Identifier> poiWeights;
	
	public PoiDensityProvider(Map<Identifier, Float> weights){
		poiWeights = new Object2FloatOpenHashMap<>(weights);
	}
	
	public float densityAt(World world, Vec3d position){
		// TODO: POIs are not actually synced, need to cache this info ourselves
		return 0;
	}
	
	public MapCodec<? extends DensityProvider> getCodec(){
		return CODEC;
	}
}