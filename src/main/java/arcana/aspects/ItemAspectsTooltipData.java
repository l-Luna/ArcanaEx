package arcana.aspects;

import net.minecraft.client.item.TooltipData;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;

public record ItemAspectsTooltipData(List<AspectStack> aspects, @Nullable TooltipData inner) implements TooltipData{
	
	public ItemAspectsTooltipData{
		aspects.sort(Comparator.comparingInt(AspectStack::amount).reversed());
	}
}