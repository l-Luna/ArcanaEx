package arcana.duck;

import arcana.fluids.ArcanaFluid;
import org.jetbrains.annotations.Nullable;

public interface ArcanaFluidEntity{
	
	@Nullable ArcanaFluid arcana$getMaxSubmergedFluid();
	double arcana$getMaxFluidHeight();
}