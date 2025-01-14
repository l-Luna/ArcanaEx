package arcana.items;

import net.minecraft.item.BowItem;

public class CrimsonLongbowItem extends BowItem{
	
	public CrimsonLongbowItem(Settings settings){
		super(settings);
	}
	
	// BowItemMixin does the actual logic of marking the arrows as projected,
	// and HorizontalConnectingBlockMixin changes their collision
	// it might be better to change usages of collisionShape instead of the implementation,
	// (e.g. to allow including things in the tag that don't use the common superclass,)
	// but it would be less performant for everything else
}