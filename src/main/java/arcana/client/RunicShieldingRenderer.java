package arcana.client;

import arcana.components.RunicShielding;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import static arcana.Arcana.arcId;

public final class RunicShieldingRenderer{
	
	public static final Identifier iconsTexture = arcId("gui/hud/icons");
	private static final Identifier iconsTexturePath = arcId("textures/gui/hud/icons.png");
	
	public static final Identifier overlayTexture = arcId("gui/hud/runic_shielding_overlay");
	private static final Identifier overlayTexturePath = arcId("textures/gui/hud/runic_shielding_overlay.png");
	
	// y position is adjusted in InGameHudMixin
	public static void renderShielding(MatrixStack matrices, int x, int y, PlayerEntity player){
		MinecraftClient client = MinecraftClient.getInstance();
		World w = client.world;
		
		client.getProfiler().push("arcana:runic_shielding");
		RenderSystem.setShaderColor(1, 1, 1, 1);
		RenderSystem.setShaderTexture(0, iconsTexturePath);
		
		RunicShielding shielding = RunicShielding.from(player);
		int halfPoints = shielding.getHalfPoints();
		boolean justHealed = Math.abs(w.getTime() - shielding.getLastRechargeTime()) < 3;
		boolean justHurt = Math.abs(w.getTime() - shielding.getLastActivateTime()) < 3;
		boolean flash = justHealed || justHurt;
		int maxShielding = RunicShielding.getMaxShielding(player);
		for(int i = 0; i < maxShielding; i++){
			int bounce = 0;
			if(justHealed && (halfPoints - 1) / 2 == i)
				bounce = -1;
			if(justHurt && halfPoints / 2 == i)
				bounce = 1;
			int n = (i+1)*2;
			int u = 0;
			int bgV = flash ? 27 : 0;
			DrawableHelper.drawTexture(matrices, x + 8 * i, y + bounce, 0, u, bgV, 9, 9, 128, 128);
			int fgV = n-1 == halfPoints ? 9 : n <= halfPoints ? 18 : 36;
			DrawableHelper.drawTexture(matrices, x + 8 * i, y + bounce, 0, u, fgV, 9, 9, 128, 128);
		}
		
		RenderSystem.setShaderTexture(0, InGameHud.GUI_ICONS_TEXTURE);
		client.getProfiler().pop();
	}
	
	public static void renderOverlay(MatrixStack matrices, float tickDelta){
		MinecraftClient client = MinecraftClient.getInstance();
		PlayerEntity player = client.player;
		if(player == null)
			return;
		
		RunicShielding shielding = RunicShielding.from(player);
		long timeSinceActivation = player.world.getTime() - shielding.getLastActivateTime();
		if(timeSinceActivation < 13){
			int scaledWidth = client.getWindow().getScaledWidth();
			int scaledHeight = client.getWindow().getScaledHeight();
			float opacity = Math.max(1- (timeSinceActivation + Math.min(tickDelta, 1)) / 13, 0.1f);
			RenderSystem.setShaderColor(1, 1, 1, 1);
			RenderSystem.setShaderTexture(0, overlayTexturePath);
			// use scaled width/height as texture height to stretch texture
			RenderHelper.drawTexture(matrices, 0, 0, 0, 0, 0, scaledWidth, scaledHeight, scaledWidth, scaledHeight, 1, 1, 1, opacity);
		}
	}
}