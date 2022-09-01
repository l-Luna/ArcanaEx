package arcana.client.research.sections;

import arcana.research.sections.CookingRecipeSection;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.Recipe;

import static arcana.client.research.EntrySectionRenderer.overlayTexture;
import static arcana.screens.ResearchEntryScreen.*;

public class CookingRecipeSectionRenderer extends AbstractRecipeSectionRenderer<CookingRecipeSection>{
	
	protected void renderRecipe(MatrixStack matrices, Recipe<?> recipe, CookingRecipeSection section, int pageIdx, int screenWidth, int screenHeight, int mouseX, int mouseY, boolean right){
		if(recipe instanceof AbstractCookingRecipe cr){
			int x = right ? pageX + rightXOffset : pageX;
			int inputX = x + (screenWidth - 256 + pageWidth) / 2 - 8;
			int inputY = pageY + (screenHeight - bgHeight + pageHeight) / 2 + 8 - heightOffset;
			RenderSystem.setShaderTexture(0, overlayTexture(section));
			drawTexture(matrices, inputX - 9, inputY - 9, 101, 219, 1, 34, 48, 256, 256);
			ItemStack[] stacks = cr.getIngredients().get(0).getMatchingStacks();
			client().getItemRenderer().renderInGui(stacks[displayIdx(stacks.length)], inputX, inputY);
		}
	}
	
	protected void renderRecipeTooltips(MatrixStack matrices, Recipe<?> recipe, CookingRecipeSection section, int pageIdx, int screenWidth, int screenHeight, int mouseX, int mouseY, boolean right){
		if(recipe instanceof AbstractCookingRecipe cr){
			int x = right ? pageX + rightXOffset : pageX;
			int inputX = x + (screenWidth - 256 + pageWidth) / 2 - 8;
			int inputY = pageY + (screenHeight - bgHeight + pageHeight) / 2 + 8 - heightOffset;
			ItemStack[] stacks = cr.getIngredients().get(0).getMatchingStacks();
			tooltipArea(matrices, stacks[displayIdx(stacks.length)], mouseX, mouseY, inputX, inputY);
		}
	}
}