package arcana.integration.emi;

import arcana.ArcanaRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EmiVoidPuttyAnvilRepairRecipe implements EmiRecipe{
	
	private final Identifier id;
	private final int uniq = ArcanaEmiPlugin.RNG.nextInt();
	
	public EmiVoidPuttyAnvilRepairRecipe(Identifier id){
		this.id = id;
	}
	
	public EmiRecipeCategory getCategory(){
		return VanillaEmiRecipeCategories.ANVIL_REPAIRING;
	}
	
	public @Nullable Identifier getId(){
		return id;
	}
	
	public List<EmiIngredient> getInputs(){
		return List.of(EmiStack.of(ArcanaRegistry.VOID_PUTTY));
	}
	
	public List<EmiStack> getOutputs(){
		return List.of();
	}
	
	public List<EmiIngredient> getCatalysts(){
		return EmiVoidPuttyRepairRecipe.REPAIRABLES_INGREDIENTS;
	}
	
	public boolean supportsRecipeTree(){
		return false;
	}
	
	public int getDisplayWidth(){
		return 125;
	}
	
	public int getDisplayHeight(){
		return 18;
	}
	
	public void addWidgets(WidgetHolder widgets){
		widgets.addTexture(EmiTexture.PLUS, 27, 3);
		widgets.addTexture(EmiTexture.EMPTY_ARROW, 75, 1);
		widgets.addGeneratedSlot(r -> EmiVoidPuttyRepairRecipe.getRepairable(r, true), uniq, 0, 0);
		widgets.addSlot(EmiStack.of(ArcanaRegistry.VOID_PUTTY), 49, 0);
		widgets.addGeneratedSlot(r -> EmiVoidPuttyRepairRecipe.getRepairable(r, false), uniq, 107, 0).recipeContext(this);
	}
}