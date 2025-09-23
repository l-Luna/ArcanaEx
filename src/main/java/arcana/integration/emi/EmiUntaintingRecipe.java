package arcana.integration.emi;

import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

import java.util.List;

public class EmiUntaintingRecipe extends GenericEmiConversionRecipe{
	
	private static final EmiTexture ARROW = new EmiTexture(ArcanaEmiPlugin.WIDGETS, 0, 16, 25, 16);
	
	public EmiUntaintingRecipe(EmiIngredient item, Item output, Identifier baseId){
		super(item, List.of(EmiStack.of(output)), baseId);
	}
	
	protected String typeId(){
		return "untainting";
	}
	
	protected EmiTexture arrowTexture(){
		return ARROW;
	}
	
	public EmiRecipeCategory getCategory(){
		return ArcanaEmiPlugin.UNTAINTING;
	}
}