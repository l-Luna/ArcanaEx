package arcana.screens;

import arcana.util.MnemonicInventoryData;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import org.jetbrains.annotations.Nullable;

public class MnemonicSlot extends Slot{
	
	private final MnemonicInventoryData mnemonics;
	
	public MnemonicSlot(Inventory inventory, int index, int x, int y, MnemonicInventoryData mnemonics){
		super(inventory, index, x, y);
		this.mnemonics = mnemonics;
	}
	
	public @Nullable ItemStack getRememberedShape(){
		return mnemonics.getRememberedShapes().get(getIndex());
	}
}