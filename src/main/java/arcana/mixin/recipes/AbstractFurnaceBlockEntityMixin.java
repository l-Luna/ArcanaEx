package arcana.mixin.recipes;

import arcana.Arcana;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractFurnaceBlockEntity.class)
public class AbstractFurnaceBlockEntityMixin{
	
	@Inject(method = "craftRecipe", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;increment(I)V"))
	private static void applyCookingRecipeAmount(Recipe<?> recipe, DefaultedList<ItemStack> slots, int count, CallbackInfoReturnable<Boolean> cir){
		ItemStack inputSlot = slots.get(0);
		// don't mess with non-arcana recipes
		if(!Registry.ITEM.getId(inputSlot.getItem()).getNamespace().equals(Arcana.MODID))
			return;
		ItemStack outputSlot = slots.get(2);
		// MC's already added 1, just add remainder
		outputSlot.increment(recipe.getOutput().getCount() - 1);
	}
}