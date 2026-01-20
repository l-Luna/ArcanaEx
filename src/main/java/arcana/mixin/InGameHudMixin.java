package arcana.mixin;

import arcana.ArcanaRegistry;
import arcana.client.ArcanaClient;
import arcana.client.renderers.RunicShieldingRenderer;
import arcana.effects.PressureStatusEffect;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.booleans.BooleanArrayList;
import it.unimi.dsi.fastutil.booleans.BooleanList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin extends DrawableHelper{
	
	@Shadow
	@Final
	private MinecraftClient client;
	@Unique
	private final BooleanList suppressStack = new BooleanArrayList();
	
	@Inject(method = "renderHealthBar", at = @At("TAIL"))
	void renderRunicShieldingBars(MatrixStack matrices, PlayerEntity player, int x, int y, int _lines, int regeneratingHeartIndex, float maxHealth, int lastHealth, int health, int absorption, boolean blinking, CallbackInfo ci){
		int lines = MathHelper.ceil((maxHealth + absorption) / 20f);
		y -= lines * 10;
		if(player.getArmor() > 0)
			y -= 10;
		RunicShieldingRenderer.renderShielding(matrices, x, y, player);
	}
	
	@WrapMethod(method = "renderHealthBar")
	void applyFrailEffect(MatrixStack matrices, PlayerEntity player, int x, int y, int lines, int regeneratingHeartIndex, float maxHealth, int lastHealth, int health, int absorption, boolean blinking, Operation<Void> original){
		if(player.hasStatusEffect(ArcanaRegistry.WARP_FRAIL))
			lastHealth = health = 0;
		original.call(matrices, player, x, y, lines, regeneratingHeartIndex, maxHealth, lastHealth, health, absorption, blinking);
	}
	
	@Inject(method = "method_18620", at = @At("TAIL"))
	void drawPressureOverlay(Sprite sprite, float opacity, MatrixStack matrices, int x, int y, CallbackInfo ci){
		if(suppressStack.removeBoolean(0)){
			RenderSystem.setShaderTexture(0, ArcanaClient.SUPPRESSED_EFFECT_TEX_PATH);
			DrawableHelper.drawTexture(matrices, x + 1, y + 1, getZOffset(), 0, 0, 22, 22, 22, 22);
		}
	}
	
	@Inject(method = "renderStatusEffectOverlay", at = @At(value = "HEAD"))
	void initPressureOverlay(MatrixStack matrices, CallbackInfo ci){
		suppressStack.clear();
	}
	
	@Inject(method = "renderStatusEffectOverlay",
	        at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", shift = At.Shift.AFTER))
	void queuePressureOverlay(MatrixStack matrices, CallbackInfo ci, @Local StatusEffect effect){
		suppressStack.add(PressureStatusEffect.suppresses(client.player, effect));
	}
}