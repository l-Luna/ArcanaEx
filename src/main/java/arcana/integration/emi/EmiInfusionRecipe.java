package arcana.integration.emi;

import arcana.aspects.AspectMap;
import arcana.recipes.infusion.SimpleInfusionRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

public class EmiInfusionRecipe extends AbstractEmiInfusionRecipe{
	
	protected final Identifier id;
	protected final List<EmiIngredient> outers;
	protected final EmiIngredient central;
	protected final EmiStack output;
	protected final AspectMap aspects;
	protected final int instability;
	
	public EmiInfusionRecipe(Identifier id, SimpleInfusionRecipe recipe){
		this.id = id;
		outers = recipe.outerIngredients().stream().map(EmiIngredient::of).toList();
		central = EmiIngredient.of(recipe.centralIngredient());
		output = EmiStack.of(recipe.getResult());
		aspects = recipe.aspects();
		instability = recipe.instability();
	}
	
	public @Nullable Identifier getId(){
		return id;
	}
	
	public List<EmiIngredient> getInputs(){
		return Stream.of(
				outers.stream(),
				Stream.of(central),
				aspects.asStacks().stream().map(AspectEmiStack::new)
		).flatMap(x -> x).map(EmiIngredient.class::cast).toList();
	}
	
	public List<EmiStack> getOutputs(){
		return List.of(output);
	}
	
	public void addWidgets(WidgetHolder widgets){
		addBaseWidgets(widgets, central, outers, instability, aspects, output);
	}
}