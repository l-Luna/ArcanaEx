package arcana.client.research.sections;

import arcana.aspects.AspectMap;
import arcana.aspects.AspectStack;
import arcana.client.AspectRenderer;
import arcana.recipes.AlchemyRecipe;
import arcana.research.sections.AlchemyRecipeSection;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.util.Pair;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import static arcana.client.research.EntrySectionRenderer.overlayTexture;
import static arcana.screens.ResearchEntryScreen.*;

public class AlchemyRecipeSectionRenderer extends AbstractRecipeSectionRenderer<AlchemyRecipeSection>{
	
	protected void renderRecipe(MatrixStack matrices, Recipe<?> recipe, AlchemyRecipeSection section, int pageIdx, int screenWidth, int screenHeight, int mouseX, int mouseY, boolean right){
		if(recipe instanceof AlchemyRecipe ar){
			int x = right ? pageX + rightXOffset : pageX;
			
			int ulX = x + (screenWidth - 256 + pageWidth) / 2 - 35;
			int ulY = pageY + (screenHeight - bgHeight + pageHeight) / 2 - 10 - heightOffset;
			RenderSystem.setShaderTexture(0, overlayTexture(section));
			drawTexture(matrices, ulX, ulY, 101, 73, 1, 70, 70, 256, 256);
			drawTexture(matrices, ulX + 19, ulY - 4, 101, 23, 145, 17, 17, 256, 256);
			
			int inputX = ulX + 1, inputY = ulY - 5;
			ItemStack[] stacks = ar.getIngredients().get(0).getMatchingStacks();
			client().getItemRenderer().renderInGui(stacks[displayIdx(stacks.length)], inputX, inputY);
			
			// Display aspects
			int aspectStartX = ulX + 12;
			int aspectStartY = ulY + 20;
			positionAspects(ar.getAspects(), aspectStartX, aspectStartY).forEach((stack, pos) ->
					AspectRenderer.renderAspectStack(stack, matrices, textRenderer(), pos.getLeft(), pos.getRight(), 101));
		}
	}
	
	protected void renderRecipeTooltips(MatrixStack matrices, Recipe<?> recipe, AlchemyRecipeSection section, int pageIdx, int screenWidth, int screenHeight, int mouseX, int mouseY, boolean right){
		if(recipe instanceof AlchemyRecipe ar){
			int x = right ? pageX + rightXOffset : pageX;
			
			int ulX = x + (screenWidth - 256 + pageWidth) / 2 - 35;
			int ulY = pageY + (screenHeight - bgHeight + pageHeight) / 2 - 10 - heightOffset;
			
			int inputX = ulX + 1, inputY = ulY - 5;
			ItemStack[] stacks = ar.getIngredients().get(0).getMatchingStacks();
			tooltipArea(matrices, stacks[displayIdx(stacks.length)], mouseX, mouseY, inputX, inputY);
			
			// Display aspects
			int aspectStartX = ulX + 9;
			int aspectStartY = ulY + 30;
			positionAspects(ar.getAspects(), aspectStartX, aspectStartY).forEach((stack, pos) ->
					tooltipArea(matrices, stack.type(), mouseX, mouseY, pos.getLeft(), pos.getRight()));
		}
	}
	
	public static Map<AspectStack, Pair<Integer, Integer>> positionAspects(AspectMap map, int tlX, int tlY){
		// vertically centre rows, horizontally centre within rows, sort by size
		var stacks = map.asStacks();
		Map<AspectStack, Pair<Integer, Integer>> ret = new HashMap<>();
		stacks.sort(Comparator.comparingInt(AspectStack::amount).reversed());
		int rows = (int)Math.ceil(stacks.size() / 3f);
		for(int row = 0; row < rows; row++){
			int aspectsOnRow = Math.min(3, stacks.size() - row * 3);
			int x = tlX + (48 - aspectsOnRow * 19) / 2;
			int y = tlY + (48 - rows * 19) / 2 + row * 19;
			for(int i = 0; i < aspectsOnRow; i++){
				var idx = row * 3 + i;
				if(idx < stacks.size())
					ret.put(stacks.get(idx), new Pair<>(x + i * 19, y));
			}
		}
		return ret;
	}
}