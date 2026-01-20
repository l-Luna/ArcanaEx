package arcana.client.tooltip;

import arcana.aspects.AspectStack;
import arcana.client.AspectRenderHelper;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
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
	
	public void drawItems(TextRenderer textRenderer, int x, int y, MatrixStack matrices, ItemRenderer itemRenderer, int z){
		if(inner != null){
			inner.drawItems(textRenderer, x, y, matrices, itemRenderer, z);
			y += inner.getHeight();
		}
		int n = 0;
		for(AspectStack aspect : aspects){
			AspectRenderHelper.renderAspectStack(aspect, matrices, textRenderer, x + n * 19 + 1, y + 1, z);
			n++;
			if(n >= 6){
				n = 0;
				y += 19;
			}
		}
	}
}