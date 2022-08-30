package arcana.client.research.sections;

import arcana.client.research.EntrySectionRenderer;
import arcana.research.sections.ImageSection;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;

import static arcana.screens.ResearchEntryScreen.*;

public class ImageSectionRenderer implements EntrySectionRenderer<ImageSection>{
	
	public void render(MatrixStack stack, ImageSection image, int idx, int screenW, int screenH, int mX, int mY, boolean right){
		RenderSystem.setShaderTexture(0, image.getImage());
		client().currentScreen.drawTexture(stack, (right ? pageX + rightXOffset : pageX) + (screenW - 256) / 2, pageY + (screenH - 181) / 2 + heightOffset, 0, 0, pageWidth, pageHeight);
	}
	
	public void renderAfter(MatrixStack stack, ImageSection image, int idx, int screenW, int screenH, int mX, int mY, boolean right){
		// no-op
	}
	
	public int span(ImageSection section, PlayerEntity player){
		return 1;
	}
}