package arcana.mixin.recipes;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.CookingRecipeSerializer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;
import java.util.function.Function;

@Mixin(CookingRecipeSerializer.class)
public class CookingRecipeSerializerMixin<T extends AbstractCookingRecipe>{
	
	// extend the codec with an `arcana:amount` field
	@ModifyExpressionValue(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/codecs/RecordCodecBuilder;mapCodec(Ljava/util/function/Function;)Lcom/mojang/serialization/MapCodec;", remap = false))
	MapCodec<T> addAmountField(MapCodec<T> original){
		return RecordCodecBuilder.mapCodec(i -> i
				.group(
						original.forGetter(Function.identity()),
						Codec.INT.optionalFieldOf("arcana:amount").forGetter(x -> Optional.of(x.getResult(null).getCount())))
				.apply(i, (T recipe, Optional<Integer> amount) -> {
					amount.ifPresent(it -> recipe.getResult(null).setCount(it));
					return recipe;
				}));
	}
}