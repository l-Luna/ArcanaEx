package arcana.client.research.sections;

import arcana.aspects.Aspect;
import arcana.aspects.Aspects;
import arcana.client.AspectRenderHelper;
import arcana.client.research.EntrySectionRenderer;
import arcana.research.sections.AspectCombosSection;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

import java.util.List;

import static arcana.client.research.EntrySectionRenderer.overlayTexture;
import static arcana.screens.ResearchEntryScreen.*;

public class AspectCombosSectionRenderer implements EntrySectionRenderer<AspectCombosSection>{
	
	public void render(DrawContext ctx, AspectCombosSection section, int pageIdx, int screenWidth, int screenHeight, int mouseX, int mouseY, boolean right){
		int x = (right ? pageX + rightXOffset : pageX) + (screenWidth - 256) / 2 + 4;
		int y = pageY + (screenHeight - bgHeight) / 2 + 10 - heightOffset;
		
		List<Aspect> aspects = Aspects.getCompoundAspects();
		for(int i = pageIdx * 5; i < aspects.size() && i < (pageIdx + 1) * 5; i++){
			Aspect aspect = aspects.get(i);
			int dIdx = i - pageIdx * 5;
			Identifier texture = overlayTexture(section);
			if(i != pageIdx * 5)
				ctx.drawTexture(texture, x - 2, y + 30 * dIdx - 17, 101, 1, 194, 100, 20, 256, 256);
			ctx.drawTexture(texture, x - 2, y + 30 * dIdx - 2, 101, 1, 216, 100, 20, 256, 256);
			AspectRenderHelper.renderAspect(aspect.left(), ctx, x, y + 30 * dIdx, 101);
			AspectRenderHelper.renderAspect(aspect.right(), ctx, x + 34, y + 30 * dIdx, 101);
			AspectRenderHelper.renderAspect(aspect, ctx, x + 80, y + 30 * dIdx, 101);
		}
	}
	
	public void renderAfter(DrawContext ctx, AspectCombosSection section, int pageIdx, int screenWidth, int screenHeight, int mouseX, int mouseY, boolean right){
		int x = (right ? pageX + rightXOffset : pageX) + (screenWidth - 256) / 2 + 4;
		int y = pageY + (screenHeight - bgHeight) / 2 + 10 - heightOffset;
		
		List<Aspect> aspects = Aspects.getCompoundAspects();
		for(int i = pageIdx * 5; i < aspects.size() && i < (pageIdx + 1) * 5; i++){
			Aspect aspect = aspects.get(i);
			int dIdx = i - pageIdx * 5;
			tooltipArea(ctx, aspect.left(), mouseX, mouseY, x, y + 30 * dIdx);
			tooltipArea(ctx, aspect.right(), mouseX, mouseY, x + 34, y + 30 * dIdx);
			tooltipArea(ctx, aspect, mouseX, mouseY, x + 80, y + 30 * dIdx);
		}
	}
	
	public int span(AspectCombosSection section, PlayerEntity player){
		return (int)Math.ceil((Aspects.aspects.size() - 6) / 5f);
	}
}