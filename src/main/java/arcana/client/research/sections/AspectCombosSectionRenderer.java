package arcana.client.research.sections;

import arcana.aspects.Aspect;
import arcana.aspects.Aspects;
import arcana.client.AspectRenderer;
import arcana.client.research.EntrySectionRenderer;
import arcana.research.sections.AspectCombosSection;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;

import java.util.List;

import static arcana.client.research.EntrySectionRenderer.overlayTexture;
import static arcana.screens.ResearchEntryScreen.*;

public class AspectCombosSectionRenderer implements EntrySectionRenderer<AspectCombosSection>{
	
	public void render(MatrixStack matrices, AspectCombosSection section, int pageIdx, int screenWidth, int screenHeight, int mouseX, int mouseY, boolean right){
		int x = (right ? pageX + rightXOffset : pageX) + (screenWidth - 256) / 2 + 4;
		int y = pageY + (screenHeight - bgHeight) / 2 + 10 - heightOffset;
		
		List<Aspect> aspects = Aspects.getCompoundAspects();
		for(int i = pageIdx * 5; i < aspects.size() && i < (pageIdx + 1) * 5; i++){
			Aspect aspect = aspects.get(i);
			int dIdx = i - pageIdx * 5;
			AspectRenderer.renderAspect(aspect.left(), matrices, x, y + 30 * dIdx, 101);
			AspectRenderer.renderAspect(aspect.right(), matrices, x + 40, y + 30 * dIdx, 101);
			AspectRenderer.renderAspect(aspect, matrices, x + 80, y + 30 * dIdx, 101);
			RenderSystem.setShaderTexture(0, overlayTexture(section));
			drawTexture(matrices, x + 20, y + 30 * dIdx, 101, 105, 161, 12, 13, 256, 256);
			drawTexture(matrices, x + 60, y + 30 * dIdx, 101, 118, 161, 12, 13, 256, 256);
		}
	}
	
	public void renderAfter(MatrixStack matrices, AspectCombosSection section, int pageIdx, int screenWidth, int screenHeight, int mouseX, int mouseY, boolean right){
		int x = (right ? pageX + rightXOffset : pageX) + (screenWidth - 256) / 2 + 4;
		int y = pageY + (screenHeight - bgHeight) / 2 + 10 - heightOffset;
		
		List<Aspect> aspects = Aspects.getCompoundAspects();
		for(int i = pageIdx * 5; i < aspects.size() && i < (pageIdx + 1) * 5; i++){
			Aspect aspect = aspects.get(i);
			int dIdx = i - pageIdx * 5;
			tooltipArea(matrices, aspect.left(), mouseX, mouseY, x, y + 30 * dIdx);
			tooltipArea(matrices, aspect.right(), mouseX, mouseY, x + 40, y + 30 * dIdx);
			tooltipArea(matrices, aspect, mouseX, mouseY, x + 80, y + 30 * dIdx);
		}
	}
	
	public int span(AspectCombosSection section, PlayerEntity player){
		return (int)(Math.ceil((Aspects.ASPECTS.size() - 6) / 5f));
	}
}