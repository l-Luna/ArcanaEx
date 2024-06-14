package arcana.screens;

import arcana.blocks.be.CrystallizationPressBlockEntity;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;

import static arcana.Arcana.arcId;

public class CrystallizationPressScreen extends HandledScreen<CrystallizationPressScreenHandler>{
	
	private static final Identifier texture = arcId("textures/gui/container/crystallization_press.png");
	
	public CrystallizationPressScreen(CrystallizationPressScreenHandler handler, PlayerInventory inventory, Text title){
		super(handler, inventory, title);
	}
	
	protected void init(){
		super.init();
		titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2;
	}
	
	protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY){
		RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
		RenderSystem.setShaderColor(1, 1, 1, 1);
		RenderSystem.setShaderTexture(0, texture);
		drawTexture(matrices, x, y, 0, 0, backgroundWidth, backgroundHeight);
		
		if(handler.getQuartzAmount() > 0){
			int pixels = (int)Math.ceil(18 * handler.getQuartzAmount() / (double)CrystallizationPressBlockEntity.maxQuartzLevel);
			drawTexture(matrices, x + 44, y + 44 + (18 - pixels), 176, 18 - pixels, 4, pixels);
		}
		
		if(handler.getProgress() > 0){
			int pixels = (int)Math.ceil(22 * handler.getProgress() / (double)CrystallizationPressBlockEntity.maxProgress);
			drawTexture(matrices, x + 97, y + 33, 180, 0, pixels, 13);
		}
		
		if(handler.getEssentiaAmount() > 0){
			int pixels = (int)Math.ceil(52 * handler.getEssentiaAmount() / (double)CrystallizationPressBlockEntity.capacity);
			int colour = handler.getEssentiaColour();
			RenderSystem.setShaderColor(ColorHelper.Argb.getRed(colour) / 255f, ColorHelper.Argb.getGreen(colour) / 255f, ColorHelper.Argb.getBlue(colour) / 255f, 1f);
			drawTexture(matrices, x + 67, y + 13 + (52 - pixels), 202, 52 - pixels, 16, pixels);
		}
	}
	
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta){
		renderBackground(matrices);
		super.render(matrices, mouseX, mouseY, delta);
		drawMouseoverTooltip(matrices, mouseX, mouseY);
	}
	
	protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY){
		// no-op, no labels
	}
}