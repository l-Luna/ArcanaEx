package arcana.integration.emi;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;

// copy of EmiBrewingRecipe to avoid private API
// TODO: use modified texture to hide bottle icon in output, instead of covering up with slot backs
public class EmiAltBrewingRecipe implements EmiRecipe{
	
	private static final Identifier BACKGROUND = new Identifier("textures/gui/container/brewing_stand.png");
	private static final EmiStack BLAZE_POWDER = EmiStack.of(Items.BLAZE_POWDER);
	private final EmiIngredient input, ingredient;
	private final EmiStack output, input3, output3;
	private final Identifier id;
	
	public EmiAltBrewingRecipe(EmiStack input, EmiIngredient ingredient, EmiStack output, Identifier id){
		this.input = input;
		this.ingredient = ingredient;
		this.output = output;
		this.input3 = input.copy().setAmount(3);
		this.output3 = output.copy().setAmount(3);
		this.id = id;
	}
	
	public EmiRecipeCategory getCategory(){
		return VanillaEmiRecipeCategories.BREWING;
	}
	
	public @Nullable Identifier getId(){
		return id;
	}
	
	public List<EmiIngredient> getInputs(){
		return List.of(input3, ingredient);
	}
	
	public List<EmiStack> getOutputs(){
		return List.of(output3);
	}
	
	public int getDisplayWidth(){
		return 120;
	}
	
	public int getDisplayHeight(){
		return 61;
	}
	
	public void addWidgets(WidgetHolder widgets){
		widgets.addTexture(BACKGROUND, 0, 0, 103, 61, 16, 14);
		widgets.addAnimatedTexture(BACKGROUND, 81, 2, 9, 28, 176, 0, 1000 * 20, false, false, false)
				.tooltip((mx, my) -> List.of(TooltipComponent.of(EmiPort.translatable("emi.cooking.time", 20).asOrderedText())));
		widgets.addAnimatedTexture(BACKGROUND, 47, 0, 12, 29, 185, 0, 700, false, true, false);
		widgets.addTexture(BACKGROUND, 44, 30, 18, 4, 176, 29);
		widgets.addSlot(BLAZE_POWDER, 0, 2).drawBack(false);
		widgets.addSlot(input, 39, 36);
		widgets.addSlot(ingredient, 62, 2).drawBack(false);
		widgets.addSlot(output, 85, 36).recipeContext(this);
	}
}