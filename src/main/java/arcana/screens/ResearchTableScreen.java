package arcana.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import static arcana.Arcana.arcId;

public class ResearchTableScreen extends HandledScreen<ResearchTableScreenHandler>{
	
	private static final Identifier texture = arcId("textures/gui/container/research_table.png");
	
	public ResearchTableScreen(ResearchTableScreenHandler handler, PlayerInventory inventory, Text title){
		super(handler, inventory, title);
	}
	
	protected void init(){
		backgroundWidth = 338;
		backgroundHeight = 241;
		super.init();
		titleY = -100;
	}
	
	protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY){
		RenderSystem.setShaderTexture(0, texture);
		drawRtTexture(matrices, x, y, 0, 0, 0, backgroundWidth, backgroundHeight);
	}
	
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta){
		renderBackground(matrices);
		super.render(matrices, mouseX, mouseY, delta);
		drawMouseoverTooltip(matrices, mouseX, mouseY);
	}
	
	protected void drawRtTexture(MatrixStack matrices, int x, int y, int z, float u, float v, int width, int height){
		drawTexture(matrices, x, y, z, u, v, width, height, 338, 338);
	}
	
	protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY){
		// no-op - don't draw label
	}
}