package arcana.client.research.sections;

import arcana.research.sections.CraftingRecipeSection;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.ShapedRecipe;

import static arcana.client.research.EntrySectionRenderer.overlayTexture;
import static arcana.screens.ResearchEntryScreen.*;

public class CraftingRecipeSectionRenderer extends AbstractRecipeSectionRenderer<CraftingRecipeSection>{
	
	protected void renderRecipe(DrawContext ctx, Recipe<?> recipe, CraftingRecipeSection section, int pageIdx, int screenWidth, int screenHeight, int mouseX, int mouseY, boolean right){
		if(recipe instanceof CraftingRecipe cr){
			int x = right ? pageX + rightXOffset : pageX;
			int ulX = x + (screenWidth - 256 + pageWidth) / 2 - 32, ulY = pageY + (screenHeight - bgHeight + pageHeight) / 2 - 10 - heightOffset;
			
			ctx.drawTexture(overlayTexture(section), ulX - 4, ulY - 4, 101, 145, 1, 72, 72, 256, 256);
			
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
							ctx.drawItem(displayStack(stacks), itemX, itemY);
					}
				}
		}
	}
	
	protected void renderRecipeTooltips(DrawContext ctx, Recipe<?> recipe, CraftingRecipeSection section, int pageIdx, int screenWidth, int screenHeight, int mouseX, int mouseY, boolean right){
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
							tooltipArea(ctx, displayStack(stacks), mouseX, mouseY, itemX, itemY);
					}
				}
		}
	}
}