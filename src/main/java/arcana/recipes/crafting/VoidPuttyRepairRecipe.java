package arcana.recipes.crafting;

import arcana.ArcanaRegistry;
import arcana.ArcanaTags;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;

import static arcana.util.InventoryUtil.streamInventory;

// inspired by:
// https://github.com/DaFuqs/Spectrum/blob/1.20.1-aria-for-painters/src/main/java/de/dafuqs/spectrum/recipe/crafting/dynamic/RepairAnythingRecipe.java
// more limited; only applies to repairable tools + items in whitelist, less upkeep than a blacklist
public class VoidPuttyRepairRecipe extends SpecialCraftingRecipe{
	
	public static final RecipeSerializer<VoidPuttyRepairRecipe> SERIALIZER = new SpecialRecipeSerializer<>(VoidPuttyRepairRecipe::new);
	
	private static final Ingredient VOID_PUTTY = Ingredient.ofItems(ArcanaRegistry.VOID_PUTTY);
	
	public VoidPuttyRepairRecipe(CraftingRecipeCategory group){
		super(group);
	}
	
	public boolean matches(CraftingRecipeInput inventory, World world){
		return streamInventory(inventory).filter(VOID_PUTTY).count() == 1 &&
				streamInventory(inventory).filter(VoidPuttyRepairRecipe::isRepairable).count() == 1;
	}
	
	@SuppressWarnings("OptionalGetWithoutIsPresent")
	public ItemStack craft(CraftingRecipeInput inventory, RegistryWrapper.WrapperLookup lookup){
		ItemStack newStack = streamInventory(inventory).filter(VoidPuttyRepairRecipe::isRepairable).findAny().get().copy();
		newStack.setDamage(0);
		return newStack;
	}
	
	public boolean fits(int width, int height){
		return width * height >= 2;
	}
	
	public RecipeSerializer<?> getSerializer(){
		return SERIALIZER;
	}
	
	public static boolean isRepairable(ItemStack item){
		if(item.getMaxDamage() <= 0)
			return false;
		return item.getRegistryEntry().isIn(ArcanaTags.VOID_PUTTY_REPAIR_WHITELIST)
				|| item.getItem() instanceof ToolItem tool && !tool.getMaterial().getRepairIngredient().isEmpty()
				|| item.getItem() instanceof ArmorItem armor && !armor.getMaterial().value().repairIngredient().get().isEmpty();
	}
}