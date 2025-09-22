package arcana.integration.emi;

import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.item.Item;
import net.minecraft.util.registry.Registry;

import java.util.List;

public class EmiUntaintingRecipe extends GenericEmiConversionRecipe{
	
	private static final EmiTexture ARROW = new EmiTexture(ArcanaEmiPlugin.WIDGETS, 0, 16, 25, 16);
	
	public EmiUntaintingRecipe(Item item, List<Item> outputs){
		super(EmiStack.of(item),
				outputs.stream().map(x -> EmiStack.of(x).setChance(1f / outputs.size())).toList(),
				Registry.ITEM.getId(item)
		);
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