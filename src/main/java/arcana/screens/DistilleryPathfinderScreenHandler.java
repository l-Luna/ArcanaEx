package arcana.screens;

import arcana.ArcanaRegistry;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class DistilleryPathfinderScreenHandler extends ScreenHandler{
	
	// "main" material inventory
	private final Inventory inventory;
	// [burn time, max burn time, progress]
	private final PropertyDelegate propertyDelegate;
	
	public DistilleryPathfinderScreenHandler(int syncId, PlayerInventory pInv){
		this(syncId, pInv, new SimpleInventory(1), new SimpleInventory(1), new ArrayPropertyDelegate(3));
	}
	
	public DistilleryPathfinderScreenHandler(int syncId, PlayerInventory pInv, Inventory material, Inventory fuel, PropertyDelegate propertyDelegate){
		super(ArcanaRegistry.DISTILLERY_PATHFINDER_SCREEN_HANDLER, syncId);
		
		this.propertyDelegate = propertyDelegate;
		
		inventory = material;
		inventory.onOpen(pInv.player);
		
		// to-melt slot
		addSlot(new Slot(material, 0, 56, 17));
		
		// fuel slot
		addSlot(new Slot(fuel, 0, 56, 53){
			public boolean canInsert(ItemStack stack){
				return AbstractFurnaceBlockEntity.canUseAsFuel(stack);
			}
		});
		
		// player inventory slots
		for(int row = 0; row < 3; ++row)
			for(int col = 0; col < 9; col++)
				addSlot(new Slot(pInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
		
		for(int idx = 0; idx < 9; idx++)
			addSlot(new Slot(pInv, idx, 8 + idx * 18, 142));
		
		addProperties(propertyDelegate);
	}
	
	public int getBurnTime(){
		return propertyDelegate.get(0);
	}
	
	public int getMaxBurnTime(){
		return propertyDelegate.get(1);
	}
	
	public int getProgress(){
		return propertyDelegate.get(2);
	}
	
	public ItemStack transferSlot(PlayerEntity player, int index){
		return ItemStack.EMPTY;
	}
	
	public boolean canUse(PlayerEntity player){
		return inventory.canPlayerUse(player);
	}
	
	public void close(PlayerEntity player){
		super.close(player);
		inventory.onClose(player);
	}
}