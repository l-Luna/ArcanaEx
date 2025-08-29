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
import java.util.OptionalInt;
import java.util.stream.Stream;

import static arcana.Arcana.arcId;
import static arcana.client.research.sections.AlchemyRecipeSectionRenderer.positionAspects;

public class EmiAlchemyRecipe implements EmiRecipe{
	
	private static final EmiTexture background = new EmiTexture(arcId("textures/gui/emi/alchemy.png"), 0, 0, 94, 102);
	private static final EmiTexture researchNote = new EmiTexture(arcId("textures/gui/research/research_note.png"), 0, 0, 16, 16, 16, 16, 16, 16);
	
	protected final Identifier id;
	protected final EmiIngredient input;
	protected final EmiStack output;
	protected final AspectMap aspects;
	protected final @Nullable Identifier researchId;
	protected final OptionalInt researchStage;
	
	public EmiAlchemyRecipe(AlchemyRecipe recipe){
		this(recipe.getId(), EmiIngredient.of(recipe.getIngredients().get(0)), EmiStack.of(recipe.getOutput()), recipe.getAspects(), recipe.getResearchId(), recipe.getResearchStage());
	}
	
	public EmiAlchemyRecipe(Identifier id, EmiIngredient input, EmiStack output, AspectMap aspects, @Nullable Identifier researchId, OptionalInt researchStage){
		this.id = id;
		this.input = input;
		this.output = output;
		this.aspects = aspects;
		this.researchId = researchId;
		this.researchStage = researchStage;
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
		return 120;
	}
	
	public int getDisplayHeight(){
		return 54;
	}
	
	public void addWidgets(WidgetHolder widgets){
		widgets.addTexture(background, 18, 0);
		widgets.addSlot(input, 2, 2);
		positionAspects(aspects, 24, 10).forEach((stack, pos) ->
				widgets.addSlot(new AspectEmiStack(stack), pos.getLeft(), pos.getRight()).drawBack(false));
		widgets.addSlot(output, 90, 14).large(true).recipeContext(this);
		/*if(researchId != null){
			widgets.addTexture(researchNote, 0, 98);
			Entry entry = Research.getEntry(researchId);
			if(entry != null)
				widgets.addText(Text.translatable(entry.name()), 18, 102, 0, false);
			else{
				widgets.addText(Text.literal("<invalid entry>"), 18, 102, 0, false);
			}
		}*/
	}
}