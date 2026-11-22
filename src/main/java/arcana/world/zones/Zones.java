package arcana.world.zones;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class Zones{

	public static float getZoneDensity(World world, Zone zone, BlockPos position){
		// cache?
		return getZoneDensity(world, zone, Vec3d.ofCenter(position));
	}
	
	public static float getZoneDensity(World world, Zone zone, Vec3d position){
		return zone.densityProvider().densityAt(world, position);
	}
}