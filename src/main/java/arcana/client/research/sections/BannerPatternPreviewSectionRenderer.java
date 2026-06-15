package arcana.client.research.sections;

import arcana.client.research.EntrySectionRenderer;
import arcana.research.sections.BannerPatternPreviewSection;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import java.util.Optional;

import static arcana.client.research.EntrySectionRenderer.overlayTexture;
import static arcana.screens.ResearchEntryScreen.*;
import static net.minecraft.client.gui.DrawableHelper.drawTexture;

public class BannerPatternPreviewSectionRenderer implements EntrySectionRenderer<BannerPatternPreviewSection>{
	
	public void render(DrawContext ctx, BannerPatternPreviewSection section, int pageIdx, int screenWidth, int screenHeight, int mouseX, int mouseY, boolean right){
		int x = right ? pageX + rightXOffset : pageX;
		
		ItemStack stack = new ItemStack(section.getPatternItem());
		AbstractRecipeSectionRenderer.renderResult(
				matrices,
				stack,
				Optional.of(I18n.translate(stack.getTranslationKey()) + ": " + I18n.translate(stack.getTranslationKey() + ".desc")),
				x,
				pageY,
				screenWidth,
				screenHeight,
				section
		);
		
		int bannerX = x + (screenWidth - 256 + pageWidth) / 2 - 30;
		int bannerY = pageY + (screenHeight - bgHeight + pageHeight) / 2 - 8 - heightOffset;
		int shieldX = bannerX + 40, shieldY = bannerY + 15;
		RenderSystem.setShaderTexture(0, overlayTexture(section));
		drawTexture(matrices, bannerX, bannerY, 101, 132, 161, 30, 60, 256, 256);
		drawTexture(matrices, shieldX, shieldY, 101, 164, 161, 14, 22, 256, 256);
		
		RenderSystem.setShaderTexture(0, getSpriteId(section.getPattern(), true));
		drawTexture(matrices, bannerX + 4, bannerY + 6, 101, 0, 0, 22, 41, 64, 64);
		RenderSystem.setShaderTexture(0, getSpriteId(section.getPattern(), false));
		drawTexture(matrices, shieldX + 1, shieldY + 1, 101, 2, 2, 10, 20, 64, 64);
	}
	
	public void renderAfter(DrawContext ctx, BannerPatternPreviewSection section, int pageIdx, int screenWidth, int screenHeight, int mouseX, int mouseY, boolean right){
		int x = right ? pageX + rightXOffset : pageX;
		int rX = x + (screenWidth - 256) / 2 + (pageWidth - 58) / 2 + 21;
		int rY = pageY + (screenHeight - bgHeight) / 2 + 18 - heightOffset;
		tooltipArea(matrices, new ItemStack(section.getPatternItem()), mouseX, mouseY, rX, rY);
	}
	
	public int span(BannerPatternPreviewSection section, PlayerEntity player){
		return 1;
	}
	
	// from BannerPattern
	private static Identifier getSpriteId(Identifier pattern, boolean banner) {
		return Identifier.of(pattern.getNamespace(), "textures/entity/" + (banner ? "banner" : "shield") + "/" + pattern.getPath() + ".png");
	}
}