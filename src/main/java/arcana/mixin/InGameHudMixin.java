package arcana.mixin;

import arcana.client.RunicShieldingRenderer;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin{
	
	@Inject(method = "renderHealthBar", at = @At("TAIL"))
	void renderRunicShieldingBars(MatrixStack matrices, PlayerEntity player, int x, int y, int _lines, int regeneratingHeartIndex, float maxHealth, int lastHealth, int health, int absorption, boolean blinking, CallbackInfo ci){
		int lines = MathHelper.ceil((maxHealth + absorption) / 20f);
		y -= lines * 10;
		if(player.getArmor() > 0)
			y -= 10;
		RunicShieldingRenderer.renderShielding(matrices, x, y, player);
	}
}