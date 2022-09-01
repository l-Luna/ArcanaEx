package arcana.client.research.sections;

import arcana.client.research.EntrySectionRenderer;
import arcana.research.sections.WandInteractionSection;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

import static arcana.client.research.EntrySectionRenderer.overlayTexture;
import static arcana.screens.ResearchEntryScreen.*;
import static net.minecraft.client.gui.DrawableHelper.drawTexture;

public class WandInteractionSectionRenderer implements EntrySectionRenderer<WandInteractionSection>{
	
	public void render(MatrixStack matrices, WandInteractionSection section, int pageIdx, int screenWidth, int screenHeight, int mouseX, int mouseY, boolean right){
		int x = right ? pageX + rightXOffset : pageX;
		
		AbstractRecipeSectionRenderer.renderResult(matrices, new ItemStack(section.getResult()), x, pageY, screenWidth, screenHeight, section);
		
		int inputX = x + (screenWidth - 256 + pageWidth) / 2 - 8;
		int inputY = pageY + (screenHeight - bgHeight + pageHeight) / 2 - 8 + 35 - heightOffset;
		RenderSystem.setShaderTexture(0, overlayTexture(section));
		drawTexture(matrices, inputX - 34, inputY - 44, 101, 159, 75, 84, 84, 256, 256);
		drawTexture(matrices, inputX - 35, inputY - 27, 101, 1, 145, 20, 20, 256, 256);
		client().getItemRenderer().renderInGui(new ItemStack(section.getInput()), inputX, inputY);
	}
	
	public void renderAfter(MatrixStack matrices, WandInteractionSection section, int pageIdx, int screenWidth, int screenHeight, int mouseX, int mouseY, boolean right){
		int x = right ? pageX + rightXOffset : pageX;
		int inputX = x + (screenWidth - 256 + pageWidth) / 2 - 8;
		int inputY = pageY + (screenHeight - bgHeight + pageHeight) / 2 - 8 + 35 - heightOffset;
		tooltipArea(matrices, new ItemStack(section.getInput()), mouseX, mouseY, inputX, inputY);
		
		int rX = x + (screenWidth - 256) / 2 + (pageWidth - 58) / 2 + 21;
		int rY = pageY + (screenHeight - bgHeight) / 2 + 18 - heightOffset;
		tooltipArea(matrices, new ItemStack(section.getResult()), mouseX, mouseY, rX, rY);
	}
	
	public int span(WandInteractionSection section, PlayerEntity player){
		return 1;
	}
}
