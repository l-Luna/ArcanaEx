package arcana.mixin;

import arcana.ArcanaRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CampfireBlock.class)
public class CampfireBlockMixin{

	// need to use the wand focus!
	@Inject(method = "canBeLit", at = @At("TAIL"), cancellable = true)
	private static void disallowBasicWardedCampfireLighting(BlockState state, CallbackInfoReturnable<Boolean> cir){
		cir.setReturnValue(cir.getReturnValueZ() && !state.isOf(ArcanaRegistry.WARDED_CAMPFIRE));
	}
}