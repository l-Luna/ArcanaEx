package arcana.mixin.warding;

import arcana.aura.WardedChunk;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.AbstractBlockState.class)
public class AbstractBlockStateMixin{
	
	@Inject(method = "getHardness", at = @At("HEAD"), cancellable = true)
	void applyWardingHardness(BlockView bv, BlockPos pos, CallbackInfoReturnable<Float> cir){
		if(bv instanceof World world && WardedChunk.isWarded(world, pos))
			cir.setReturnValue(-1f);
	}
	
	@Inject(method = "onStateReplaced", at = @At("HEAD"))
	void removeWardingOnBreak(World world, BlockPos pos, BlockState state, boolean moved, CallbackInfo ci){
		if(WardedChunk.isWarded(world, pos) && state.isAir())
			WardedChunk.setWarded(world, pos, false);
	}
}