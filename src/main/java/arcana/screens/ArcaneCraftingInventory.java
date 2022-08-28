package arcana.screens;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.screen.ScreenHandler;

public class ArcaneCraftingInventory extends CraftingInventory{
	
	public ArcaneCraftingInventory(ScreenHandler handler, int width, int height){
		super(handler, width, height);
	}
	
	public static class ArcaneCraftingResultInventory extends CraftingResultInventory{}
}