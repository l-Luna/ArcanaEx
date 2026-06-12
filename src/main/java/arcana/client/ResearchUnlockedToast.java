package arcana.client;

import arcana.research.Entry;
import arcana.research.Icon;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

public class ResearchUnlockedToast implements Toast{
	
	private final Entry entry;
	private final boolean addendum;
	
	public ResearchUnlockedToast(Entry entry, boolean addendum){
		this.entry = entry;
		this.addendum = addendum;
	}
	
	public Visibility draw(DrawContext ctx, ToastManager manager, long startTime){
		ctx.drawGuiTexture(Identifier.ofVanilla("toast/advancement"), 0, 0, 0, getWidth(), getHeight());
		
		List<Icon> icons = entry.icons();
		RenderHelper.renderIcon(ctx, icons.get((int)((startTime / 200) % icons.size())), 8, 8, 1, 1, entry.getIntMeta("icon_frames"));
		
		TextRenderer text = manager.getClient().textRenderer;
		ctx.drawText(text, Text.translatable(addendum ? "message.arcana.addendum_toast" : "message.arcana.research_toast"), 30, 7, 0xffffff00, false);
		ctx.drawText(text, Text.translatable(entry.name()), 30, 18, 0xffffffff, false);
		
		return startTime > 5000 ? Visibility.HIDE : Visibility.SHOW;
	}
}
