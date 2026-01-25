package arcana.mixin;

import arcana.ArcanaRegistry;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CampfireBlock.class)
public class CampfireBlockMixin{

	@ModifyReturnValue(method = "canBeLit", at = @At("TAIL"))
	private static boolean disallowBasicMagicalCampfireLighting(boolean original, BlockState state){
		return original && !state.isOf(ArcanaRegistry.WARDED_CAMPFIRE) && !state.isOf(ArcanaRegistry.CRIMSON_CAMPFIRE);
	}
}