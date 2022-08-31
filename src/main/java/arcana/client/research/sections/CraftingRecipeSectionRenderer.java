package arcana.client.research.sections;

import arcana.research.sections.CraftingRecipeSection;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.ShapedRecipe;

import static arcana.screens.ResearchEntryScreen.*;

public class CraftingRecipeSectionRenderer extends AbstractRecipeSectionRenderer<CraftingRecipeSection>{
	
	protected void renderRecipe(MatrixStack matrices, Recipe<?> recipe, CraftingRecipeSection section, int pageIdx, int screenWidth, int screenHeight, int mouseX, int mouseY, boolean right){
		if(recipe instanceof CraftingRecipe cr){
			int x = right ? pageX + rightXOffset : pageX;
			int ulX = x + (screenWidth - 256 + pageWidth) / 2 - 32, ulY = pageY + (screenHeight - bgHeight + pageHeight) / 2 - 10 - heightOffset;
			
			RenderSystem.setShaderTexture(0, overlayTexture(section));
			drawTexture(matrices, ulX - 4, ulY - 4, 101, 145, 1, 72, 72, 256, 256);
			
			int width = recipe instanceof ShapedRecipe ? ((ShapedRecipe)cr).getWidth() : 3;
			int height = recipe instanceof ShapedRecipe ? ((ShapedRecipe)cr).getHeight() : 3;
			
			for(int xx = 0; xx < width; xx++)
				for(int yy = 0; yy < height; yy++){
					int index = xx + yy * width;
					if(index < recipe.getIngredients().size()){
						int itemX = ulX + xx * 24;
						int itemY = ulY + yy * 24;
						ItemStack[] stacks = recipe.getIngredients().get(index).getMatchingStacks();
						if(stacks.length > 0)
							client().getItemRenderer().renderInGui(stacks[displayIdx(stacks.length)], itemX, itemY);
					}
				}
		}
	}
	
	protected void renderRecipeTooltips(MatrixStack matrices, Recipe<?> recipe, CraftingRecipeSection section, int pageIdx, int screenWidth, int screenHeight, int mouseX, int mouseY, boolean right){
		if(recipe instanceof CraftingRecipe cr){
			int x = right ? pageX + rightXOffset : pageX;
			int ulX = x + (screenWidth - 256 + pageWidth) / 2 - 32, ulY = pageY + (screenHeight - bgHeight + pageHeight) / 2 - 10 - heightOffset;
			
			int width = recipe instanceof ShapedRecipe ? ((ShapedRecipe)cr).getWidth() : 3;
			int height = recipe instanceof ShapedRecipe ? ((ShapedRecipe)cr).getHeight() : 3;
			
			for(int xx = 0; xx < width; xx++)
				for(int yy = 0; yy < height; yy++){
					int index = xx + yy * width;
					if(index < recipe.getIngredients().size()){
						int itemX = ulX + xx * 24;
						int itemY = ulY + yy * 24;
						ItemStack[] stacks = recipe.getIngredients().get(index).getMatchingStacks();
						if(stacks.length > 0)
							tooltipArea(matrices, stacks[displayIdx(stacks.length)], mouseX, mouseY, itemX, itemY);
					}
				}
		}
	}
}