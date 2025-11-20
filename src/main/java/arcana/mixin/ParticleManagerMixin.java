package arcana.mixin;

import arcana.ArcanaRegistry;
import arcana.aura.WardedChunk;
import net.minecraft.block.BlockState;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleManager.class)
public abstract class ParticleManagerMixin{
	
	@Shadow
	protected ClientWorld world;
	
	@Inject(method = "addBlockBreakingParticles", at = @At("HEAD"))
	void applyWardingBlockBreakingEffect(BlockPos pos, Direction direction, CallbackInfo ci){
		if(WardedChunk.isWarded(world, pos))
			world.addParticle(ArcanaRegistry.WARDING_EFFECT, pos.getX(), pos.getY(), pos.getZ(), 0, 0, 0);
	}
	
	@Inject(method = "addBlockBreakParticles", at = @At("HEAD"))
	void applyWardingBlockBreakEffect(BlockPos pos, BlockState state, CallbackInfo ci){
		if(WardedChunk.isWarded(world, pos))
			world.addParticle(ArcanaRegistry.WARDING_EFFECT, pos.getX(), pos.getY(), pos.getZ(), 0, 0, 0);
	}
}