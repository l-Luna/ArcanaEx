package arcana.client.research.requirements;

import arcana.client.RenderHelper;
import arcana.client.research.RequirementRenderer;
import arcana.research.requirements.XpRequirement;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

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
		float red = (float)((Math.sin(halfTime) + 1) * .5f);
		float blue = (float)((Math.sin((float)(halfTime + (Math.PI * 4 / 3f))) + 1) * .1f);
		stack.push();
		RenderSystem.setShaderTexture(0, experienceOrbTexture);
		RenderSystem.enableBlend();
		RenderHelper.drawTexture(stack, x, y, 100, u, v, 16, 16, 64, 64, red, 1, blue, 1);
		stack.pop();
	}
}