package arcana.client;

import arcana.aspects.Aspect;
import arcana.aspects.AspectMap;
import arcana.aspects.AspectStack;
import arcana.components.Researcher;
import arcana.research.BuiltinResearch;
import arcana.research.Research;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;

import java.util.ArrayList;
import java.util.List;

public final class AspectRenderer{
	
	public static void renderAspectStack(AspectStack stack, MatrixStack matrices, int x, int y, int z){
		renderAspectStack(stack, matrices, MinecraftClient.getInstance().textRenderer, x, y, z);
	}
	
	public static void renderAspectStack(AspectStack stack, MatrixStack matrices, TextRenderer text, int x, int y, int z){
		renderAspectStack(stack.type(), stack.amount(), matrices, text, false, x, y, z);
	}
	
	public static void renderAspectStack(Aspect aspect, int amount, MatrixStack matrices, TextRenderer text, boolean alwaysDrawLabel, int x, int y, int z){
		renderAspect(aspect, matrices, x, y, z, 1, 1, 1, 1);
		if(alwaysDrawLabel || amount > 1)
			renderAspectStackOverlay(amount, matrices, text, x, y, z);
	}
	
	public static void renderAspect(Aspect aspect, MatrixStack matrices, int x, int y, int z){
		renderAspect(aspect, matrices, x, y, z, 1, 1, 1, 1);
	}
	
	public static void renderAspect(Aspect aspect, MatrixStack matrices, int x, int y, int z, float r, float g, float b, float a){
		RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
		RenderSystem.setShaderColor(r, g, b, a);
		RenderSystem.setShaderTexture(0, texture(aspect));
		RenderHelper.drawTexture(matrices, x, y, z, 0, 0, 16, 16, 16, 16, r, g, b, a);
	}
	
	public static Identifier texture(Aspect aspect){
		// aspect sprite is "$modid:textures/aspects/$id"
		return new Identifier(aspect.id().getNamespace(), "textures/aspects/%s.png".formatted(aspect.id().getPath()));
	}
	
	public static void renderAspectStackOverlay(int amount, MatrixStack matrices, TextRenderer text, int x, int y, int z){
		renderAspectStackOverlay(amount, matrices, text, x, y, z, 0xFFFFFF);
	}
	
	public static void renderAspectStackOverlay(int amount, MatrixStack matrices, TextRenderer text, int x, int y, int z, int colour){
		matrices.push();
		matrices.translate(0, 0, z + 1);
		var label = String.valueOf(amount);
		text.drawWithShadow(matrices, label, x + 18 - text.getWidth(label), y + 9, colour);
		matrices.pop();
	}
	
	public static void renderAspectsInWorld(MatrixStack matrices, PlayerEntity player, AspectMap aspects, BlockPos pos, Vec3f offset){
		if(player == null)
			return;
		Vec3d playerPos = player.getLerpedPos(MinecraftClient.getInstance().getTickDelta());
		
		matrices.push();
		// apply centering before rotation
		matrices.translate(0.5, 0, 0.5);
		double diffX = pos.getX() - playerPos.getX() + 0.5, diffZ = pos.getZ() - playerPos.getZ() + 0.5;
		float angle = (float)Math.atan2(diffX, diffZ);
		matrices.multiply(Quaternion.fromEulerXyz(new Vec3f(0, angle, 0)));
		// but block-specific offset after
		matrices.translate(offset.getX(), offset.getY(), offset.getZ());
		
		List<AspectStack> stacks = aspects.asStacks();
		
		double sqrDist = playerPos.squaredDistanceTo(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5);
		if(sqrDist > 8 * 8){
			matrices.pop();
			return;
		}
		var alpha = (float)(1 - Math.sqrt(sqrDist) / 10);
		var intAlpha = (int)(Math.max(0, alpha * 255)) << 24;
		
		for(int i = 0, size = stacks.size(); i < size; i++){
			AspectStack stack = stacks.get(i);
			matrices.push();
			var scale = 24f;
			matrices.scale(1 / scale, 1 / scale, -1 / scale);
			matrices.translate(16 * (size / 2d - i), 0, 0);
			matrices.multiply(Quaternion.fromEulerXyz(0, 0, (float)Math.PI));
			RenderSystem.enableDepthTest();
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			AspectRenderer.renderAspect(stack.type(), matrices, 0, 0, 0, 1, 1, 1, alpha);
			AspectRenderer.renderAspectStackOverlay(stack.amount(), matrices, MinecraftClient.getInstance().textRenderer, 0, 0, 0, 0xFFFFFF | intAlpha);
			matrices.pop();
		}
		matrices.pop();
	}
	
	public static void renderAspectTooltip(Aspect aspect, MatrixStack matrices, int x, int y){
		MinecraftClient.getInstance().currentScreen.renderTooltipFromComponents(matrices, tooltips(aspect), x, y);
	}
	
	public static List<TooltipComponent> tooltips(Aspect aspect){
		List<TooltipComponent> ret = new ArrayList<>(4);
		ret.add(fromText(aspect.name()));
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