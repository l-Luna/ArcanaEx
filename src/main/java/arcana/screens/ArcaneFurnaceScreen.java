package arcana.screens;

import arcana.blocks.be.ArcaneFurnaceBlockEntity;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper.Argb;

import static arcana.Arcana.arcId;

public class ArcaneFurnaceScreen extends HandledScreen<ArcaneFurnaceScreenHandler>{
	
	private static final Identifier texture = arcId("textures/gui/container/arcane_furnace.png");
	
	public ArcaneFurnaceScreen(ArcaneFurnaceScreenHandler handler, PlayerInventory inventory, Text title){
		super(handler, inventory, title);
	}
	
	protected void init(){
		backgroundHeight = 163;
		super.init();
		titleY = (backgroundWidth - textRenderer.getWidth(title)) / 2;
	}
	
	protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY){
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1, 1, 1, 1);
		RenderSystem.setShaderTexture(0, texture);
		drawTexture(matrices, x, y, 0, 0, backgroundWidth, backgroundHeight);
		
		if(handler.getBurnTime() > 0 && handler.getMaxBurnTime() > 0){
			int pixels = (int)Math.ceil(14 * handler.getBurnTime() / (double)handler.getMaxBurnTime());
			drawTexture(matrices, x + 33, y + 30 + (14 - pixels), 176, 14 - pixels, 13, pixels);
		}
		
		if(handler.getSubstrateAmount() > 0 && handler.getMaxSubstrateAmount() > 0){
			int pixels = (int)Math.ceil(12 * handler.getSubstrateAmount() / (double)handler.getMaxSubstrateAmount());
			int colour = handler.getSubstrateColour();
			RenderSystem.setShaderColor(Argb.getRed(colour) / 255f, Argb.getGreen(colour) / 255f, Argb.getBlue(colour) / 255f, 1f);
			drawTexture(matrices, x + 57, y + 32 + (12 - pixels), 202, 12 - pixels, 13, pixels);
		}
		RenderSystem.setShaderColor(1, 1, 1, 1);
		
		if(handler.getProgres() > 0 && handler.getMaxProgress() > 0){
			int pixels = (int)Math.ceil(20 * handler.getProgres() / (double)handler.getMaxProgress());
			drawTexture(matrices, x + 79, y + 30, 215, 0, pixels, 13);
		}
		
		if(handler.getAspectTotal() > 0){
			int pixels = (int)Math.ceil(52 * handler.getAspectTotal() / (double)ArcaneFurnaceBlockEntity.capacity);
			drawTexture(matrices, x + 142, y + 11 + (52 - pixels), 176, 14 + (52 - pixels), 16, pixels);
		}
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