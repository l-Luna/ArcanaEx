package arcana.client.research.sections;

import arcana.aspects.AspectStack;
import arcana.client.AspectRenderHelper;
import arcana.recipes.XIngredient;
import arcana.recipes.infusion.SimpleInfusionRecipe;
import arcana.research.sections.InfusionRecipeSection;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.List;

import static arcana.client.research.EntrySectionRenderer.overlayTexture;
import static arcana.screens.ResearchEntryScreen.*;

public class InfusionRecipeSectionRenderer extends AbstractRecipeSectionRenderer<InfusionRecipeSection>{
	
	protected void renderRecipe(DrawContext ctx,
	                            Recipe<?> recipe,
	                            InfusionRecipeSection section,
	                            int pageIdx,
	                            int screenWidth,
	                            int screenHeight,
	                            int mouseX,
	                            int mouseY,
	                            boolean right){
		if(recipe instanceof SimpleInfusionRecipe ir){
			int x = right ? pageX + rightXOffset : pageX;
			int ulX = x + (screenWidth - 256 + pageWidth) / 2 - 25;
			int ulY = pageY + (screenHeight - bgHeight + pageHeight) / 2 - 10 - heightOffset - 14;
			
			ctx.drawTexture(overlayTexture(section), ulX - 10, ulY - 10, 101, 1, 73, 70, 70, 256, 256);
			
			int midX = ulX + 35 - 18;
			int midY = ulY + 35 - 18;
			
			ItemStack[] centralStacks = ir.centralIngredient().getMatchingStacks();
			if(centralStacks.length > 0)
				ctx.drawItem(centralStacks[displayIdx(centralStacks.length)], midX, midY);
			
			List<XIngredient> outers = ir.outerIngredients();
			for(int i = 0; i < outers.size(); i++){
				ItemStack[] stacks = outers.get(i).getMatchingStacks();
				if(stacks.length > 0){
					int offX = (int)(32 * Math.sin(2 * Math.PI * (i / (float)outers.size())));
					int offY = (int)(32 * Math.cos(2 * Math.PI * (i / (float)outers.size())));
					ctx.drawItem(stacks[displayIdx(stacks.length)], midX + offX, midY + offY);
				}
			}
			
			MutableText text = Text.translatable("recipe.infusion.instability.title", Text.translatable("recipe.infusion.instability." + ir.instability()));
			ctx.drawText(textRenderer(), text, (int)(midX + 8 - textRenderer().getWidth(text.getString()) / 2f), midY + 54, 0, false);
			
			List<AspectStack> stacks = ir.aspects().asStacks();
			int spacing = (stacks.size() == 1) ? 0 : (stacks.size() >= 6) ? 1 : (stacks.size() < 4) ? 3 : 2;
			int aspectX = ulX + 73 / 2 - (stacks.size() * (16 + spacing * 2)) / 2 - 11;
			int aspectY = ulY + 86;
			
			for(int i = 0, length = stacks.size(); i < length; i++)
				AspectRenderHelper.renderAspectStack(stacks.get(i), ctx, client().textRenderer, aspectX + i * (16 + 2 * spacing) + spacing, aspectY, 101);
		}
	}
	
	protected void renderRecipeTooltips(DrawContext ctx,
	                                    Recipe<?> recipe,
	                                    InfusionRecipeSection section,
	                                    int pageIdx,
	                                    int screenWidth,
	                                    int screenHeight,
	                                    int mouseX,
	                                    int mouseY,
	                                    boolean right){
		if(recipe instanceof SimpleInfusionRecipe ir){
			int x = right ? pageX + rightXOffset : pageX;
			int ulX = x + (screenWidth - 256 + pageWidth) / 2 - 25;
			int ulY = pageY + (screenHeight - bgHeight + pageHeight) / 2 - 10 - heightOffset - 14;
			
			int midX = ulX + 35 - 18;
			int midY = ulY + 35 - 18;
			
			ItemStack[] centralStacks = ir.centralIngredient().getMatchingStacks();
			if(centralStacks.length > 0)
				tooltipArea(ctx, centralStacks[displayIdx(centralStacks.length)], mouseX, mouseY, midX, midY);
			
			List<XIngredient> outers = ir.outerIngredients();
			for(int i = 0; i < outers.size(); i++){
				ItemStack[] stacks = outers.get(i).getMatchingStacks();
				if(stacks.length > 0){
					int offX = (int)(32 * Math.sin(2 * Math.PI * (i / (float)outers.size())));
					int offY = (int)(32 * Math.cos(2 * Math.PI * (i / (float)outers.size())));
					tooltipArea(ctx, stacks[displayIdx(stacks.length)], mouseX, mouseY, midX + offX, midY + offY);
				}
			}
			
			List<AspectStack> stacks = ir.aspects().asStacks();
			int spacing = (stacks.size() == 1) ? 0 : (stacks.size() >= 6) ? 1 : (stacks.size() < 4) ? 3 : 2;
			int aspectX = ulX + 73 / 2 - (stacks.size() * (16 + spacing * 2)) / 2 - 11;
			int aspectY = ulY + 86;
			
			for(int i = 0, length = stacks.size(); i < length; i++)
				tooltipArea(ctx, stacks.get(i).type(), mouseX, mouseY, aspectX + i * (16 + 2 * spacing) + spacing, aspectY);
		}
	}
}
