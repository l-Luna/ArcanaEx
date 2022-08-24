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
		renderAspect(stack.type(), matrices, x, y, z);
		renderAspectStackOverlay(stack, matrices, text, x, y, z);
	}
	
	public static void renderAspect(Aspect aspect, MatrixStack matrices, int x, int y, int z){
		// aspect sprite is "$modid:textures/aspects/$name"
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1, 1, 1, 1);
		RenderSystem.setShaderTexture(0, texture(aspect));
		DrawableHelper.drawTexture(matrices, x, y, z, 0, 0, 16, 16, 16, 16);
	}
	
	public static Identifier texture(Aspect aspect){
		return new Identifier(aspect.name().getNamespace(), "textures/aspects/%s.png".formatted(aspect.name().getPath()));
	}
	
	public static void renderAspectStackOverlay(AspectStack stack, MatrixStack matricies, TextRenderer text, int x, int y, int z){
		matricies.translate(0, 0, z + 1);
		var label = String.valueOf(stack.amount());
		text.drawWithShadow(matricies, label, x + 17 - text.getWidth(label), y + 9, 0xFFFFFF);
	}
}