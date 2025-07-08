package arcana.mixin;

import arcana.ArcanaRegistry;
import arcana.ArcanaTags;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.tag.FluidTags;
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
	
	// this handles lava flowing into taint goo, i.e. all directions except taint goo falling onto lava
	// see TaintGooFluid itself for that
	@Inject(method = "receiveNeighborFluids", at = @At("HEAD"), cancellable = true)
	void receiveNeighborFluids(World world, BlockPos pos, BlockState state, CallbackInfoReturnable<Boolean> cir){
		if(fluid.isIn(ArcanaTags.TAINT_GOO))
			for(Direction dir : FluidBlock.FLOW_DIRECTIONS){
				BlockPos from = pos.offset(dir.getOpposite());
				if(world.getFluidState(from).isIn(FluidTags.LAVA)){
					world.setBlockState(pos, ArcanaRegistry.TAINT_CRUST.getDefaultState());
					playExtinguishSound(world, pos);
					cir.setReturnValue(false);
				}
			}
	}
}