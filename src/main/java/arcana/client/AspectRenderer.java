package arcana.client;

import arcana.aspects.Aspect;
import arcana.aspects.AspectStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.List;

public final class AspectRenderer{
	
	public static void renderAspectStack(AspectStack stack, MatrixStack matrices,  int x, int y, int z){
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
		// aspect sprite is "$modid:textures/aspects/$id"
		RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
		RenderSystem.setShaderColor(r, g, b, a);
		RenderSystem.setShaderTexture(0, texture(aspect));
		RenderHelper.drawTexture(matrices, x, y, z, 0, 0, 16, 16, 16, 16, r, g, b, a);
	}
	
	public static Identifier texture(Aspect aspect){
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
	
	public static void renderAspectTooltip(Aspect aspect, MatrixStack matrices, int x, int y){
		// i would like to give it a custom background colour, but...
		MinecraftClient.getInstance().currentScreen.renderTooltip(matrices, tooltips(aspect), x, y);
	}
	
	public static List<Text> tooltips(Aspect aspect){
		if(MinecraftClient.getInstance().options.advancedItemTooltips)
			return List.of(aspect.name(), Text.literal(aspect.id().toString()).formatted(Formatting.DARK_GRAY));
		else return List.of(aspect.name());
	}
}