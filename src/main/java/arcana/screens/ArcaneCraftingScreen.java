package arcana.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import static arcana.Arcana.arcId;

public class ArcaneCraftingScreen extends HandledScreen<ArcaneCraftingScreenHandler>{
	
	private static final Identifier TEXTURE = arcId("textures/gui/container/arcane_crafting_table.png");
	
	public ArcaneCraftingScreen(ArcaneCraftingScreenHandler handler, PlayerInventory inventory, Text title){
		super(handler, inventory, title);
	}
	
	protected void init(){
		backgroundWidth = 184;
		backgroundHeight = 233;
		super.init();
		titleX = 10;
		titleY = -5;
	}
	
	protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY){
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1, 1, 1, 1);
		RenderSystem.setShaderTexture(0, TEXTURE);
		drawTexture(matrices, x, y, 0, 0, backgroundWidth, backgroundHeight);
		
		// draw required aspects for recipe
	}
	
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		renderBackground(matrices);
		super.render(matrices, mouseX, mouseY, delta);
		drawMouseoverTooltip(matrices, mouseX, mouseY);
	}
	
	protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY){
		// no-op - don't draw label
	}
}