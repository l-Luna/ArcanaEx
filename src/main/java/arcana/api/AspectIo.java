package arcana.api;

import arcana.aspects.AspectStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public interface AspectIo{
	
	// returns the residual, if any
	@Nullable
	AspectStack accept(AspectStack stack, World world, BlockPos pos, Direction from);
	
	@Nullable
	AspectStack draw(int max, World world, BlockPos pos, Direction from);
}