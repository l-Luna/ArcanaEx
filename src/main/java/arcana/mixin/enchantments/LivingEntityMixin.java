package arcana.mixin.enchantments;

import arcana.enchantments.LootSwapEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.function.Consumer;

@Mixin(LivingEntity.class)
public class LivingEntityMixin{
	
	// loot swap enchantments
	
	@ModifyArg(method = "dropLoot",
	           at = @At(value = "INVOKE",
	                    target = "Lnet/minecraft/loot/LootTable;generateLoot(Lnet/minecraft/loot/context/LootContextParameterSet;JLjava/util/function/Consumer;)V"),
	           index = 2)
	Consumer<ItemStack> applyLootSwaps(LootContextParameterSet ctx, long seed, Consumer<ItemStack> lootConsumer){
		return ctx.getOptional(LootContextParameters.ATTACKING_ENTITY) instanceof LivingEntity le
				? LootSwapEffect.applyLootSwaps(lootConsumer, le.getMainHandStack(), ctx.getWorld().random)
				: lootConsumer;
	}
}