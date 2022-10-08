package arcana.mixin;

import arcana.items.BootsOfTheTravellerItem;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin{
	
	// change auto-jump threshold with step assist
	@ModifyArg(method = "autoJump",
	           at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/Vec3d;add(DDD)Lnet/minecraft/util/math/Vec3d;"),
	           index = 1)
	private double changeAutoJumpRequiredHeight(double y){
		if(y == 0.51f && ((LivingEntity)(Object)this).getEquippedStack(EquipmentSlot.FEET).getItem() instanceof BootsOfTheTravellerItem)
			return 1.01;
		return y;
	}
}