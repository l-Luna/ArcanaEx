package arcana.client;

import arcana.aspects.AspectStack;
import net.minecraft.client.item.TooltipData;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record AspectsTooltipData(List<AspectStack> aspects, @Nullable TooltipData inner) implements TooltipData{}