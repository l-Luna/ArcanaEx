package arcana.mixin.accessor;

import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.SmithingTransformRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SmithingTransformRecipe.class)
public interface SmithingTransformRecipeAccessor{
	
	@Accessor("template")
	Ingredient arcana$getTemplate();
	
	@Accessor("base")
	Ingredient arcana$getBase();
	
	@Accessor("addition")
	Ingredient arcana$getAddition();
}