package arcana.mixin.recipes;

import arcana.recipes.ArcanaRecipe;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.recipebook.ClientRecipeBook;
import net.minecraft.client.recipebook.RecipeBookGroup;
import net.minecraft.recipe.RecipeEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientRecipeBook.class)
@Environment(EnvType.CLIENT)
public class ClientRecipeBookMixin{
	
	@Inject(method = "getGroupForRecipe",
	        at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V", remap = false),
	        cancellable = true)
	private static void hideRecipeBookWarning(RecipeEntry<?> recipe, CallbackInfoReturnable<RecipeBookGroup> cir){
		if(recipe.value() instanceof ArcanaRecipe)
			cir.setReturnValue(RecipeBookGroup.UNKNOWN);
	}
}