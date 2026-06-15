package arcana.client.renderers;

import arcana.ArcanaRegistry;
import arcana.aspects.Aspect;
import arcana.aspects.Aspects;
import arcana.aspects.ScaledAspectMap;
import arcana.aura.AuraChunk;
import arcana.client.RenderHelper;
import arcana.items.WandItem;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.world.World;

import java.util.Map;

import static arcana.Arcana.arcId;

public final class HudRenderer{
	
	public static final Identifier fluxMeterFrame = arcId("textures/gui/hud/flux_meter_frame.png");
	public static final Identifier fluxMeterFilling = arcId("textures/gui/hud/flux_chaos.png");
	public static final Identifier wandAspects = arcId("textures/gui/hud/wand_aspects.png");
	
	private static final Map<Aspect, Vec2f> aspectPositions = Map.of(
			Aspects.AIR, new Vec2f(3, 11),
			Aspects.FIRE, new Vec2f(40, 11),
			Aspects.WATER, new Vec2f(3, 31),
			Aspects.EARTH, new Vec2f(40, 31),
			Aspects.ORDER, new Vec2f(21, 3),
			Aspects.ENTROPY, new Vec2f(21, 41)
	);
	
	public static void renderHud(DrawContext ctx, RenderTickCounter counter){
		MatrixStack matrices = ctx.getMatrices();
		float delta = counter.getTickDelta(true);
		PlayerEntity player = MinecraftClient.getInstance().player;
		if(player != null){
			World world = player.getWorld();
			ItemStack mainHand = player.getMainHandStack(),offHand = player.getOffHandStack();
			matrices.push();
			ItemStack wandStack;
			if((wandStack = mainHand).getItem() instanceof WandItem || (wandStack = offHand).getItem() instanceof WandItem){
				ScaledAspectMap aspectStacks = WandItem.aspectsFrom(wandStack);
				Identifier coreId = WandItem.coreFrom(wandStack).id();
				Identifier coreTexId = Identifier.of(coreId.getNamespace(), "textures/gui/hud/wand_bases/" + coreId.getPath() + ".png");
				
				// draw bg
				ctx.drawTexture(coreTexId, 8, 8, 0, 0, 0, 64, 64, 64, 64);
				
				RenderSystem.setShaderTexture(0, wandAspects);
				for(int i = 0; i < Aspects.primals.size(); i++){
					Aspect primal = Aspects.primals.get(i);
					float fullness = aspectStacks.get(primal) / (float)WandItem.capacity(wandStack);
					int pixels = (int)(14 * fullness);
					// if you have even 1 aspect, always draw at least one pixel of filling
					if(fullness > 0)
						pixels = Math.max(pixels, 1);
					
					int aspectX = (int)aspectPositions.get(primal).x, aspectY = (int)aspectPositions.get(primal).y;
					RenderHelper.drawTexture(ctx, 8 + aspectX, 8 + aspectY + 1 + (14 - pixels), 0, i * 16, 17 + (14 - pixels), 16, pixels, 128, 128, 1f, 1f, 1f, 1f);
					RenderHelper.drawTexture(ctx, 8 + aspectX, 8 + aspectY + 1 + (14 - pixels), 0, i * 16, 33 + (14 - pixels), 16, pixels, 128, 128, 1f, 1f, 1f, fullness);
					RenderHelper.drawTexture(ctx, 8 + aspectX, 8 + aspectY, 0, i * 16, 0, 16, 16, 128, 128, 1f, 1f, 1f, 1f);
				}
				
				ItemStack focusStack = WandItem.focusFrom(wandStack);
				if(!focusStack.isEmpty()){
					ctx.drawItem(focusStack, 8 + 21, 8 + 22);
					ctx.drawItemInSlot(MinecraftClient.getInstance().textRenderer, focusStack, 8 + 21, 8 + 22);
				}
				
				// for future HUD components
				matrices.translate(70, 0, 0);
			}
			
			if(mainHand.isOf(ArcanaRegistry.FLUX_METER) || offHand.isOf(ArcanaRegistry.FLUX_METER)){
				int frame = (int)((world.getTime() + delta) % 10);
				AuraChunk auraHere = AuraChunk.from(world, player.getBlockPos());
				float flux = auraHere != null ? auraHere.flux() : 0;
				int pixHeight = (int)Math.min(flux, 100);
				
				ctx.drawTexture(fluxMeterFilling, 8, 8 + (100 - pixHeight), 0, 0, 100 * frame, 32, pixHeight, 1024, 1024);
				
				// display the frame at top-left
				ctx.drawTexture(fluxMeterFrame, 0, 0, 0, 0, 48, 116);
				
				// if flux is over max, flash white
				if(flux > 100){
					int amount = (int)(Math.abs(((MathHelper.sin((world.getTime() + delta) / 3f)) / 3f)) * 255);
					int colour = 0x00ffffff | (amount << 24);
					ctx.fill(8, 8, 40, 108, colour);
				}
				
				// if shift is held, display the amount of flux "exactly"
				// rounded to 2dp
				if(Screen.hasShiftDown())
					ctx.drawText(MinecraftClient.getInstance().textRenderer, String.format("%.2f", flux), 47, 8 + (97 - pixHeight), -1, false);
			}
			matrices.pop();
		}
	}
}