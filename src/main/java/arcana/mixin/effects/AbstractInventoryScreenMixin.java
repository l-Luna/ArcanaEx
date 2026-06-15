package arcana.mixin.effects;

import arcana.client.ArcanaClient;
import arcana.effects.PressureStatusEffect;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractInventoryScreen.class)
public abstract class AbstractInventoryScreenMixin<T extends ScreenHandler> extends HandledScreen<T>{
	
	public AbstractInventoryScreenMixin(T handler, PlayerInventory inventory, Text title){
		super(handler, inventory, title);
	}
	
	@ModifyReturnValue(method = "getStatusEffectDescription", at = @At("TAIL"))
	Text getStatusEffectDescription(Text original, StatusEffectInstance effect){
		PlayerEntity player = MinecraftClient.getInstance().player;
		if(PressureStatusEffect.suppresses(player, effect.getEffectType().value()))
			return original.copy().formatted(Formatting.STRIKETHROUGH, Formatting.GRAY);
		return original;
	}
	
	@Inject(method = "drawStatusEffectSprites", at = @At("TAIL"))
	void drawStatusEffectSprites(DrawContext ctx, int x, int height, Iterable<StatusEffectInstance> effects, boolean wide, CallbackInfo ci){
		// copied and adjusted from the original
		int i = y;
		for(StatusEffectInstance effect : effects){
			if(PressureStatusEffect.suppresses(client.player, effect.getEffectType().value()))
				ctx.drawTexture(ArcanaClient.SUPPRESSED_EFFECT_TEX_PATH, x + (wide ? 6 : 7) - 2, i + 7 - 2, 0, 0, 0, 22, 22, 22, 22);
			i += height;
		}
	}
}