package arcana.client;

import arcana.research.Entry;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class ResearchUnlockedToast implements Toast{
	
	private final Entry entry;
	
	public ResearchUnlockedToast(Entry entry){
		this.entry = entry;
	}
	
	public Visibility draw(MatrixStack matrices, ToastManager manager, long startTime){
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, TEXTURE);
		RenderSystem.setShaderColor(1, 1, 1, 1);
		manager.drawTexture(matrices, 0, 0, 0, 0, getWidth(), getHeight());
		
		var icons = entry.icons();
		RenderHelper.renderIcon(matrices, icons.get((int)((startTime / 200) % icons.size())), 8, 8, 1, 1, entry.getIntMeta("icon_frames"));
		
		var text = manager.getClient().textRenderer;
		text.draw(matrices, Text.translatable("message.arcana.research_toast"), 30, 7, 0xffffff00);
		text.draw(matrices, Text.translatable(entry.name()), 30, 18, 0xffffffff);
		
		return startTime > 5000 ? Visibility.HIDE : Visibility.SHOW;
	}
}
