package arcana.blocks.courier;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.InventoryChangedListener;
import net.minecraft.inventory.SimpleInventory;

public final class InventoryListenerHooks{

	public static void addHook(SimpleInventory inv, InventoryChangedListener listener){
		inv.addListener(listener);
	}
	
	public static <T extends BlockEntity & Inventory> void addHook(T inv, InventoryChangedListener listener){
		// FIXME
	}
}