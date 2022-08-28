package arcana.client;

import arcana.aspects.Aspect;
import arcana.aspects.AspectMap;
import arcana.aspects.Aspects;
import arcana.items.WandItem;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;

import java.util.List;

public record WandAspectsTooltipComponent(ItemStack wand) implements TooltipComponent{
	
	public int getHeight(){
		return 19 + 7 + 2;
	}
	
	public int getWidth(TextRenderer textRenderer){
		return 19 * 6 + 2;
	}
	
	public void drawItems(TextRenderer text, int x, int y, MatrixStack matrices, ItemRenderer itemRenderer, int z){
		AspectMap aspects = WandItem.aspectsFrom(wand);
		List<Aspect> primals = Aspects.PRIMALS;
		for(int i = 0; i < primals.size(); i++){
			Aspect primal = primals.get(i);
			int py = y + 1 + (i % 2 == 0 ? 7 : 0);
			AspectRenderer.renderAspectStack(primal, aspects.get(primal), matrices, text, true, x + i * 19 + 1, py, z);
		}
	}
}