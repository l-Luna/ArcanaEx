package arcana.fluids;

import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockView;

public class FluidUtil{
	
	// adapted from FluidRenderer
	
	public static float calculateLerpedFluidHeight(BlockView world, Fluid fluid, BlockPos pos, float fracX, float fracZ){
		float bH = getFluidBaseHeight(world, fluid, pos),
		      wH = getFluidBaseHeight(world, fluid, pos.west()),
		      eH = getFluidBaseHeight(world, fluid, pos.east()),
		      nH = getFluidBaseHeight(world, fluid, pos.north()),
		      sH = getFluidBaseHeight(world, fluid, pos.south());
		float northH = MathHelper.lerp(fracX, calculateLerpedFluidHeight(world, fluid, bH, wH, nH, pos.west().north()), calculateLerpedFluidHeight(world, fluid, bH, eH, nH, pos.east().north()));
		float southH = MathHelper.lerp(fracX, calculateLerpedFluidHeight(world, fluid, bH, wH, sH, pos.west().south()), calculateLerpedFluidHeight(world, fluid, bH, eH, sH, pos.east().south()));
		return MathHelper.lerp(fracZ, northH, southH);
	}
	
	public static float calculateLerpedFluidHeight(BlockView world, Fluid fluid, float centreHeight, float xHeight, float zHeight, BlockPos diagonal){
		if(!(zHeight >= 1) && !(xHeight >= 1)){
			float[] fs = new float[2];
			if(zHeight > 0 || xHeight > 0){
				float diagonalH = getFluidBaseHeight(world, fluid, diagonal);
				if(diagonalH >= 1)
					return 1;
				
				addHeight(fs, diagonalH);
			}
			
			addHeight(fs, centreHeight);
			addHeight(fs, zHeight);
			addHeight(fs, xHeight);
			return fs[0] / fs[1];
		}else
			return 1;
	}
	
	private static void addHeight(float[] avg, float height){
		if(height >= 0.8f){
			avg[0] += height * 10;
			avg[1] += 10;
		}else if(height >= 0){
			avg[0] += height;
			avg[1]++;
		}
	}
	
	private static float getFluidBaseHeight(BlockView world, Fluid fluid, BlockPos pos){
		BlockState state = world.getBlockState(pos);
		return getFluidBaseHeight(world, fluid, pos, state, state.getFluidState());
	}
	
	private static float getFluidBaseHeight(BlockView world, Fluid fluid, BlockPos pos, BlockState bstate, FluidState fstate){
		if(fluid.matchesType(fstate.getFluid()))
			return fluid.matchesType(world.getBlockState(pos.up()).getFluidState().getFluid()) ? 1.0F : fstate.getHeight();
		else
			return !bstate.getMaterial().isSolid() ? 0 : -1;
	}
}