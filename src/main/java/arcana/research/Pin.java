package arcana.research;

import net.minecraft.item.Item;
import org.jetbrains.annotations.Nullable;

public record Pin(Icon icon, Entry entry, int stage, @Nullable Item result){}