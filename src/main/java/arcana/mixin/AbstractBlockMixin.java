package arcana.mixin;

import arcana.blocks.ArcanaBlockSettings;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.context.LootContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

// we want other modifiers to return value to see the block loot at all
@Mixin(value = AbstractBlock.class, priority = 1)
public class AbstractBlockMixin{

	// based on lib39 dessicant auto-drops, but using ArcanaBlockSettings
	
	@ModifyReturnValue(method = "getDroppedStacks", at = @At("RETURN"))
	public List<ItemStack> autoDrop(List<ItemStack> original, BlockState state, LootContext.Builder builder){
		if(original.isEmpty()
				&& state.getBlock().settings instanceof ArcanaBlockSettings abs
				&& abs.getDropsSelf()
				&& state.getBlock().asItem() != Items.AIR)
			return List.of(state.getBlock().asItem().getDefaultStack());
		return original;
	}
}