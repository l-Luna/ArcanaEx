package arcana.mixin;

import arcana.items.BootsOfTheTravellerItem;
import com.mojang.authlib.GameProfile;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ClientPlayerEntity.class)
@Environment(EnvType.CLIENT)
public abstract class ClientPlayerEntityMixin extends PlayerEntity{
	
	public ClientPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile){
		super(world, pos, yaw, gameProfile);
	}
	
	// change auto-jump threshold with step assist
	@ModifyArg(method = "autoJump",
	           at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/Vec3d;add(DDD)Lnet/minecraft/util/math/Vec3d;"),
	           index = 1)
	private double changeAutoJumpRequiredHeight(double y){
		if(y == 0.51f && getEquippedStack(EquipmentSlot.FEET).getItem() instanceof BootsOfTheTravellerItem)
			return 1.01;
		return y;
	}
}