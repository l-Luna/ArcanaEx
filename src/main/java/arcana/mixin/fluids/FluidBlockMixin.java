package arcana.mixin.fluids;

import arcana.fluids.ArcanaFluid;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FluidBlock.class)
public abstract class FluidBlockMixin{
	
	@Shadow
	@Final
	protected FlowableFluid fluid;
	
	@Shadow
	protected abstract void playExtinguishSound(WorldAccess world, BlockPos pos);
	
	@Shadow
	public abstract FluidState getFluidState(BlockState state);
	
	// this handles lava flowing into taint goo (or equiv.), i.e. all directions except taint goo falling onto lava
	// see ArcanaFluid itself for that
	@Inject(method = "receiveNeighborFluids", at = @At("HEAD"), cancellable = true)
	void receiveNeighborFluids(World world, BlockPos pos, BlockState state, CallbackInfoReturnable<Boolean> cir){
		if(fluid instanceof ArcanaFluid af)
			for(Direction dir : FluidBlock.FLOW_DIRECTIONS){
				BlockPos from = pos.offset(dir.getOpposite());
				FluidState selfState = getFluidState(state), otherState = world.getFluidState(from);
				var interaction = af.interact(selfState, otherState);
				if(interaction.isPresent()){
					world.setBlockState(pos, interaction.get());
					playExtinguishSound(world, pos);
					cir.setReturnValue(false);
				}
			}
	}
}