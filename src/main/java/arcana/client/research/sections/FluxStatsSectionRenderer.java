package arcana.client.research.sections;

import arcana.Arcana;
import arcana.aura.AuraWorld;
import arcana.aura.FluxOrigin;
import arcana.client.research.EntrySectionRenderer;
import arcana.research.sections.FluxStatsSection;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static arcana.screens.ResearchEntryScreen.*;

public class FluxStatsSectionRenderer implements EntrySectionRenderer<FluxStatsSection>{
	
	public void render(DrawContext ctx, FluxStatsSection section, int pageIdx, int screenWidth, int screenHeight, int mouseX, int mouseY, boolean right){
		int x = (right ? pageX + rightXOffset : pageX) + (screenWidth - 256) / 2;
		int y = pageY + (screenHeight - bgHeight) / 2 + 10 - heightOffset;
		
		Map<FluxOrigin, Float> stats = AuraWorld.from(MinecraftClient.getInstance().world).globalFluxStats();
		List<FluxOrigin> ordOrigins = new ArrayList<>(stats.keySet());
		TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
		for(int i = pageIdx * 5; i < stats.size() && i < (pageIdx + 1) * 5; i++){
			int dIdx = i - pageIdx * 5;
			FluxOrigin origin = ordOrigins.get(i);
			
			if(Registries.ITEM.containsId(origin.sprite))
				ctx.drawItem(new ItemStack(Registries.ITEM.get(origin.sprite)), x, y + 30 * dIdx);
			else
				ctx.drawTexture(origin.sprite, x, y, 1, 0, 0, 16, 16, 16, 16);
			ctx.getMatrices().push();
			ctx.getMatrices().translate(x + 20, y + 30 * dIdx, 0);
			float scaling = Arcana.CONFIG.textScaling;
			ctx.getMatrices().scale(scaling, scaling, 1);
			ctx.drawText(textRenderer, Text.translatable(origin.translationKey), 0, 1, 0xFF000000, false);
			ctx.drawText(textRenderer, String.format("%.2f", stats.get(origin)), 0, 15, 0xFF000000, false);
			ctx.getMatrices().pop();
		}
	}
	
	public void renderAfter(DrawContext ctx, FluxStatsSection section, int pageIdx, int screenWidth, int screenHeight, int mouseX, int mouseY, boolean right){
	
	}
	
	public int span(FluxStatsSection section, PlayerEntity player){
		return (int)Math.ceil(AuraWorld.from(player.getWorld()).globalFluxStats().size() / 5f);
	}
}