package arcana.integration.emi;

import arcana.aspects.Aspect;
import arcana.aspects.AspectStack;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.recipe.EmiIngredientRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static arcana.Arcana.arcId;

public class EmiItemsByAspectsRecipe extends EmiIngredientRecipe{
	
	private final List<EmiStack> items;
	private final AspectEmiStack aspect;
	
	public EmiItemsByAspectsRecipe(List<EmiStack> items, AspectEmiStack aspect){
		this.items = items;
		this.aspect = aspect;
	}
	
	public EmiItemsByAspectsRecipe(List<EmiStack> items, Aspect aspect){
		this(items, new AspectEmiStack(new AspectStack(aspect, 1)));
	}
	
	protected EmiIngredient getIngredient(){
		return aspect;
	}
	
	protected List<EmiStack> getStacks(){
		return items;
	}
	
	protected EmiRecipe getRecipeContext(EmiStack stack, int offset){
		return null;
	}
	
	public EmiRecipeCategory getCategory(){
		return ArcanaEmiPlugin.ITEMS_BY_ASPECTS;
	}
	
	public @Nullable Identifier getId(){
		return arcId("items_with/" + EmiUtil.subId(aspect.getId()));
	}
	
	public List<EmiIngredient> getInputs(){
		return new ArrayList<>(items);
	}
	
	public List<EmiStack> getOutputs(){
		return List.of(aspect);
	}
}