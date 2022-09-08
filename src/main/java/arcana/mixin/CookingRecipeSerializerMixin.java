package arcana.mixin;

import com.google.gson.JsonObject;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.CookingRecipeSerializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CookingRecipeSerializer.class)
public class CookingRecipeSerializerMixin{
	
	@SuppressWarnings("rawtypes")
	@Inject(method = "read(Lnet/minecraft/util/Identifier;Lcom/google/gson/JsonObject;)Lnet/minecraft/recipe/AbstractCookingRecipe;",
	        at = @At("RETURN"))
	private void applyArcanaCookingRecipeAmount(Identifier identifier, JsonObject jsonObject, CallbackInfoReturnable cir){
		AbstractCookingRecipe cr = (AbstractCookingRecipe)cir.getReturnValue();
		if(jsonObject.has("arcana:amount")){
			int amount = JsonHelper.getInt(jsonObject, "arcana:amount");
			cr.getOutput().setCount(amount);
		}
	}
}