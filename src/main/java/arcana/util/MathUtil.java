package arcana.util;

import net.minecraft.entity.Entity;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColorHelper.Argb;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

import java.util.Arrays;

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
	
	public static int interpColours(int colA, int colB, float delta){
		return MathHelper.packRgb(
				(Argb.getRed(colA) * (1 - delta) + Argb.getRed(colB) * delta) / 255f,
				(Argb.getGreen(colA) * (1 - delta) + Argb.getGreen(colB) * delta) / 255f,
				(Argb.getBlue(colA) * (1 - delta) + Argb.getBlue(colB) * delta) / 255f
		);
	}
	
	private static final int[] DYE_COLOURS = Arrays.stream(DyeColor.values()).mapToInt(DyeColor::getFireworkColor).toArray();
	public static int dyeGradient(float f){
		return interpGradient(f, DYE_COLOURS);
	}
	
	public static int interpGradient(float f, int[] colours){
		// mangled from SheepWoolFeatureRenderer
		int which = (int)f;
		int colA = which % colours.length, colB = (which + 1) % colours.length;
		return interpColours(colours[colA], colours[colB], f - which);
	}
}