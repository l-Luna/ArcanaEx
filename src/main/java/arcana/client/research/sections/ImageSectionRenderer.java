package arcana.client.research.sections;

import arcana.client.research.EntrySectionRenderer;
import arcana.research.sections.ImageSection;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;

import static arcana.screens.ResearchEntryScreen.*;

public class ImageSectionRenderer implements EntrySectionRenderer<ImageSection>{
	
	public void render(DrawContext ctx, ImageSection image, int idx, int screenW, int screenH, int mX, int mY, boolean right){
		ctx.drawTexture(image.getImage(), (right ? pageX + rightXOffset : pageX) + (screenW - 256) / 2, pageY + (screenH - bgHeight) / 2 - heightOffset, 0, 0, pageWidth, pageHeight);
	}
	
	public void renderAfter(DrawContext ctx, ImageSection image, int idx, int screenW, int screenH, int mX, int mY, boolean right){
		// no-op
	}
	
	public int span(ImageSection section, PlayerEntity player){
		return 1;
	}
}