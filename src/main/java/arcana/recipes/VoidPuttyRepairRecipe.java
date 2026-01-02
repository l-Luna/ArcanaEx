package arcana.recipes;

import arcana.ArcanaRegistry;
import arcana.ArcanaTags;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.stream.IntStream;
import java.util.stream.Stream;

// inspired by:
// https://github.com/DaFuqs/Spectrum/blob/1.20.1-aria-for-painters/src/main/java/de/dafuqs/spectrum/recipe/crafting/dynamic/RepairAnythingRecipe.java
// more limited; only applies to repairable tools + items in whitelist, less upkeep than a blacklist
public class VoidPuttyRepairRecipe extends SpecialCraftingRecipe{
	
	public static final RecipeSerializer<VoidPuttyRepairRecipe> SERIALIZER = new SpecialRecipeSerializer<>(VoidPuttyRepairRecipe::new);
	
	private static final Ingredient VOID_PUTTY = Ingredient.ofItems(ArcanaRegistry.VOID_PUTTY);
	
	public VoidPuttyRepairRecipe(Identifier id){
		super(id);
	}
	
	public boolean matches(CraftingInventory inventory, World world){
		return stream(inventory).filter(VOID_PUTTY).count() == 1 &&
				stream(inventory).filter(VoidPuttyRepairRecipe::isRepairable).count() == 1;
	}
	
	@SuppressWarnings("OptionalGetWithoutIsPresent")
	public ItemStack craft(CraftingInventory inventory){
		ItemStack newStack = stream(inventory).filter(VoidPuttyRepairRecipe::isRepairable).findAny().get().copy();
		newStack.setDamage(0);
		return newStack;
	}
	
	public boolean fits(int width, int height){
		return width * height >= 2;
	}
	
	public RecipeSerializer<?> getSerializer(){
		return SERIALIZER;
	}
	
	public static boolean isRepairable(ItemStack stack){
		return isRepairable(stack.getItem());
	}
	
	public static boolean isRepairable(Item item){
		if(item.getMaxDamage() <= 0)
			return false;
		return item.getRegistryEntry().isIn(ArcanaTags.VOID_PUTTY_REPAIR_WHITELIST)
				|| item instanceof ToolItem tool && !tool.getMaterial().getRepairIngredient().isEmpty()
				|| item instanceof ArmorItem armor && !armor.getMaterial().getRepairIngredient().isEmpty();
	}
	
	private static Stream<ItemStack> stream(Inventory i){
		return IntStream.range(0, i.size()).mapToObj(i::getStack);
	}
}