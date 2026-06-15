package arcana.client.research.sections;

import arcana.client.research.EntrySectionRenderer;
import arcana.research.sections.WandInteractionSection;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import java.util.Optional;

import static arcana.client.research.EntrySectionRenderer.overlayTexture;
import static arcana.screens.ResearchEntryScreen.*;

public class WandInteractionSectionRenderer implements EntrySectionRenderer<WandInteractionSection>{
	
	public void render(DrawContext ctx, WandInteractionSection section, int pageIdx, int screenWidth, int screenHeight, int mouseX, int mouseY, boolean right){
		int x = right ? pageX + rightXOffset : pageX;
		
		AbstractRecipeSectionRenderer.renderResult(ctx, new ItemStack(section.getResult()), Optional.empty(), x, pageY, screenWidth, screenHeight, section);
		
		int inputX = x + (screenWidth - 256 + pageWidth) / 2 - 8;
		int inputY = pageY + (screenHeight - bgHeight + pageHeight) / 2 - 8 + 35 - heightOffset;
		Identifier texture = overlayTexture(section);
		ctx.drawTexture(texture, inputX - 34, inputY - 44, 101, 159, 75, 84, 84, 256, 256);
		ctx.drawTexture(texture, inputX - 35, inputY - 27, 101, 1, 145, 20, 20, 256, 256);
		ctx.drawItem(new ItemStack(section.getInput()), inputX, inputY);
	}
	
	public void renderAfter(DrawContext ctx, WandInteractionSection section, int pageIdx, int screenWidth, int screenHeight, int mouseX, int mouseY, boolean right){
		int x = right ? pageX + rightXOffset : pageX;
		int inputX = x + (screenWidth - 256 + pageWidth) / 2 - 8;
		int inputY = pageY + (screenHeight - bgHeight + pageHeight) / 2 - 8 + 35 - heightOffset;
		tooltipArea(ctx, new ItemStack(section.getInput()), mouseX, mouseY, inputX, inputY);
		
		int rX = x + (screenWidth - 256) / 2 + (pageWidth - 58) / 2 + 21;
		int rY = pageY + (screenHeight - bgHeight) / 2 + 18 - heightOffset;
		tooltipArea(ctx, new ItemStack(section.getResult()), mouseX, mouseY, rX, rY);
	}
	
	public int span(WandInteractionSection section, PlayerEntity player){
		return 1;
	}
}