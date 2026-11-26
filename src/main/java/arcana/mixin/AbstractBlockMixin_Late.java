package arcana.mixin;

import arcana.enchantments.LootSwapEffect;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

// we want loot swaps to be applied after other mods insert new loot
@Mixin(value = AbstractBlock.class, priority = 2001)
public class AbstractBlockMixin_Late{
	
	@ModifyReturnValue(method = "getDroppedStacks", at = @At("RETURN"))
	public List<ItemStack> autoDrop(List<ItemStack> original, BlockState state, LootContextParameterSet.Builder builder){
		LootContextParameterSet ctx = builder.build(LootContextTypes.BLOCK);
		ItemStack stack = ctx.getOptional(LootContextParameters.TOOL);
		return stack != null ? LootSwapEffect.applyLootSwaps(original, stack, ctx.getWorld().random) : original;
	}
}