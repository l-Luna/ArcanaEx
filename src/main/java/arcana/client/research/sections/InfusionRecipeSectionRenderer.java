package arcana.client.research.sections;

import arcana.client.AspectRenderer;
import arcana.recipes.InfusionRecipe;
import arcana.research.sections.InfusionRecipeSection;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.text.Text;

import static arcana.client.research.EntrySectionRenderer.overlayTexture;
import static arcana.screens.ResearchEntryScreen.*;

public class InfusionRecipeSectionRenderer extends AbstractRecipeSectionRenderer<InfusionRecipeSection>{
	
	protected void renderRecipe(MatrixStack matrices,
	                            Recipe<?> recipe,
	                            InfusionRecipeSection section,
	                            int pageIdx,
	                            int screenWidth,
	                            int screenHeight,
	                            int mouseX,
	                            int mouseY,
	                            boolean right){
		if(recipe instanceof InfusionRecipe ir){
			int x = right ? pageX + rightXOffset : pageX;
			int ulX = x + (screenWidth - 256 + pageWidth) / 2 - 25;
			int ulY = pageY + (screenHeight - bgHeight + pageHeight) / 2 - 10 - heightOffset - 14;
			
			RenderSystem.setShaderTexture(0, overlayTexture(section));
			drawTexture(matrices, ulX - 10, ulY - 10, 101, 1, 73, 70, 70, 256, 256);
			
			var midX = ulX + 35 - 18;
			var midY = ulY + 35 - 18;
			
			ItemStack[] centralStacks = ir.centralIngredient().getMatchingStacks();
			if(centralStacks.length > 0)
				client().getItemRenderer().renderInGui(centralStacks[displayIdx(centralStacks.length)], midX, midY);
			
			var outers = ir.outerIngredients();
			for(int i = 0; i < outers.size(); i++){
				ItemStack[] stacks = outers.get(i).getMatchingStacks();
				if(stacks.length > 0){
					int offX = (int)(32 * Math.sin(2 * Math.PI * (i / (float)outers.size())));
					int offY = (int)(32 * Math.cos(2 * Math.PI * (i / (float)outers.size())));
					client().getItemRenderer().renderInGui(stacks[displayIdx(stacks.length)], midX + offX, midY + offY);
				}
			}
			
			var text = Text.translatable("recipe.infusion.instability", Text.translatable("recipe.infusion.instability." + ir.instability()));
			textRenderer().draw(matrices, text, midX + 8 - textRenderer().getWidth(text.getString()) / 2f, midY + 54, 0);
			
			var stacks = ir.aspects().asStacks();
			int spacing = (stacks.size() == 1) ? 0 : (stacks.size() >= 6) ? 1 : (stacks.size() < 4) ? 3 : 2;
			int aspectX = ulX + 73 / 2 - (stacks.size() * (16 + spacing * 2)) / 2 - 11;
			int aspectY = ulY + 86;
			
			for(int i = 0, length = stacks.size(); i < length; i++)
				AspectRenderer.renderAspectStack(stacks.get(i), matrices, client().textRenderer, aspectX + i * (16 + 2 * spacing) + spacing, aspectY, 101);
		}
	}
	
	protected void renderRecipeTooltips(MatrixStack matrices,
	                                    Recipe<?> recipe,
	                                    InfusionRecipeSection section,
	                                    int pageIdx,
	                                    int screenWidth,
	                                    int screenHeight,
	                                    int mouseX,
	                                    int mouseY,
	                                    boolean right){
		if(recipe instanceof InfusionRecipe ir){
			int x = right ? pageX + rightXOffset : pageX;
			int ulX = x + (screenWidth - 256 + pageWidth) / 2 - 25;
			int ulY = pageY + (screenHeight - bgHeight + pageHeight) / 2 - 10 - heightOffset - 14;
			
			var midX = ulX + 35 - 18;
			var midY = ulY + 35 - 18;
			
			ItemStack[] centralStacks = ir.centralIngredient().getMatchingStacks();
			if(centralStacks.length > 0)
				tooltipArea(matrices, centralStacks[displayIdx(centralStacks.length)], mouseX, mouseY, midX, midY);
			
			var outers = ir.outerIngredients();
			for(int i = 0; i < outers.size(); i++){
				ItemStack[] stacks = outers.get(i).getMatchingStacks();
				if(stacks.length > 0){
					int offX = (int)(32 * Math.sin(2 * Math.PI * (i / (float)outers.size())));
					int offY = (int)(32 * Math.cos(2 * Math.PI * (i / (float)outers.size())));
					tooltipArea(matrices, stacks[displayIdx(stacks.length)], mouseX, mouseY, midX + offX, midY + offY);
				}
			}
			
			var stacks = ir.aspects().asStacks();
			int spacing = (stacks.size() == 1) ? 0 : (stacks.size() >= 6) ? 1 : (stacks.size() < 4) ? 3 : 2;
			int aspectX = ulX + 73 / 2 - (stacks.size() * (16 + spacing * 2)) / 2 - 11;
			int aspectY = ulY + 86;
			
			for(int i = 0, length = stacks.size(); i < length; i++)
				tooltipArea(matrices, stacks.get(i).type(), mouseX, mouseY, aspectX + i * (16 + 2 * spacing) + spacing, aspectY);
		}
	}
}
