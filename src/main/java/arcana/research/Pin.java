package arcana.research;

import net.minecraft.item.Item;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record Pin(Icon icon, Entry entry, int stage, @Nullable Item result, @Nullable List<Text> tooltip){}