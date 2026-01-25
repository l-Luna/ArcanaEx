package arcana.recipes;

import arcana.api.Cap;
import arcana.api.Core;
import arcana.items.WandItem;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class WandRecipe extends SpecialCraftingRecipe{
	
	public static final RecipeSerializer<WandRecipe> SERIALIZER = new SpecialRecipeSerializer<>(WandRecipe::new);
	
	public WandRecipe(Identifier id){
		super(id);
	}
	
	public boolean matches(CraftingInventory inventory, World world){
		Cap caps = null;
		// cap in top left
		if(inventory.getStack(0).getItem() instanceof Cap c)
			caps = c;
		// same cap in top right
		if(inventory.getStack(8).getItem() != caps)
			return false;
		// core in middle
		if(Core.asCore(inventory.getStack(4).getItem()) == null)
			return false;
		// nothing else
		for(int i = 0; i < 9; i++)
			if(i != 0 && i != 4 && i != 8)
				if(!inventory.getStack(i).isEmpty())
					return false;
		return true;
	}
	
	public ItemStack craft(CraftingInventory inventory){
		Cap cap = (Cap)inventory.getStack(0).getItem();
		Core core = Core.asCore(inventory.getStack(4).getItem());
		return WandItem.withCapAndCore(cap, core);
	}
	
	public boolean fits(int width, int height){
		return width >= 3 && height >= 3;
	}
	
	public RecipeSerializer<?> getSerializer(){
		return SERIALIZER;
	}
}