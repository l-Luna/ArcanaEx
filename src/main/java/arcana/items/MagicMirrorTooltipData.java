package arcana.items;

import net.minecraft.client.item.TooltipData;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record MagicMirrorTooltipData(@Nullable UUID tag) implements TooltipData{}