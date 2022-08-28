package arcana.aspects;

import net.minecraft.client.item.TooltipData;
import net.minecraft.item.ItemStack;

public record WandAspectsTooltipData(ItemStack wand) implements TooltipData{}