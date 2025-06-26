package arcana.integration.emi;

import arcana.aspects.Aspect;
import arcana.aspects.Aspects;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static arcana.Arcana.arcId;

public class EmiAspectCrystallizationRecipe implements EmiRecipe{
	
	private static final Identifier texture = arcId("textures/gui/emi/aspect_crystallization.png");
	private static final EmiTexture background = new EmiTexture(texture, 0, 0, 88, 26);
	
	private final EmiStack crystal;
	private final AspectEmiStack essentia;
	
	public EmiAspectCrystallizationRecipe(Aspect aspect){
		crystal = EmiStack.of(Aspects.crystals.get(aspect));
		essentia = new AspectEmiStack(aspect, 2);
	}
	
	public EmiRecipeCategory getCategory(){
		return ArcanaEmiPlugin.ASPECT_CRYSTALLIZATION;
	}
	
	public @Nullable Identifier getId(){
		return null;
	}
	
	public List<EmiIngredient> getInputs(){
		return List.of(essentia);
	}
	
	public List<EmiStack> getOutputs(){
		return List.of(crystal);
	}
	
	public int getDisplayWidth(){
		return 88;
	}
	
	public int getDisplayHeight(){
		return 26;
	}
	
	public void addWidgets(WidgetHolder widgets){
		widgets.addTexture(background, 0, 0);
		widgets.addSlot(essentia, 2, 4).drawBack(false);
		widgets.addSlot(crystal, 69, 4).drawBack(false).recipeContext(this);
	}
}