package arcana.mixin;

import arcana.aura.WardedChunk;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.DebugHud;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(DebugHud.class)
public class DebugHudMixin{
	
	@Shadow
	private HitResult blockHit;
	
	@Shadow
	@Final
	private MinecraftClient client;
	
	@Inject(at = @At("RETURN"), method = "getRightText")
	protected void getLeftText(CallbackInfoReturnable<List<String>> info){
		if(blockHit.getType() == HitResult.Type.BLOCK){
			BlockPos pos = ((BlockHitResult)blockHit).getBlockPos();
			if(WardedChunk.isWarded(client.world, pos))
				info.getReturnValue().add("[Arcana] Warded block");
		}
	}
}