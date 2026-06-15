package arcana.client.tooltip;

import arcana.aspects.Aspect;
import arcana.aspects.Aspects;
import arcana.aspects.ScaledAspectMap;
import arcana.client.AspectRenderHelper;
import arcana.items.WandItem;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.item.ItemStack;

import java.util.List;

public record WandAspectsTooltipComponent(ItemStack wand) implements TooltipComponent{
	
	public int getHeight(){
		return 19 + 7 + 2;
	}
	
	public int getWidth(TextRenderer textRenderer){
		return 19 * 6 + 2;
	}
	
	public void drawItems(TextRenderer text, int x, int y, DrawContext ctx){
		ScaledAspectMap aspects = WandItem.aspectsFrom(wand);
		List<Aspect> primals = Aspects.primals;
		for(int i = 0; i < primals.size(); i++){
			Aspect primal = primals.get(i);
			int py = y + 1 + (i % 2 == 0 ? 7 : 0);
			AspectRenderHelper.renderAspectStack(primal, aspects.get(primal), ctx, text, true, x + i * 19 + 1, py, 0);
		}
	}
}