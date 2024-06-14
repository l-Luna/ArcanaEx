package arcana.screens;

import arcana.ArcanaRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class CrystallizationPressScreenHandler extends ScreenHandler{
	
	// [quartz amount, essentia amount, essentia colour, progress]
	private final PropertyDelegate propertyDelegate;
	private final Inventory quartzInv;
	
	public CrystallizationPressScreenHandler(int syncId, PlayerInventory pInv){
		this(syncId, pInv, new SimpleInventory(1), new SimpleInventory(1), new ArrayPropertyDelegate(4));
	}
	
	public CrystallizationPressScreenHandler(int syncId, PlayerInventory pInv, Inventory quartz, Inventory output, PropertyDelegate properties){
		super(ArcanaRegistry.CRYSTALLIZATION_PRESS_SCREEN_HANDLER, syncId);
		this.propertyDelegate = properties;
		this.quartzInv = quartz;
		
		quartzInv.onOpen(pInv.player);
		
		// quartz slot
		addSlot(new Slot(quartz, 0, 22, 31){
			public boolean canInsert(ItemStack stack){
				return stack.isOf(Items.QUARTZ);
			}
		});
		
		// output slot
		addSlot(new Slot(output, 0, 137, 31){
			public boolean canInsert(ItemStack stack){
				return false;
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
	
	public int getQuartzAmount(){
		return propertyDelegate.get(0);
	}
	
	public int getEssentiaAmount(){
		return propertyDelegate.get(1);
	}
	
	public int getEssentiaColour(){
		return propertyDelegate.get(2);
	}
	
	public int getProgress(){
		return propertyDelegate.get(3);
	}
	
	public ItemStack transferSlot(PlayerEntity player, int index){
		return ItemStack.EMPTY;
	}
	
	public boolean canUse(PlayerEntity player){
		return quartzInv.canPlayerUse(player);
	}
	
	public void close(PlayerEntity player){
		super.close(player);
		quartzInv.onClose(player);
	}
}