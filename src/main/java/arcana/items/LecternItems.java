package arcana.items;

import arcana.ArcanaRegistry;
import net.minecraft.item.Item;

import java.util.HashSet;
import java.util.Set;

public final class LecternItems{
	
	public static final Set<Item> LECTERN_ITEMS = new HashSet<>(Set.of(
			ArcanaRegistry.SCRIBBLED_NOTES,
			ArcanaRegistry.ARCANUM,
			ArcanaRegistry.CRIMSON_RITES
	));
}