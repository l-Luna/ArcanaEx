package arcana.mixin;

import arcana.ArcanaRegistry;
import arcana.client.ArcanaClient;
import arcana.client.renderers.RunicShieldingRenderer;
import arcana.effects.PressureStatusEffect;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
import it.unimi.dsi.fastutil.booleans.BooleanArrayList;
import it.unimi.dsi.fastutil.booleans.BooleanList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.texture.Sprite;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin{
	
	@Shadow
	@Final
	private MinecraftClient client;
	@Unique
	private static final BooleanList suppressStack = new BooleanArrayList();
	
	@Inject(method = "renderHealthBar", at = @At("TAIL"))
	void renderRunicShieldingBars(DrawContext ctx, PlayerEntity player, int x, int y, int _lines, int regeneratingHeartIndex, float maxHealth, int lastHealth, int health, int absorption, boolean blinking, CallbackInfo ci){
		int lines = MathHelper.ceil((maxHealth + absorption) / 20f);
		y -= lines * 10;
		if(player.getArmor() > 0)
			y -= 10;
		RunicShieldingRenderer.renderShielding(ctx, x, y, player);
	}
	
	@WrapMethod(method = "renderHealthBar")
	void applyFrailEffect(DrawContext ctx, PlayerEntity player, int x, int y, int lines, int regeneratingHeartIndex, float maxHealth, int lastHealth, int health, int absorption, boolean blinking, Operation<Void> original){
		if(player.hasStatusEffect(RegistryEntry.of(ArcanaRegistry.WARP_FRAIL)))
			lastHealth = health = 0;
		original.call(ctx, player, x, y, lines, regeneratingHeartIndex, maxHealth, lastHealth, health, absorption, blinking);
	}
	
	@Inject(method = "method_18620", at = @At("TAIL"))
	private static void drawPressureOverlay(DrawContext ctx, float alpha, int x, int y, Sprite sprite, CallbackInfo ci){
		if(suppressStack.removeBoolean(0)){
			ctx.drawTexture(ArcanaClient.SUPPRESSED_EFFECT_TEX_PATH, x + 1, y + 1, 0, 0, 0, 22, 22, 22, 22);
		}
	}
	
	@Inject(method = "renderStatusEffectOverlay", at = @At(value = "HEAD"))
	void initPressureOverlay(DrawContext ctx, RenderTickCounter tickCounter, CallbackInfo ci){
		suppressStack.clear();
	}
	
	@Inject(method = "renderStatusEffectOverlay",
	        at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", shift = At.Shift.AFTER))
	void queuePressureOverlay(DrawContext ctx, RenderTickCounter tickCounter, CallbackInfo ci, @Local RegistryEntry<StatusEffect> effect){
		suppressStack.add(PressureStatusEffect.suppresses(client.player, effect.value()));
	}
}