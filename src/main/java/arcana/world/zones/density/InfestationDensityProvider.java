package arcana.world.zones.density;

import arcana.world.InfestedChunk;
import arcana.world.zones.DensityProvider;
import com.mojang.serialization.MapCodec;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class InfestationDensityProvider implements DensityProvider{
	
	public static final MapCodec<InfestationDensityProvider> CODEC = MapCodec.unit(new InfestationDensityProvider());
	
	public float densityAt(World world, Vec3d position){
		return InfestedChunk.infestationDensity(world, position);
	}
	
	public MapCodec<? extends DensityProvider> getCodec(){
		return CODEC;
	}
}