package arcana.mixin.recipes;

import arcana.recipes.ArcanaRecipe;
import net.minecraft.client.recipebook.ClientRecipeBook;
import net.minecraft.client.recipebook.RecipeBookGroup;
import net.minecraft.recipe.Recipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientRecipeBook.class)
public class ClientRecipeBookMixin{
	
	@Inject(method = "getGroupForRecipe",
	        at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V"), // TODO: should use `remap = false`?
	        cancellable = true)
	private static void hideRecipeBookWarning(Recipe<?> recipe, CallbackInfoReturnable<RecipeBookGroup> cir){
		if(recipe instanceof ArcanaRecipe)
			cir.setReturnValue(RecipeBookGroup.UNKNOWN);
	}
}