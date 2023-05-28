package arcana.integration.emi;

import arcana.aspects.AspectMap;
import arcana.recipes.AlchemyRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

import static arcana.Arcana.arcId;
import static arcana.client.research.sections.AlchemyRecipeSectionRenderer.positionAspects;

public class EmiAlchemyRecipe implements EmiRecipe{
	
	private static final Identifier texture = arcId("textures/gui/emi/alchemy.png");
	private static final EmiTexture background = new EmiTexture(texture, 0, 0, 94, 102);
	
	protected final Identifier id;
	protected final EmiIngredient input;
	protected final EmiStack output;
	protected final AspectMap aspects;
	
	public EmiAlchemyRecipe(AlchemyRecipe recipe){
		this(recipe.getId(), EmiIngredient.of(recipe.getIngredients().get(0)), EmiStack.of(recipe.getOutput()), recipe.getAspects());
	}
	
	public EmiAlchemyRecipe(Identifier id, EmiIngredient input, EmiStack output, AspectMap aspects){
		this.id = id;
		this.input = input;
		this.output = output;
		this.aspects = aspects;
	}
	
	public EmiRecipeCategory getCategory(){
		return ArcanaEmiPlugin.ALCHEMY;
	}
	
	public @Nullable Identifier getId(){
		return id;
	}
	
	public List<EmiIngredient> getInputs(){
		return Stream.concat(Stream.of(input), aspects.asStacks().stream().map(AspectEmiStack::new)).toList();
	}
	
	public List<EmiStack> getOutputs(){
		return List.of(output);
	}
	
	public int getDisplayWidth(){
		return 94;
	}
	
	public int getDisplayHeight(){
		return 102;
	}
	
	public void addWidgets(WidgetHolder widgets){
		widgets.addTexture(background, 0, 0);
		widgets.addSlot(input, 5, 24).drawBack(false);
		widgets.addSlot(output, 49, 5).drawBack(false).recipeContext(this);
		positionAspects(aspects, 36, 50).forEach((stack, pos) ->
				widgets.addSlot(new AspectEmiStack(stack), pos.getLeft(), pos.getRight()).drawBack(false));
	}
}