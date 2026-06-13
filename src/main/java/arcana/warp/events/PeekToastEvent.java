package arcana.warp.events;

import arcana.warp.WarpEvent;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

public class PeekToastEvent extends WarpEvent{
	
	@Environment(EnvType.CLIENT)
	public void performOnClient(PlayerEntity player, boolean hadPrecursor){
		MinecraftClient.getInstance().getToastManager().add(new PeekToast());
	}
	
	public boolean isPrecursor(){
		return true;
	}
	
	public int minWarp(){
		return 1;
	}
	
	public boolean applicableTo(PlayerEntity player, boolean hasSignificantWarp){
		return true;
	}
	
	@Environment(EnvType.CLIENT)
	private static class PeekToast implements Toast{
		
		private static final Identifier TEXTURE = Identifier.ofVanilla("toast/advancement");
		
		public Visibility draw(DrawContext ctx, ToastManager manager, long startTime){
			RenderSystem.setShaderColor(1, 1, 1, 1);
			ctx.drawTexture(TEXTURE, 0, 0, 0, 0, getWidth(), getHeight());
			
			var text = manager.getClient().textRenderer;
			List<OrderedText> lines = text.wrapLines(Text.translatable("message.arcana.warp.peek"), 125);
			for(int i = 0; i < lines.size(); i++)
				ctx.drawText(manager.getClient().textRenderer, lines.get(i), 18, 13 + (text.fontHeight + 1) * i, -1, false);
			
			return startTime > 30 ? Visibility.HIDE : Visibility.SHOW;
		}
	}
}