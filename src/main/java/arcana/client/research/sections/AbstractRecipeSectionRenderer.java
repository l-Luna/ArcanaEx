package arcana.client.research.sections;

import arcana.client.research.EntrySectionRenderer;
import arcana.research.EntrySection;
import arcana.research.sections.AbstractRecipeSection;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;

import static arcana.screens.ResearchEntryScreen.*;
import static net.minecraft.client.gui.DrawableHelper.drawTexture;

public abstract class AbstractRecipeSectionRenderer<T extends AbstractRecipeSection> implements EntrySectionRenderer<T>{
	
	// Provide recipe context
	
	public void render(MatrixStack matrices, T section, int pageIdx, int screenWidth, int screenHeight, int mouseX, int mouseY, boolean right){
		client().world.getRecipeManager().get(section.getRecipeId()).ifPresent(recipe -> {
			ItemStack result = recipe.getOutput();
			renderResult(matrices, result, right ? pageX + rightXOffset : pageX, pageY, screenWidth, screenHeight, section);
			renderRecipe(matrices, recipe, section, pageIdx, screenWidth, screenHeight, mouseX, mouseY, right);
		});
	}
	
	public void renderAfter(MatrixStack matrices, T section, int pageIdx, int screenWidth, int screenHeight, int mouseX, int mouseY, boolean right){
		client().world.getRecipeManager().get(section.getRecipeId()).ifPresent(recipe -> {
			ItemStack result = recipe.getOutput();
			renderResultTooltip(matrices, result, right ? pageX + rightXOffset : pageX, pageY, mouseX, mouseY, screenWidth, screenHeight);
			renderRecipeTooltips(matrices, recipe, section, pageIdx, screenWidth, screenHeight, mouseX, mouseY, right);
		});
	}
	
	protected abstract void renderRecipe(MatrixStack matrices, Recipe<?> recipe, T section, int pageIdx, int screenWidth, int screenHeight, int mouseX, int mouseY, boolean right);
	
	protected abstract void renderRecipeTooltips(MatrixStack matrices, Recipe<?> recipe, T section, int pageIdx, int screenWidth, int screenHeight, int mouseX, int mouseY, boolean right);
	
	protected void renderResult(MatrixStack matrices, ItemStack stack, int x, int y, int screenWidth, int screenHeight, EntrySection section){
		RenderSystem.setShaderTexture(0, overlayTexture(section));
		int rX = x + (screenWidth - 256) / 2 + (pageWidth - 58) / 2;
		int rY = y + (screenHeight - bgHeight) / 2 + 16 - heightOffset;
		drawTexture(matrices, rX, rY, 101, 1, 167, 58, 20, 256, 256);
		client().getItemRenderer().renderInGui(stack, rX + 29 - 8, rY + 10 - 8);
		var name = stack.getName().getString();
		int stX = x + (screenWidth - 256) / 2 + (pageWidth - textRenderer().getWidth(name)) / 2;
		int stY = y + (screenHeight - bgHeight) / 2 + 11 - textRenderer().fontHeight - heightOffset;
		textRenderer().draw(matrices, name, stX, stY, 0x000000);
	}
	
	protected void renderResultTooltip(MatrixStack matrices, ItemStack stack, int x, int y, int mouseX, int mouseY, int screenWidth, int screenHeight){
		int rX = x + (screenWidth - 256) / 2 + (pageWidth - 58) / 2 + 21;
		int rY = y + (screenHeight - 181) / 2 + 18 - heightOffset;
		tooltipArea(matrices, stack, mouseX, mouseY, rX, rY);
	}
	
	protected int displayIdx(int max){
		return (int)((client().world.getTime() / 30) % max);
	}
	
	public int span(T section, PlayerEntity player){
		return 1;
	}
}