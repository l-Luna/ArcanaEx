package arcana.aspects;

import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipData;

public record WandAspectsTooltipData(ItemStack wand) implements TooltipData{}