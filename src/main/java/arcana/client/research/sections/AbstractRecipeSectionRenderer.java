package arcana.client.research.sections;

import arcana.api.RenamableRecipe;
import arcana.client.research.EntrySectionRenderer;
import arcana.research.EntrySection;
import arcana.research.sections.AbstractRecipeSection;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.Recipe;

import java.util.Optional;

import static arcana.client.research.EntrySectionRenderer.overlayTexture;
import static arcana.screens.ResearchEntryScreen.*;

public abstract class AbstractRecipeSectionRenderer<T extends AbstractRecipeSection> implements EntrySectionRenderer<T>{
	
	// Provide recipe context
	
	public void render(DrawContext ctx, T section, int pageIdx, int screenWidth, int screenHeight, int mouseX, int mouseY, boolean right){
		ClientWorld world = client().world;
		world.getRecipeManager().get(section.getRecipeId()).ifPresent(recipe -> {
			ItemStack result = recipe.value().getResult(world.getRegistryManager());
			Optional<String> overrideName = recipe.value() instanceof RenamableRecipe rr ? rr.getTranslationKey() : Optional.empty();
			overrideName = overrideName.map(I18n::translate);
			renderResult(ctx, result, overrideName, right ? pageX + rightXOffset : pageX, pageY, screenWidth, screenHeight, section);
			renderRecipe(ctx, recipe.value(), section, pageIdx, screenWidth, screenHeight, mouseX, mouseY, right);
		});
	}
	
	public void renderAfter(DrawContext matrices, T section, int pageIdx, int screenWidth, int screenHeight, int mouseX, int mouseY, boolean right){
		ClientWorld world = client().world;
		world.getRecipeManager().get(section.getRecipeId()).ifPresent(recipe -> {
			ItemStack result = recipe.value().getResult(world.getRegistryManager());
			renderResultTooltip(matrices, result, right ? pageX + rightXOffset : pageX, pageY, mouseX, mouseY, screenWidth, screenHeight);
			renderRecipeTooltips(matrices, recipe.value(), section, pageIdx, screenWidth, screenHeight, mouseX, mouseY, right);
		});
	}
	
	protected abstract void renderRecipe(DrawContext ctx, Recipe<?> recipe, T section, int pageIdx, int screenWidth, int screenHeight, int mouseX, int mouseY, boolean right);
	
	protected abstract void renderRecipeTooltips(DrawContext ctx, Recipe<?> recipe, T section, int pageIdx, int screenWidth, int screenHeight, int mouseX, int mouseY, boolean right);
	
	// static for use in WandInteractionSectionRenderer
	public static void renderResult(DrawContext ctx, ItemStack stack, Optional<String> overrideName, int x, int y, int screenWidth, int screenHeight, EntrySection section){
		MatrixStack matrices = ctx.getMatrices();
		matrices.push();
		MinecraftClient client = MinecraftClient.getInstance();
		TextRenderer textRenderer = client.textRenderer;
		int rX = x + (screenWidth - 256) / 2 + (pageWidth - 58) / 2;
		int rY = y + (screenHeight - bgHeight) / 2 + 16 - heightOffset;
		ctx.drawTexture(overlayTexture(section), rX, rY, 101, 1, 167, 58, 20, 256, 256);
		ctx.drawItem(stack, rX + 29 - 8, rY + 10 - 8);
		ctx.drawItemInSlot(textRenderer, stack, rX + 29 - 8, rY + 10 - 8);
		String name = overrideName.orElse(stack.getName().getString());
		// TODO: clean up multi-line logic
		//       ideally we could display small text above or below easily
		boolean wasNewline = false;
		if(name.contains(":") || (wasNewline = name.contains("\n"))){
			String[] split = name.split("[:\n]", 2);
			String prefix = split[0];
			if(!wasNewline)
				prefix += ":";
			name = split[1].trim();
			matrices.push();
			int stX = x + (screenWidth - 256) / 2 + (int)(pageWidth - textRenderer.getWidth(prefix)*0.8f) / 2;
			int stY = y + (screenHeight - bgHeight) / 2 + 8 - textRenderer.fontHeight - heightOffset;
			matrices.translate(stX, stY, 0);
			matrices.scale(0.8f, 0.8f, 1f);
			ctx.drawText(textRenderer, prefix, 0, 0, 0x000000, false);
			matrices.pop();
			matrices.translate(0, 5, 0);
		}
		
		int stX = x + (screenWidth - 256) / 2 + (pageWidth - textRenderer.getWidth(name)) / 2;
		int stY = y + (screenHeight - bgHeight) / 2 + 11 - textRenderer.fontHeight - heightOffset;
		ctx.drawText(textRenderer, name, stX, stY, 0x000000, false);
		matrices.pop();
	}
	
	public void renderResultTooltip(DrawContext ctx, ItemStack stack, int x, int y, int mouseX, int mouseY, int screenWidth, int screenHeight){
		int rX = x + (screenWidth - 256) / 2 + (pageWidth - 58) / 2 + 21;
		int rY = y + (screenHeight - bgHeight) / 2 + 18 - heightOffset;
		tooltipArea(ctx, stack, mouseX, mouseY, rX, rY);
	}
	
	protected int displayIdx(int max){
		return (int)((client().world.getTime() / 30) % max);
	}
	
	protected ItemStack displayStack(ItemStack[] stacks){
		if(stacks.length == 0)
			return Items.BARRIER.getDefaultStack();
		return stacks[displayIdx(stacks.length)];
	}
	
	public int span(T section, PlayerEntity player){
		return 1;
	}
}