package arcana.screens;

import arcana.ArcanaRegistry;
import arcana.ArcanaTags;
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

public class ArcaneFurnaceScreenHandler extends ScreenHandler{
	
	// "main" material inventory
	private final Inventory inventory;
	// [burn time, max burn time, substrate amount, max substrate amount, substrate colour, progress, max progress, aspect total]
	private final PropertyDelegate propertyDelegate;
	
	public ArcaneFurnaceScreenHandler(int syncId, PlayerInventory pInv){
		this(syncId, pInv, new SimpleInventory(1), new SimpleInventory(1), new SimpleInventory(1), new SimpleInventory(1), new ArrayPropertyDelegate(8));
	}
	
	public ArcaneFurnaceScreenHandler(int syncId, PlayerInventory pInv, Inventory material, Inventory fuel, Inventory substrate, Inventory husks, PropertyDelegate propertyDelegate){
		super(ArcanaRegistry.ARCANE_FURNACE_SCREEN_HANDLER, syncId);
		
		this.propertyDelegate = propertyDelegate;
		
		inventory = material;
		inventory.onOpen(pInv.player);
		
		// to-melt slot
		addSlot(new Slot(material, 0, 43, 10));
		
		// fuel slot
		addSlot(new Slot(fuel, 0, 31, 48){
			public boolean canInsert(ItemStack stack){
				return AbstractFurnaceBlockEntity.canUseAsFuel(stack);
			}
		});
		
		// substrate slot
		addSlot(new Slot(substrate, 0, 55, 48){
			public boolean canInsert(ItemStack stack){
				return stack.isIn(ArcanaTags.SUBSTRATES);
			}
		});
		
		// husks slot
		addSlot(new Slot(husks, 0, 107, 28){
			public boolean canInsert(ItemStack stack){
				return false;
			}
		});
		
		// player inventory slots
		for(int row = 0; row < 3; ++row)
			for(int col = 0; col < 9; col++)
				addSlot(new Slot(pInv, col + row * 9 + 9, 8 + col * 18, 81 + row * 18));
		
		for(int idx = 0; idx < 9; idx++)
			addSlot(new Slot(pInv, idx, 8 + idx * 18, 139));
		
		addProperties(propertyDelegate);
	}
	
	public int getBurnTime(){
		return propertyDelegate.get(0);
	}
	
	public int getMaxBurnTime(){
		return propertyDelegate.get(1);
	}
	
	public int getSubstrateAmount(){
		return propertyDelegate.get(2);
	}
	
	public int getMaxSubstrateAmount(){
		return propertyDelegate.get(3);
	}
	
	public int getSubstrateColour(){
		return propertyDelegate.get(4);
	}
	
	public int getProgres(){
		return propertyDelegate.get(5);
	}
	
	public int getMaxProgress(){
		return propertyDelegate.get(6);
	}
	
	public int getAspectTotal(){
		return propertyDelegate.get(7);
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