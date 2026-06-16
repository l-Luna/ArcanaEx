package arcana.mixin.recipes;

import arcana.ArcanaRegistry;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potions;
import net.minecraft.recipe.BrewingRecipeRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(BrewingRecipeRegistry.class)
public class BrewingRecipeRegistryMixin{
	
	@Unique
	private static boolean isSilverleafRecipe(ItemStack input, ItemStack ingredient){
		return input.isOf(Items.POTION)
				&& input.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT).potion().equals(Optional.of(Potions.WATER))
				&& ingredient.isOf(ArcanaRegistry.SILVERLEAF)
				&& ingredient.getCount() >= 8;
	}
	
	@ModifyReturnValue(method = "isValidIngredient", at = @At("TAIL"))
	private static boolean isValidIngredient(boolean original, ItemStack stack){
		return original || stack.getItem() == ArcanaRegistry.SILVERLEAF;
	}
	
	@ModifyReturnValue(method = "hasItemRecipe", at = @At("TAIL"))
	private static boolean hasItemRecipe(boolean original, ItemStack input, ItemStack ingredient){
		return original || isSilverleafRecipe(input, ingredient);
	}
	
	@Inject(method = "craft", at = @At("TAIL"), cancellable = true)
	private static void craft(ItemStack ingredient, ItemStack input, CallbackInfoReturnable<ItemStack> cir){
		if(isSilverleafRecipe(input, ingredient))
			cir.setReturnValue(new ItemStack(ArcanaRegistry.SILVERLEAF_BREW));
	}
}