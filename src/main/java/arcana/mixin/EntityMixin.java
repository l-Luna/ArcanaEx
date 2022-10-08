package arcana.mixin;

import arcana.items.BootsOfTheTravellerItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Entity.class)
public class EntityMixin{
	
	// inspired by Pehkui's implementation:
	// https://github.com/Virtuoel/Pehkui/blob/forge/1.19.2/src/main/java/virtuoel/pehkui/mixin/step_height/EntityMixin.java
	
	@Redirect(method = "adjustMovementForCollisions(Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;",
	          at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;stepHeight:F"))
	private float stepHeight(Entity self){
		var orig = originalStepHeight(self);
		if(self instanceof LivingEntity le && le.getEquippedStack(EquipmentSlot.FEET).getItem() instanceof BootsOfTheTravellerItem)
			return Math.max(orig, 1);
		else return orig;
	}
	
	@Unique
	private static float originalStepHeight(Entity e){
		return e.stepHeight;
	}
}