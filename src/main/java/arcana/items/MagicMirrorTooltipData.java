package arcana.items;

import net.minecraft.item.tooltip.TooltipData;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record MagicMirrorTooltipData(@Nullable UUID tag) implements TooltipData{}