package arcana.mixin;

import com.google.gson.JsonObject;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets="net/minecraft/client/render/model/json/ModelElement$Deserializer")
@Environment(EnvType.CLIENT)
public abstract class ModelElementDeserializerMixin{
	
	@Shadow
	protected abstract Vector3f deserializeVec3f(JsonObject object, String name);
	
	@Inject(method = "deserializeFrom", at = @At("HEAD"), cancellable = true)
	void deserializeFrom(JsonObject object, CallbackInfoReturnable<Vector3f> cir){
		if(object.has("arcana:unlock_size") && object.get("arcana:unlock_size").getAsBoolean())
			cir.setReturnValue(deserializeVec3f(object, "from"));
	}
	
	@Inject(method = "deserializeTo", at = @At("HEAD"), cancellable = true)
	void deserializeTo(JsonObject object, CallbackInfoReturnable<Vector3f> cir){
		if(object.has("arcana:unlock_size") && object.get("arcana:unlock_size").getAsBoolean())
			cir.setReturnValue(deserializeVec3f(object, "to"));
	}
}