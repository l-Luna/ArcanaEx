package arcana.client.research.requirements;

import arcana.client.RenderHelper;
import arcana.client.research.RequirementRenderer;
import arcana.research.requirements.XpRequirement;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.List;

public class XpRequirementRenderer implements RequirementRenderer<XpRequirement>{
	
	private static final Identifier experienceOrbTexture = new Identifier("textures/entity/experience_orb.png");
	
	public void render(MatrixStack matrices, int x, int y, XpRequirement requirement, int time, float delta){
		doXPRender(matrices, time, x, y, delta);
	}
	
	public List<Text> tooltip(XpRequirement requirement, int time){
		return List.of(Text.translatable("research.requirement.arcana.experience", requirement.getAmount()));
	}
	
	public static void doXPRender(MatrixStack stack, int time, int x, int y, float delta){
		int u = 0, v = 16;
		float halfTime = (time + delta) / 2f;
		float red = (MathHelper.sin(halfTime) + 1.0F) * .5f;
		float blue = (MathHelper.sin(halfTime + 4.1887903f) + 1) * .1f;
		stack.push();
		RenderSystem.setShaderTexture(0, experienceOrbTexture);
		RenderSystem.enableBlend();
		RenderHelper.drawTexture(stack, x, y, 100, u, v, 16, 16, 64, 64, red, 1, blue, 1);
		stack.pop();
	}
}