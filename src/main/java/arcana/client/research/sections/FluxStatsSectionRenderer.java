package arcana.client.research.sections;

import arcana.Arcana;
import arcana.aura.AuraWorld;
import arcana.aura.FluxOrigin;
import arcana.client.research.EntrySectionRenderer;
import arcana.research.sections.FluxStatsSection;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static arcana.screens.ResearchEntryScreen.*;

public class FluxStatsSectionRenderer implements EntrySectionRenderer<FluxStatsSection>{
	
	public void render(MatrixStack matrices, FluxStatsSection section, int pageIdx, int screenWidth, int screenHeight, int mouseX, int mouseY, boolean right){
		int x = (right ? pageX + rightXOffset : pageX) + (screenWidth - 256) / 2;
		int y = pageY + (screenHeight - bgHeight) / 2 + 10 - heightOffset;
		
		Map<FluxOrigin, Float> stats = AuraWorld.from(MinecraftClient.getInstance().world).globalFluxStats();
		List<FluxOrigin> ordOrigins = new ArrayList<>(stats.keySet());
		var itemRenderer = MinecraftClient.getInstance().getItemRenderer();
		TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
		for(int i = pageIdx * 5; i < stats.size() && i < (pageIdx + 1) * 5; i++){
			int dIdx = i - pageIdx * 5;
			FluxOrigin origin = ordOrigins.get(i);
			
			if(Registry.ITEM.containsId(origin.sprite))
				itemRenderer.renderGuiItemIcon(new ItemStack(Registry.ITEM.get(origin.sprite)), x, y + 30 * dIdx);
			else{
				RenderSystem.setShader(GameRenderer::getPositionTexShader);
				RenderSystem.setShaderTexture(0, origin.sprite);
				DrawableHelper.drawTexture(matrices, x, y, 1, 0, 0, 16, 16, 16, 16);
			}
			matrices.push();
			matrices.translate(x + 20, y + 30 * dIdx, 0);
			float scaling = Arcana.CONFIG.textScaling;
			matrices.scale(scaling, scaling, 1);
			textRenderer.draw(
					matrices,
					Text.translatable(origin.translationKey),
					0,
					1,
					0xFF000000
			);
			textRenderer.draw(
					matrices,
					String.format("%.2f", stats.get(origin)),
					0,
					15,
					0xFF000000
			);
			matrices.pop();
		}
	}
	
	public void renderAfter(MatrixStack matrices, FluxStatsSection section, int pageIdx, int screenWidth, int screenHeight, int mouseX, int mouseY, boolean right){
	
	}
	
	public int span(FluxStatsSection section, PlayerEntity player){
		return (int)Math.ceil(AuraWorld.from(player.world).globalFluxStats().size() / 5f);
	}
}