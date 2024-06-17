package arcana.client;

import arcana.ArcanaRegistry;
import arcana.components.AuraChunk;
import arcana.components.AuraWorld;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import static arcana.Arcana.arcId;

public class HudRenderer{
	
	public static final Identifier fluxMeterFrame = arcId("textures/gui/hud/flux_meter_frame.png");
	public static final Identifier fluxMeterFilling = arcId("textures/gui/hud/flux_chaos.png");
	
	public static void renderHud(MatrixStack matrices, float delta){
		PlayerEntity player = MinecraftClient.getInstance().player;
		if(player != null){
			World world = player.world;
			if(player.getMainHandStack().isOf(ArcanaRegistry.FLUX_METER) || player.getOffHandStack().isOf(ArcanaRegistry.FLUX_METER)){
				int frame = (int)((world.getTime() + delta) % 10);
				float flux = AuraWorld.from(world).getChunk(player.getBlockPos()).map(AuraChunk::getFlux).orElse(0f);
				int pixHeight = (int)Math.min(flux, 100);
				
				RenderSystem.setShader(GameRenderer::getPositionTexShader);
				RenderSystem.setShaderColor(1, 1, 1, 1);
				RenderSystem.setShaderTexture(0, fluxMeterFilling);
				RenderHelper.drawTexture(matrices, 8, 8 + (100 - pixHeight), 0, 0, 100 * frame, 32, pixHeight, 1024, 1024, 1, 1, 1, 1);
				
				// display the frame at top-left
				RenderSystem.setShaderTexture(0, fluxMeterFrame);
				RenderHelper.drawTexture(matrices, 0, 0, 0, 0f, 0f, 48, 116, 1f, 1f, 1f);
				
				// if flux is over max, flash white
				if(flux > 100){
					int amount = (int)(Math.abs(((MathHelper.sin((world.getTime() + delta) / 3f)) / 3f)) * 255);
					int colour = 0x00ffffff | (amount << 24);
					DrawableHelper.fill(matrices, 8, 8, 40, 108, colour);
				}
				
				// if shift is held, display the amount of flux "exactly"
				// rounded to 2dp
				if(Screen.hasShiftDown())
					MinecraftClient.getInstance().textRenderer.drawWithShadow(matrices, String.format("%.2f", flux), 47, 8 + (97 - pixHeight), -1);
			}
		}
	}
}