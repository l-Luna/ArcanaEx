package arcana.util;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

public final class MathUtil{

	public static Vec3d facingToVec(float pitch, float yaw){
		return new Vec3d(
				-Math.cos(pitch) * Math.sin(yaw),
				-Math.sin(pitch),
				Math.cos(yaw) * Math.cos(pitch)
		);
	}
	
	public static Vec3d facingToVec(Entity entity){
		return facingToVec((float)Math.toRadians(entity.getPitch()), (float)Math.toRadians(entity.getYaw()));
	}
}