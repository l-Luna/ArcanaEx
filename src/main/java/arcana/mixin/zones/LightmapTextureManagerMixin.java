package arcana.mixin.zones;

import arcana.client.ZoneEffects;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LightmapTextureManager.class)
@Environment(EnvType.CLIENT)
public class LightmapTextureManagerMixin{

	@ModifyReturnValue(method = "getDarkness", at = @At("RETURN"))
	float applyZoneLightModifier(float original, LivingEntity entity, float factor, float delta){
		return original - ZoneEffects.lightModifier(entity.getWorld(), entity.getEyePos());
	}
}