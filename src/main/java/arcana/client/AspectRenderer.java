package arcana.client;

import arcana.aspects.Aspect;
import arcana.aspects.AspectStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public final class AspectRenderer{

	public static void renderAspectStack(AspectStack stack, MatrixStack matrices, TextRenderer text, int x, int y, int z){
		renderAspectStack(stack.type(), stack.amount(), matrices, text, false, x, y, z);
	}
	
	public static void renderAspectStack(Aspect aspect, int amount, MatrixStack matrices, TextRenderer text, boolean alwaysDrawLabel, int x, int y, int z){
		renderAspect(aspect, matrices, x, y, z);
		if(alwaysDrawLabel || amount > 1)
			renderAspectStackOverlay(amount, matrices, text, x, y, z);
	}
	
	public static void renderAspect(Aspect aspect, MatrixStack matrices, int x, int y, int z){
		// aspect sprite is "$modid:textures/aspects/$id"
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1, 1, 1, 1);
		RenderSystem.setShaderTexture(0, texture(aspect));
		DrawableHelper.drawTexture(matrices, x, y, z, 0, 0, 16, 16, 16, 16);
	}
	
	public static Identifier texture(Aspect aspect){
		return new Identifier(aspect.id().getNamespace(), "textures/aspects/%s.png".formatted(aspect.id().getPath()));
	}
	
	public static void renderAspectStackOverlay(int amount, MatrixStack matrices, TextRenderer text, int x, int y, int z){
		matrices.push();
		matrices.translate(0, 0, z + 1);
		var label = String.valueOf(amount);
		text.drawWithShadow(matrices, label, x + 18 - text.getWidth(label), y + 9, 0xFFFFFF);
		matrices.pop();
	}
}