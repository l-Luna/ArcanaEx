package arcana.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.util.DyeColor;
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
	
	public static int dyeGradient(float f){
		// from SheepWoolFeatureRenderer
		int which = (int)f;
		float where = f - which;
		int dyes = DyeColor.values().length;
		int dyeA = which % dyes, dyeB = (which + 1) % dyes;
		float[] colA = SheepEntity.getRgbColor(DyeColor.byId(dyeA));
		float[] colB = SheepEntity.getRgbColor(DyeColor.byId(dyeB));
		float r = colA[0] * (1 - where) + colB[0] * where;
		float g = colA[1] * (1 - where) + colB[1] * where;
		float b = colA[2] * (1 - where) + colB[2] * where;
		return MathHelper.packRgb(r, g, b);
	}
}