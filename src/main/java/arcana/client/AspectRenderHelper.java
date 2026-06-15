package arcana.client;

import arcana.aspects.Aspect;
import arcana.aspects.AspectMap;
import arcana.aspects.AspectStack;
import arcana.cca_components.Researcher;
import arcana.client.tooltip.ItemAspectsTooltipComponent;
import arcana.client.tooltip.PinkMarkerComponent;
import arcana.research.BuiltinResearch;
import arcana.research.Research;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.HoveredTooltipPositioner;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public final class AspectRenderHelper{
	
	public static void renderAspectStack(AspectStack stack, DrawContext ctx, int x, int y, int z){
		renderAspectStack(stack, ctx, MinecraftClient.getInstance().textRenderer, x, y, z);
	}
	
	public static void renderAspectStack(AspectStack stack, DrawContext ctx, TextRenderer text, int x, int y, int z){
		renderAspectStack(stack.type(), stack.amount(), ctx, text, false, x, y, z);
	}
	
	public static void renderAspectStack(Aspect aspect, float amount, DrawContext ctx, TextRenderer text, boolean alwaysDrawLabel, int x, int y, int z){
		renderAspect(aspect, ctx, x, y, z, 1, 1, 1, 1);
		if(alwaysDrawLabel || amount > 1)
			renderAspectStackOverlay(amount, ctx, text, x, y, z);
	}
	
	public static void renderAspect(Aspect aspect, DrawContext ctx, int x, int y, int z){
		renderAspect(aspect, ctx, x, y, z, 1, 1, 1, 1);
	}
	
	public static void renderAspect(Aspect aspect, DrawContext ctx, int x, int y, int z, float r, float g, float b, float a){
		RenderSystem.setShaderColor(1, 1, 1, 1);
		RenderSystem.setShaderTexture(0, texture(aspect));
		RenderHelper.drawTexture(ctx, x, y, z, 0, 0, 16, 16, 16, 16, r, g, b, a);
	}
	
	public static Identifier texture(Aspect aspect){
		return Identifier.of(aspect.id().getNamespace(), "textures/aspects/%s.png".formatted(aspect.id().getPath()));
	}
	
	public static void renderAspectStackOverlay(float amount, DrawContext ctx, TextRenderer text, int x, int y, int z){
		renderAspectStackOverlay(amount, ctx, text, x, y, z, 0xFFFFFFFF);
	}
	
	public static void renderAspectStackOverlay(float amount, DrawContext ctx, TextRenderer text, int x, int y, int z, int colour){
		ctx.getMatrices().push();
		ctx.getMatrices().translate(0, 0, z + 1);
		String label = amount != (int)amount ? String.format("%.1f", amount) : String.valueOf((int)amount);
		if(amount < 100)
			ctx.drawTextWithShadow(text, label, x + 18 - text.getWidth(label), y + 9, colour);
		else
			RenderHelper.drawTinyNumbers(ctx,
					label,
					x + 23 - label.length() * 5,
					y + 19,
					ColorHelper.Argb.getRed(colour) / 255f,
					ColorHelper.Argb.getGreen(colour) / 255f,
					ColorHelper.Argb.getBlue(colour) / 255f,
					ColorHelper.Argb.getAlpha(colour) / 255f
			);
		ctx.getMatrices().pop();
	}
	
	public static void renderAspectsInWorld(MatrixStack matrices, PlayerEntity player, AspectMap aspects, BlockPos pos, Vector3f offset){
		if(player == null)
			return;
		Vec3d playerPos = player.getLerpedPos(MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(true));
		
		matrices.push();
		// apply centering before rotation
		matrices.translate(0.5, 0, 0.5);
		double diffX = pos.getX() - playerPos.getX() + 0.5, diffZ = pos.getZ() - playerPos.getZ() + 0.5;
		float angle = (float)Math.atan2(diffX, diffZ);
		matrices.multiply(RotationAxis.POSITIVE_Y.rotation(angle));
		// but block-specific offset after
		matrices.translate(offset.x(), offset.y(), offset.z());
		
		List<AspectStack> stacks = aspects.asStacks();
		
		double sqrDist = playerPos.squaredDistanceTo(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5);
		if(sqrDist > 9.5 * 9.5){
			matrices.pop();
			return;
		}
		var alpha = (float)(1 - Math.sqrt(sqrDist) / 10);
		var intAlpha = (int)(Math.max(0, alpha * 255)) << 24;
		
		final int wrap = 5;
		int size = stacks.size();
		int width = Math.min(wrap, size);
		for(int i = 0; i < size; i++){
			AspectStack stack = stacks.get(i);
			matrices.push();
			var scale = 24f;
			matrices.scale(1 / scale, 1 / scale, -1 / scale);
			matrices.translate(16 * (width / 2d - (i % wrap)), 16 * (i / wrap), 0);
			matrices.multiply(RotationAxis.POSITIVE_Z.rotation(MathHelper.PI));
			RenderSystem.enableDepthTest();
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			AspectRenderHelper.renderAspect(stack.type(), matrices, 0, 0, 0, 1, 1, 1, alpha);
			AspectRenderHelper.renderAspectStackOverlay(stack.amount(), matrices, MinecraftClient.getInstance().textRenderer, 0, 0, 0, 0xFFFFFF | intAlpha);
			matrices.pop();
		}
		matrices.pop();
	}
	
	public static void renderAspectTooltip(Aspect aspect, DrawContext ctx, int x, int y){
		ctx.drawTooltip(MinecraftClient.getInstance().textRenderer, tooltips(aspect), x, y, HoveredTooltipPositioner.INSTANCE);
	}
	
	public static List<TooltipComponent> tooltips(Aspect aspect){
		List<TooltipComponent> ret = new ArrayList<>(4);
		ret.add(fromText(aspect.name()));
		ret.add(fromText(aspect.desc()));
		ret.add(new PinkMarkerComponent());
		
		if(MinecraftClient.getInstance().options.advancedItemTooltips)
			ret.add(fromText(Text.literal(aspect.id().toString()).formatted(Formatting.DARK_GRAY)));
		
		Researcher researcher = Researcher.from(MinecraftClient.getInstance().player);
		if(researcher.isEntryComplete(Research.getEntry(BuiltinResearch.researchExpertiseEntry))
				&& Screen.hasShiftDown()
				&& aspect.left() != null && aspect.right() != null){
			ret.add(new ItemAspectsTooltipComponent(List.of(new AspectStack(aspect.left(), 1), new AspectStack(aspect.right(), 1)), null));
		}
		
		return ret;
	}
	
	private static TooltipComponent fromText(Text t){
		return TooltipComponent.of(t.asOrderedText());
	}
}