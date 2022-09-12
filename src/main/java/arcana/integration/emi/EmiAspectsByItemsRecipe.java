package arcana.integration.emi;

import arcana.aspects.AspectStack;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class EmiAspectsByItemsRecipe implements EmiRecipe{
	
	private final EmiIngredient item;
	private final List<AspectEmiStack> aspects;
	
	public EmiAspectsByItemsRecipe(EmiIngredient item, List<AspectStack> aspects){
		this.item = item;
		this.aspects = aspects.stream().map(AspectEmiStack::new).toList();
	}
	
	public EmiRecipeCategory getCategory(){
		return ArcanaEmiPlugin.ASPECTS_BY_ITEMS;
	}
	
	public @Nullable Identifier getId(){
		return null;
	}
	
	public List<EmiIngredient> getInputs(){
		return List.of(item);
	}
	
	public List<EmiStack> getOutputs(){
		// fix generic type
		return Collections.unmodifiableList(aspects);
	}
	
	public int getDisplayWidth(){
		return 27 + 32 + 2 + Math.min(aspects.size(), 3) * 20;
	}
	
	public int getDisplayHeight(){
		return 20 + Math.max(0, (int)(Math.ceil(aspects.size() / 3f) - 1)) * 20;
	}
	
	public void addWidgets(WidgetHolder widgets){
		widgets.addSlot(item, 0, 1);
		widgets.addTexture(EmiTexture.EMPTY_ARROW, 27, 2);
		for(int i = 0; i < aspects.size(); i++)
			widgets.addSlot(aspects.get(i), 27 + 32 + (i % 3) * 20, 1 + (i / 3) * 20).recipeContext(this);
	}
}