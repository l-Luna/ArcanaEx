package arcana.mixin.warding;

import arcana.aura.WardedChunk;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.BlockState;
import net.minecraft.block.FireBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// higher priority to apply after fabric's mixin
@Mixin(value = FireBlock.class, priority = 1001)
public class FireBlockMixin{
	
	@ModifyExpressionValue(method = "trySpreadingFire", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/FireBlock;getSpreadChance(Lnet/minecraft/block/BlockState;)I"))
	int getSpreadChance(int original, World world, BlockPos pos, int spreadFactor, Random random, int currentAge){
		if(WardedChunk.isWarded(world, pos))
			return 0;
		return original;
	}
	
	@ModifyExpressionValue(method = "getBurnChance(Lnet/minecraft/world/WorldView;Lnet/minecraft/util/math/BlockPos;)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/FireBlock;getBurnChance(Lnet/minecraft/block/BlockState;)I"))
	int getBurnChance(int original, WorldView view, BlockPos pos, @Local Direction dir){
		if(view instanceof World world && WardedChunk.isWarded(world, pos.offset(dir)))
			return 0;
		return original;
	}
	
	@ModifyExpressionValue(method = "scheduledTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/FireBlock;isFlammable(Lnet/minecraft/block/BlockState;)Z"))
	boolean scheduledTick_isFlammable(boolean original, BlockState state, ServerWorld world, BlockPos pos, Random random){
		if(WardedChunk.isWarded(world, pos))
			return false;
		return original;
	}
	
	@ModifyExpressionValue(method = "areBlocksAroundFlammable", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/FireBlock;isFlammable(Lnet/minecraft/block/BlockState;)Z"))
	boolean areBlocksAroundFlammable_isFlammable(boolean original, BlockView view, BlockPos pos, @Local Direction direction){
		if(view instanceof World world && WardedChunk.isWarded(world, pos.offset(direction)))
			return false;
		return original;
	}
}