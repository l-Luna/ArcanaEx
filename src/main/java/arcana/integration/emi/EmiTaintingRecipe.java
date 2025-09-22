package arcana.integration.emi;

import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.item.Item;
import net.minecraft.util.registry.Registry;

import java.util.List;

public class EmiTaintingRecipe extends GenericEmiConversionRecipe{
	
	private static final EmiTexture ARROW = new EmiTexture(ArcanaEmiPlugin.WIDGETS, 0, 0, 25, 16);
	
	public EmiTaintingRecipe(Item input, Item output){
		super(EmiStack.of(input), List.of(EmiStack.of(output)), Registry.ITEM.getId(input));
	}
	
	protected String typeId(){
		return "tainting";
	}
	
	protected EmiTexture arrowTexture(){
		return ARROW;
	}
	
	public EmiRecipeCategory getCategory(){
		return ArcanaEmiPlugin.TAINTING;
	}
}