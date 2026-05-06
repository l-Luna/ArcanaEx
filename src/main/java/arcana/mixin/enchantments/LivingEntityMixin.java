package arcana.mixin.enchantments;

import arcana.enchantments.LootSwapEnchantment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
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
	                    target = "Lnet/minecraft/loot/LootTable;generateLoot(Lnet/minecraft/loot/context/LootContext;Ljava/util/function/Consumer;)V"),
	           index = 1)
	Consumer<ItemStack> applyLootSwaps(LootContext ctx, Consumer<ItemStack> lootConsumer){
		return ctx.get(LootContextParameters.KILLER_ENTITY) instanceof LivingEntity le
				? LootSwapEnchantment.applyLootSwaps(lootConsumer, le.getMainHandStack(), ctx.getRandom())
				: lootConsumer;
	}
}