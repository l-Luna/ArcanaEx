package arcana.integration.emi;

import arcana.ArcanaRegistry;
import arcana.recipes.crafting.VoidPuttyRepairRecipe;
import dev.emi.emi.api.recipe.EmiPatternCraftingRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.GeneratedSlotWidget;
import dev.emi.emi.api.widget.SlotWidget;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Random;

public class EmiVoidPuttyRepairRecipe extends EmiPatternCraftingRecipe{
	
	public static List<ItemStack> REPAIRABLES;
	public static List<EmiIngredient> REPAIRABLES_INGREDIENTS;
	
	private static void initRepairables(){
		REPAIRABLES = Registries.ITEM.stream().map(Item::getDefaultStack).filter(VoidPuttyRepairRecipe::isRepairable).toList();
		REPAIRABLES_INGREDIENTS = REPAIRABLES.stream().map(EmiStack::of).map(EmiIngredient.class::cast).toList();
	}
	
	public EmiVoidPuttyRepairRecipe(Identifier id){
		super(List.of(EmiStack.of(ArcanaRegistry.VOID_PUTTY)), EmiStack.EMPTY, id, true);
		initRepairables();
	}
	
	public SlotWidget getInputWidget(int slot, int x, int y){
		if(slot == 0)
			return new GeneratedSlotWidget(rng -> getRepairable(rng, true), unique, x, y);
		else if(slot == 1)
			return new SlotWidget(EmiStack.of(ArcanaRegistry.VOID_PUTTY), x, y);
		return new SlotWidget(EmiStack.EMPTY, x, y);
	}
	
	public SlotWidget getOutputWidget(int x, int y){
		return new GeneratedSlotWidget(rng -> getRepairable(rng, false), unique, x, y);
	}
	
	public List<EmiIngredient> getCatalysts(){
		return REPAIRABLES_INGREDIENTS;
	}
	
	public static EmiStack getRepairable(Random rng, boolean damaged){
		ItemStack stack = REPAIRABLES.get(rng.nextInt(REPAIRABLES.size())).copy();
		if(damaged && stack.getMaxDamage() > 2)
			stack.setDamage(rng.nextInt(1, stack.getMaxDamage() - 1));
		return EmiStack.of(stack);
	}
}