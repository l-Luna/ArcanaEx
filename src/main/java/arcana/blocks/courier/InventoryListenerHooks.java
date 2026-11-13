package arcana.blocks.courier;

import arcana.duck.ArcanaBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.InventoryChangedListener;
import net.minecraft.inventory.SimpleInventory;

public final class InventoryListenerHooks{

	public static void addHook(SimpleInventory inv, InventoryChangedListener listener){
		inv.addListener(listener);
	}
	
	public static void addHook(BlockEntity inv, InventoryChangedListener listener){
		// the mixin implementation checks whether the block is actually an inventory or not already
		((ArcanaBlockEntity)inv).arcana$addListener(listener);
	}
}