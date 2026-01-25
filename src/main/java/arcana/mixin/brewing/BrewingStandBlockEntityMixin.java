package arcana.mixin.brewing;

import arcana.ArcanaRegistry;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.block.entity.BrewingStandBlockEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BrewingStandBlockEntity.class)
public class BrewingStandBlockEntityMixin{
	
	@WrapOperation(method = "craft", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;decrement(I)V"))
	private static void craft(ItemStack stack, int amount, Operation<Void> original){
		if(stack.isOf(ArcanaRegistry.SILVERLEAF))
			stack.decrement(8);
		else original.call(stack, amount);
	}
}