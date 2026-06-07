package arcana.integration.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.emi.emi.screen.WidgetGroup;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * Allows dynamically generating groups of widgets, for recipes with no set item count.
 * Note that slot widgets nested inside will not have identical behaviour to top-level ones.
 */
public class DynamicWrapperGroupWidget extends Widget{
	
	private static final int INCREMENT = 800;
	
	private final WidgetGroup inner;
	private final BiConsumer<WidgetGroup, Long> generator;
	
	private long lastGenerated = -1;
	
	public DynamicWrapperGroupWidget(EmiRecipe recipe, WidgetHolder holder, BiConsumer<WidgetGroup, Long> generator){
		this.generator = generator;
		// TODO: positioning a dynamic section inside a non-dynamic recipe?
		inner = new WidgetGroup(recipe, 0, 0, holder.getWidth(), holder.getHeight());
	}
	
	public Bounds getBounds(){
		return new Bounds(inner.x, inner.y, inner.width, inner.height);
	}
	
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta){
		long time = System.currentTimeMillis() / INCREMENT;
		if(lastGenerated == -1 || time > lastGenerated){
			lastGenerated = time;
			inner.widgets.clear();
			generator.accept(inner, time);
		}
		matrices.push();
		matrices.translate(inner.x, inner.y, 0);
		for(Widget widget : inner.widgets)
			widget.render(matrices, mouseX, mouseY, delta);
		matrices.pop();
	}
	
	public List<TooltipComponent> getTooltip(int mouseX, int mouseY){
		int adjMouseX = mouseX - inner.x, adjMouseY = mouseY - inner.y;
		for(Widget widget : inner.widgets)
			if(widget.getBounds().contains(adjMouseX, adjMouseY)){
				List<TooltipComponent> tooltip = widget.getTooltip(adjMouseX, adjMouseY);
				if(!tooltip.isEmpty())
					return tooltip;
			}
		return super.getTooltip(mouseX, mouseY);
	}
	
	public boolean mouseClicked(int mouseX, int mouseY, int button){
		int adjMouseX = mouseX - inner.x, adjMouseY = mouseY - inner.y;
		for(Widget widget : inner.widgets)
			if(widget.getBounds().contains(adjMouseX, adjMouseY) && widget.mouseClicked(adjMouseX, adjMouseY, button))
				return true;
		return super.mouseClicked(mouseX, mouseY, button);
	}
	
	public boolean keyPressed(int keyCode, int scanCode, int modifiers){
		// EMI doesn't give us the mouse position but does use it for bounds checks, so duplicate that behaviour
		MinecraftClient client = MinecraftClient.getInstance();
		Window window = client.getWindow();
		int mouseX = (int) (client.mouse.getX() * window.getScaledWidth() / window.getWidth());
		int mouseY = (int) (client.mouse.getY() * window.getScaledHeight() / window.getHeight());
		
		int adjMouseX = mouseX - inner.x, adjMouseY = mouseY - inner.y;
		for(Widget widget : inner.widgets)
			if(widget.getBounds().contains(adjMouseX, adjMouseY) && widget.keyPressed(keyCode, scanCode, modifiers))
				return true;
		return super.keyPressed(keyCode, scanCode, modifiers);
	}
}