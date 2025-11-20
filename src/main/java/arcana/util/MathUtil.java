package arcana.util;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

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
	
	public static Vec3d randomDir(Random rng){
		// from https://math.stackexchange.com/a/44691
		float theta = rng.nextFloat() * 2 * MathHelper.PI;
		float z = rng.nextFloat() * 2 - 1;
		float u = MathHelper.sqrt(1 - z*z);
		return new Vec3d(
				u * MathHelper.cos(theta),
				u * MathHelper.sin(theta),
				z
		);
	}
	
	public static BlockPos toChunkOffset(BlockPos pos){
		return new BlockPos(pos.getX() & 0b1111, pos.getY(), pos.getZ() & 0b1111);
	}
}