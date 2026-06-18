package arcana.client.research.sections;

import arcana.client.AspectRenderHelper;
import arcana.recipes.arcane_crafting.ShapedArcaneCraftingRecipe;
import arcana.research.sections.ArcaneCraftingRecipeSection;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;

import static arcana.client.research.EntrySectionRenderer.overlayTexture;
import static arcana.screens.ResearchEntryScreen.*;

public class ArcaneCraftingRecipeSectionRenderer extends AbstractRecipeSectionRenderer<ArcaneCraftingRecipeSection>{
	
	// TODO: support unshaped arcane crafting, merge with CraftingRecipeSectionRenderer
	
	protected void renderRecipe(DrawContext ctx, Recipe<?> recipe, ArcaneCraftingRecipeSection section, int pageIdx, int screenWidth, int screenHeight, int mouseX, int mouseY, boolean right){
		if(recipe instanceof ShapedArcaneCraftingRecipe sacr){
			int x = right ? pageX + rightXOffset : pageX;
			int ulX = x + (screenWidth - 256 + pageWidth) / 2 - 32;
			int ulY = pageY + (screenHeight - bgHeight + pageHeight) / 2 - 10 - heightOffset - 15;
			
			ctx.drawTexture(overlayTexture(section), ulX - 10, ulY - 10, 101, 73, 75, 84, 84, 256, 256);
			
			int width = sacr.getWidth();
			int height = sacr.getHeight();
			
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
			
			var stacks = sacr.aspects().asStacks();
			int spacing = (stacks.size() == 1) ? 0 : (stacks.size() >= 6) ? 1 : (stacks.size() < 4) ? 3 : 2;
			int aspectX = ulX + 73 / 2 - (stacks.size() * (16 + spacing * 2)) / 2 - 4;
			int aspectY = ulY + 82;
			
			for(int i = 0, length = stacks.size(); i < length; i++)
				AspectRenderHelper.renderAspectStack(stacks.get(i), ctx, client().textRenderer, aspectX + i * (16 + 2 * spacing) + spacing, aspectY, 101);
		}
	}
	
	protected void renderRecipeTooltips(DrawContext ctx, Recipe<?> recipe, ArcaneCraftingRecipeSection section, int pageIdx, int screenWidth, int screenHeight, int mouseX, int mouseY, boolean right){
		if(recipe instanceof ShapedArcaneCraftingRecipe sacr){
			int x = right ? pageX + rightXOffset : pageX;
			int ulX = x + (screenWidth - 256 + pageWidth) / 2 - 32;
			int ulY = pageY + (screenHeight - bgHeight + pageHeight) / 2 - 10 - heightOffset - 15;
			
			int width = sacr.getWidth();
			int height = sacr.getHeight();
			
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
			
			var stacks = sacr.aspects().asStacks();
			int spacing = (stacks.size() == 1) ? 0 : (stacks.size() >= 6) ? 1 : (stacks.size() < 4) ? 3 : 2;
			int aspectX = ulX + 73 / 2 - (stacks.size() * (16 + spacing * 2)) / 2 - 4;
			int aspectY = ulY + 82;
			
			for(int i = 0, length = stacks.size(); i < length; i++)
				tooltipArea(ctx, stacks.get(i).type(), mouseX, mouseY, aspectX + i * (16 + 2 * spacing) + spacing, aspectY);
		}
	}
}