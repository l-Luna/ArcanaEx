package arcana.duck;

import net.minecraft.item.ItemStack;

public interface ArcanaPlayerEntity{
	
	void arcana$setDeathStashedMirrorStack(ItemStack stack);
	
	ItemStack arcana$getDeathStashedMirrorStack();
}