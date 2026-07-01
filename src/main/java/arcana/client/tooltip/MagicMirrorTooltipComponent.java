package arcana.client.tooltip;

import arcana.aspects.Aspect;
import arcana.aspects.Aspects;
import arcana.client.AspectRenderHelper;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
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
	
	public void drawItems(TextRenderer textRenderer, int x, int y, DrawContext ctx){
		ctx.drawTexture(BG_TEXTURE, x, y, 0, 0, 175, 24, 256, 256);
		if(tag == null){
			for(int g = 0; g < 2; g++)
				for(int i = 0; i < 4; i++)
					ctx.drawTexture(QUESTION_MARK_TEXTURE, x + 6 + i*20 + g*87, y + 4, 0, 0, 16, 16, 16, 16);
		}else{
			Random rng = new Random(tag.hashCode());
			List<Aspect> compounds = Aspects.getOrderedAspects();
			for(int g = 0; g < 2; g++)
				for(int i = 0; i < 4; i++)
					AspectRenderHelper.renderAspect(compounds.get(rng.nextInt(compounds.size())), ctx, x + 6 + i*20 + g*87, y + 4, 0);
		}
	}
}