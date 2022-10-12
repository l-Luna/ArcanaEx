package arcana.integration.emi;

import arcana.aspects.AspectMap;
import arcana.aspects.AspectStack;
import arcana.recipes.InfusionRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

import static arcana.Arcana.arcId;

public class EmiInfusionRecipe implements EmiRecipe{
	
	private static final Identifier texture = arcId("textures/gui/emi/infusion.png");
	private static final EmiTexture background = new EmiTexture(texture, 0, 0, 58, 91);
	
	protected final Identifier id;
	protected final List<EmiIngredient> outers;
	protected final EmiIngredient central;
	protected final EmiStack output;
	protected final AspectMap aspects;
	protected final int instability;
	
	public EmiInfusionRecipe(InfusionRecipe recipe){
		id = recipe.getId();
		outers = recipe.outerIngredients().stream().map(EmiXIngredient::of).toList();
		central = EmiIngredient.of(recipe.centralIngredient());
		output = EmiStack.of(recipe.getOutput());
		aspects = recipe.aspects();
		instability = recipe.instability();
	}
	
	public EmiRecipeCategory getCategory(){
		return ArcanaEmiPlugin.INFUSION;
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
	
	public int getDisplayWidth(){
		return 110;
	}
	
	public int getDisplayHeight(){
		return 140;
	}
	
	public void addWidgets(WidgetHolder widgets){
		int left = 26;
		widgets.addTexture(background, left, 0);
		widgets.addSlot(output, left + 20, 1).recipeContext(this).drawBack(false);
		int midX = left + 21 - 1;
		int midY = 58 - 1;
		widgets.addSlot(central, midX, midY).drawBack(false);
		for(int i = 0; i < outers.size(); i++){
			int offX = (int)(32 * Math.sin(2 * Math.PI * (i / (float)outers.size())));
			int offY = (int)(32 * Math.cos(2 * Math.PI * (i / (float)outers.size())));
			widgets.addSlot(outers.get(i), midX + offX, midY + offY).drawBack(false);
		}
		var text = Text.translatable("recipe.infusion.instability.title", Text.translatable("recipe.infusion.instability." + instability));
		var textRenderer = MinecraftClient.getInstance().textRenderer;
		widgets.addText(text.asOrderedText(), (getDisplayWidth() - textRenderer.getWidth(text.getString())) / 2, 110, 0, false);
		
		List<AspectStack> stacks = aspects.asStacks();
		int spacing = (aspects.size() == 1) ? 0 : (aspects.size() >= 6) ? 1 : (aspects.size() < 4) ? 3 : 2;
		int aspectX = (getDisplayWidth() / 2) - (aspects.size() * (16 + spacing * 2)) / 2;
		int aspectY = 120;
		for(int i = 0; i < stacks.size(); i++)
			widgets.addSlot(new AspectEmiStack(stacks.get(i)), aspectX + i * (16 + 2 * spacing) + spacing, aspectY).drawBack(false);
	}
}