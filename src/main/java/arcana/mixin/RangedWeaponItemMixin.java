package arcana.mixin;

import arcana.cca_components.CaArrow;
import arcana.items.CrimsonLongbowItem;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(RangedWeaponItem.class)
public class RangedWeaponItemMixin{
	
	@WrapOperation(method = "shootAll",
	               at = @At(value = "INVOKE",
	                        target = "Lnet/minecraft/server/world/ServerWorld;spawnEntity(Lnet/minecraft/entity/Entity;)Z"))
	boolean projectArrow(ServerWorld instance, Entity entity, Operation<Boolean> original, ServerWorld world,
	                     LivingEntity shooter,
	                     Hand hand,
	                     ItemStack stack,
	                     List<ItemStack> projectiles,
	                     float speed,
	                     float divergence,
	                     boolean critical,
	                     @Nullable LivingEntity target){
		if(entity instanceof ArrowEntity ae && stack.getItem() instanceof CrimsonLongbowItem)
			CaArrow.setProjected(ae, true);
		return false;
	}
}