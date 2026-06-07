package arcana.integration.emi;

import arcana.aspects.AspectMap;
import arcana.aspects.AspectStack;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

import static arcana.Arcana.arcId;

public abstract class AbstractEmiInfusionRecipe implements EmiRecipe{
	
	protected static final Identifier texture = arcId("textures/gui/emi/infusion.png");
	protected static final EmiTexture background = new EmiTexture(texture, 0, 0, 58, 91);
	
	public EmiRecipeCategory getCategory(){
		return ArcanaEmiPlugin.INFUSION;
	}
	
	public int getDisplayWidth(){
		return 110;
	}
	
	public int getDisplayHeight(){
		return 140;
	}
	
	public void addBaseWidgets(WidgetHolder widgets, EmiIngredient cental, List<EmiIngredient> outerStacks, int instability, AspectMap aspects, EmiStack result){
		int left = 26;
		widgets.addTexture(background, left, 0);
		widgets.addSlot(result, left + 20, 1).recipeContext(this).drawBack(false);
		int midX = left + 21 - 1;
		int midY = 58 - 1;
		widgets.addSlot(cental, midX, midY).drawBack(false);
		int outerCount = outerStacks.size();
		for(int i = 0; i < outerCount; i++){
			int offX = (int)(32 * Math.sin(2 * Math.PI * (i / (float)outerCount)));
			int offY = (int)(32 * Math.cos(2 * Math.PI * (i / (float)outerCount)));
			widgets.addSlot(outerStacks.get(i), midX + offX, midY + offY).drawBack(false);
		}
		var text = Text.translatable("recipe.infusion.instability.title", Text.translatable("recipe.infusion.instability." + instability));
		var textRenderer = MinecraftClient.getInstance().textRenderer;
		widgets.addText(text.asOrderedText(), (getDisplayWidth() - textRenderer.getWidth(text.getString())) / 2, 110, 0, false);
		
		List<AspectStack> stacks = aspects.asStacks();
		int spacing = (stacks.size() == 1) ? 0 : (stacks.size() >= 6) ? 1 : (stacks.size() < 4) ? 3 : 2;
		int aspectX = (getDisplayWidth() / 2) - (stacks.size() * (16 + spacing * 2)) / 2;
		int aspectY = 120;
		for(int i = 0; i < stacks.size(); i++)
			widgets.addSlot(new AspectEmiStack(stacks.get(i)), aspectX + i * (16 + 2 * spacing) + spacing, aspectY).drawBack(false);
	}
}