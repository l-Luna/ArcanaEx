package arcana.mixin;

import arcana.ArcanaRegistry;
import arcana.ArcanaTags;
import arcana.duck.ProjectedBlockHitResult;
import arcana.entities.wisps.WispEntity;
import arcana.util.InventoryUtil;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin{

	@Unique
	private static boolean hasExtendedPlacement(ItemStack stack){
		return stack.getItem() instanceof BlockItem || stack.isIn(ArcanaTags.PLANE_PROJECTION_WHITELIST);
	}
	
	@Inject(method = "method_18144", at = @At("HEAD"), cancellable = true)
	private static void handleWispAttackValidity(Entity entity, CallbackInfoReturnable<Boolean> cir){
		if(entity instanceof WispEntity)
			cir.setReturnValue(MinecraftClient.getInstance().player.getMainHandStack().isIn(ArcanaTags.WISP_ATTACK_WHITELIST));
	}
	
	@WrapOperation(method = "updateTargetedEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;raycast(DFZ)Lnet/minecraft/util/hit/HitResult;"))
	HitResult handlePlaneProjection(Entity instance, double maxDistance, float tickDelta, boolean includeFluids, Operation<HitResult> original){
		HitResult value = original.call(instance, maxDistance, tickDelta, includeFluids);
		if(value instanceof BlockHitResult bhr
				&& bhr.getType() == HitResult.Type.MISS
				&& instance instanceof PlayerEntity player
				&& (hasExtendedPlacement(player.getMainHandStack()) || hasExtendedPlacement(player.getOffHandStack()))
				&& InventoryUtil.hasTrinket(player, ArcanaRegistry.PLANE_PROJECTION_RING)){
			if(player.isSneaking() && player.raycast(2, tickDelta, includeFluids) instanceof BlockHitResult bhrMini)
				return new ProjectedBlockHitResult(bhrMini);
			return new ProjectedBlockHitResult(bhr);
		}
		return value;
	}
}