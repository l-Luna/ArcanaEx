package arcana.client.research.sections;

import arcana.aspects.AspectMap;
import arcana.aspects.AspectStack;
import arcana.client.AspectRenderHelper;
import arcana.recipes.infusion.InfusionEnchantmentRecipe;
import arcana.recipes.infusion.SimpleInfusionRecipe;
import arcana.research.sections.InfusionRecipeSection;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.ArrayList;
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
		if(recipe instanceof SimpleInfusionRecipe ir)
			renderInfusionRecipe(
					displayStack(ir.centralIngredient().getMatchingStacks()), ir.outerIngredients(), ir.aspects().asStacks(), ir.instability(),
					ctx, screenWidth, screenHeight, right, section
			);
		else if(recipe instanceof InfusionEnchantmentRecipe ier){
			RegistryEntry<Enchantment> enchantment = ier.getEnchantment();
			int currentLevel = displayIdx(enchantment.value().getMaxLevel());
			int targetLevel = currentLevel + 1;
			int multiplier = 1 << currentLevel;
			ItemStack input = ier.getPreviewStack().copy();
			ItemStack output = ier.getPreviewStack().copy();
			if(targetLevel > 1)
				EnchantmentHelper.apply(input, b -> b.add(enchantment, targetLevel - 1));
			EnchantmentHelper.apply(output, b -> b.add(enchantment, targetLevel));
			List<Ingredient> outers = new ArrayList<>(ier.getBaseIngredients().size() * targetLevel);
			for(int i = 0; i < multiplier; i++)
				outers.addAll(ier.getBaseIngredients());
			AspectMap aspects = ier.getBaseAspects().copy();
			aspects.multiply(multiplier);
			renderInfusionRecipe(
					input, outers, aspects.asStacks(), ier.getBaseInstability() + targetLevel,
					ctx, screenWidth, screenHeight, right, section
			);
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
		if(recipe instanceof SimpleInfusionRecipe ir)
			renderInfusionRecipeTooltips(
					displayStack(ir.centralIngredient().getMatchingStacks()), ir.outerIngredients(), ir.aspects().asStacks(),
					ctx, screenWidth, screenHeight, mouseX, mouseY, right
			);
		else if(recipe instanceof InfusionEnchantmentRecipe ier){
			RegistryEntry<Enchantment> enchantment = ier.getEnchantment();
			int currentLevel = displayIdx(enchantment.value().getMaxLevel());
			int targetLevel = currentLevel + 1;
			int multiplier = 1 << currentLevel;
			ItemStack input = ier.getPreviewStack().copy();
			ItemStack output = ier.getPreviewStack().copy();
			if(targetLevel > 1)
				EnchantmentHelper.apply(input, b -> b.add(enchantment, targetLevel - 1));
			EnchantmentHelper.apply(output, b -> b.add(enchantment, targetLevel));
			List<Ingredient> outers = new ArrayList<>(ier.getBaseIngredients().size() * targetLevel);
			for(int i = 0; i < multiplier; i++)
				outers.addAll(ier.getBaseIngredients());
			AspectMap aspects = ier.getBaseAspects().copy();
			aspects.multiply(multiplier);
			renderInfusionRecipeTooltips(
					input, outers, aspects.asStacks(),
					ctx, screenWidth, screenHeight, mouseX, mouseY, right
			);
		}
	}
	
	protected ItemStack getRecipeResult(RecipeEntry<?> recipe, ClientWorld world){
		if(recipe.value() instanceof InfusionEnchantmentRecipe ier){
			RegistryEntry<Enchantment> enchantment = ier.getEnchantment();
			ItemStack output = ier.getPreviewStack().copy();
			EnchantmentHelper.apply(output, b -> b.add(enchantment, displayIdx(enchantment.value().getMaxLevel()) + 1));
			return output;
		}
		return super.getRecipeResult(recipe, world);
	}
	
	//
	
	protected void renderInfusionRecipe(ItemStack central, List<Ingredient> outerIngredients, List<AspectStack> aspects, int instability,
	                                    DrawContext ctx, int screenWidth, int screenHeight, boolean right, InfusionRecipeSection section){
		int x = right ? pageX + rightXOffset : pageX;
		int ulX = x + (screenWidth - 256 + pageWidth) / 2 - 25;
		int ulY = pageY + (screenHeight - bgHeight + pageHeight) / 2 - 10 - heightOffset - 14;
		
		ctx.drawTexture(overlayTexture(section), ulX - 10, ulY - 10, 101, 1, 73, 70, 70, 256, 256);
		
		int midX = ulX + 35 - 18;
		int midY = ulY + 35 - 18;
		
		ctx.drawItem(central, midX, midY);
		
		for(int i = 0; i < outerIngredients.size(); i++){
			ItemStack[] stacks = outerIngredients.get(i).getMatchingStacks();
			if(stacks.length > 0){
				int offX = (int)(32 * Math.sin(2 * Math.PI * (i / (float)outerIngredients.size())));
				int offY = (int)(32 * Math.cos(2 * Math.PI * (i / (float)outerIngredients.size())));
				ctx.drawItem(displayStack(stacks), midX + offX, midY + offY);
			}
		}
		
		MutableText text = Text.translatable("recipe.infusion.instability.title", Text.translatable("recipe.infusion.instability." + instability));
		ctx.drawText(textRenderer(), text, (int)(midX + 8 - textRenderer().getWidth(text.getString()) / 2f), midY + 54, 0, false);
		
		int spacing = (aspects.size() == 1) ? 0 : (aspects.size() >= 6) ? 1 : (aspects.size() < 4) ? 3 : 2;
		int aspectX = ulX + 73 / 2 - (aspects.size() * (16 + spacing * 2)) / 2 - 11;
		int aspectY = ulY + 86;
		
		for(int i = 0, length = aspects.size(); i < length; i++)
			AspectRenderHelper.renderAspectStack(aspects.get(i), ctx, client().textRenderer, aspectX + i * (16 + 2 * spacing) + spacing, aspectY, 101);
	}
	
	protected void renderInfusionRecipeTooltips(ItemStack central, List<Ingredient> outerIngredients, List<AspectStack> aspects,
	                                            DrawContext ctx, int screenWidth, int screenHeight, int mouseX, int mouseY, boolean right){
		int x = right ? pageX + rightXOffset : pageX;
		int ulX = x + (screenWidth - 256 + pageWidth) / 2 - 25;
		int ulY = pageY + (screenHeight - bgHeight + pageHeight) / 2 - 10 - heightOffset - 14;
		
		int midX = ulX + 35 - 18;
		int midY = ulY + 35 - 18;
		
		tooltipArea(ctx, central, mouseX, mouseY, midX, midY);
		
		for(int i = 0; i < outerIngredients.size(); i++){
			ItemStack[] stacks = outerIngredients.get(i).getMatchingStacks();
			if(stacks.length > 0){
				int offX = (int)(32 * Math.sin(2 * Math.PI * (i / (float)outerIngredients.size())));
				int offY = (int)(32 * Math.cos(2 * Math.PI * (i / (float)outerIngredients.size())));
				tooltipArea(ctx, displayStack(stacks), mouseX, mouseY, midX + offX, midY + offY);
			}
		}
		
		int spacing = (aspects.size() == 1) ? 0 : (aspects.size() >= 6) ? 1 : (aspects.size() < 4) ? 3 : 2;
		int aspectX = ulX + 73 / 2 - (aspects.size() * (16 + spacing * 2)) / 2 - 11;
		int aspectY = ulY + 86;
		
		for(int i = 0, length = aspects.size(); i < length; i++)
			tooltipArea(ctx, aspects.get(i).type(), mouseX, mouseY, aspectX + i * (16 + 2 * spacing) + spacing, aspectY);
	}
}