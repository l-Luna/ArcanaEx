package arcana.client.tooltip;

import arcana.aspects.Aspect;
import arcana.aspects.Aspects;
import arcana.client.AspectRenderHelper;
import arcana.client.RenderHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import static arcana.Arcana.arcId;

public record MagicMirrorTooltipComponent(@Nullable UUID tag) implements TooltipComponent{
	
	private static final Identifier BG_TEXTURE = arcId("textures/gui/magic_mirror_id.png");
	private static final Identifier QUESTION_MARK_TEXTURE = arcId("textures/gui/unknown_aspect_b.png");
	
	public int getHeight(){
		return 26;
	}
	
	public int getWidth(TextRenderer textRenderer){
		return 175;
	}
	
	public void drawItems(TextRenderer textRenderer, int x, int y, MatrixStack matrices, ItemRenderer itemRenderer, int z){
		RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
		RenderSystem.setShaderColor(1, 1, 1, 1);
		RenderSystem.setShaderTexture(0, BG_TEXTURE);
		RenderHelper.drawTexture(matrices, x, y, z, 0, 0, 175, 24, 256, 256, 1, 1, 1, 1);
		if(tag == null){
			RenderSystem.setShaderTexture(0, QUESTION_MARK_TEXTURE);
			for(int g = 0; g < 2; g++)
				for(int i = 0; i < 4; i++)
					RenderHelper.drawTexture(matrices, x + 6 + i*20 + g*87, y + 4, z, 0, 0, 16, 16, 16, 16, 1, 1, 1, 1);
		}else{
			Random rng = new Random(tag.hashCode());
			List<Aspect> compounds = Aspects.getOrderedAspects();
			for(int g = 0; g < 2; g++)
				for(int i = 0; i < 4; i++)
					AspectRenderHelper.renderAspect(compounds.get(rng.nextInt(compounds.size())), matrices, x + 6 + i*20 + g*87, y + 4, z);
		}
	}
}