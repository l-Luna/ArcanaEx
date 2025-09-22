package arcana.integration.emi;

import arcana.aspects.AspectStack;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.util.Identifier;

import java.util.List;

public class EmiAspectsByItemsRecipe extends GenericEmiConversionRecipe{
	
	public EmiAspectsByItemsRecipe(EmiIngredient item, List<AspectStack> aspects, Identifier baseId){
		super(item, aspects.stream().map(x -> (EmiStack)new AspectEmiStack(x)).toList(), baseId);
	}
	
	protected String typeId(){
		return "aspects_of";
	}
	
	public EmiRecipeCategory getCategory(){
		return ArcanaEmiPlugin.ASPECTS_BY_ITEMS;
	}
}