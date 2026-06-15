package arcana.client.tooltip;

import arcana.aspects.AspectStack;
import arcana.client.AspectRenderHelper;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record ItemAspectsTooltipComponent(List<AspectStack> aspects, @Nullable TooltipComponent inner) implements TooltipComponent{
	
	public int getHeight(){
		return 20 * MathHelper.ceil(aspects.size() / 6f) + (inner != null ? inner.getHeight() : 0);
	}
	
	public int getWidth(TextRenderer text){
		return Math.max(Math.min(aspects().size(), 6) * 19 + 2, inner == null ? 0 : inner.getWidth(text));
	}
	
	public void drawItems(TextRenderer textRenderer, int x, int y, DrawContext ctx){
		if(inner != null){
			inner.drawItems(textRenderer, x, y, ctx);
			y += inner.getHeight();
		}
		int n = 0;
		for(AspectStack aspect : aspects){
			AspectRenderHelper.renderAspectStack(aspect, ctx, textRenderer, x + n * 19 + 1, y + 1, 0);
			n++;
			if(n >= 6){
				n = 0;
				y += 19;
			}
		}
	}
}