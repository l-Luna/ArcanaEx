package arcana.client;

import arcana.aspects.AspectStack;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record AspectsTooltipComponent(List<AspectStack> aspects, @Nullable TooltipComponent inner) implements TooltipComponent{
	
	public int getHeight(){
		return 20 + (inner != null ? inner.getHeight() : 0);
	}
	
	public int getWidth(TextRenderer text){
		return Math.max(aspects().size() * 19 + 2, inner == null ? 0 : inner.getWidth(text));
	}
	
	public void drawItems(TextRenderer textRenderer, int x, int y, MatrixStack matrices, ItemRenderer itemRenderer, int z){
		if(inner != null){
			inner.drawItems(textRenderer, x, y, matrices, itemRenderer, z);
			y += inner.getHeight();
		}
		for(int i = 0; i < aspects.size(); i++)
			AspectRenderer.renderAspectStack(aspects.get(i), matrices, textRenderer, x + i * 19 + 1, y + 1, z);
	}
}