package arcana.world.zones;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.function.Function;

public interface DensityProvider{
	
	BiMap<String, MapCodec<? extends DensityProvider>> TYPES = HashBiMap.create();
	Codec<DensityProvider> TYPE_CODEC = Codec
			.<MapCodec<? extends DensityProvider>>stringResolver(TYPES.inverse()::get, TYPES::get)
			.dispatch(DensityProvider::getCodec, Function.identity());
	
	float densityAt(World world, Vec3d position);
	
	MapCodec<? extends DensityProvider> getCodec();
}