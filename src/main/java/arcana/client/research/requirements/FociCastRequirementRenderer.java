package arcana.client.research.requirements;

import arcana.client.research.RequirementRenderer;
import arcana.components.Researcher;
import arcana.research.requirements.FociCastRequirement;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

import static arcana.Arcana.arcId;

public class FociCastRequirementRenderer implements RequirementRenderer<FociCastRequirement>{
	
	public static final Identifier FOCI_CAST_TEX = arcId("textures/research/foci_cast.png");
	
	public void render(MatrixStack matrices, int x, int y, FociCastRequirement requirement, int time, float delta){
		RenderSystem.setShaderTexture(0, FOCI_CAST_TEX);
		DrawableHelper.drawTexture(matrices, x, y, 101, 0, 0, 16, 16, 16, 16);
	}
	
	public List<? extends Text> tooltip(FociCastRequirement requirement, int time){
		int done = Researcher.from(client().player).getCastFociCount();
		return List.of(
				Text.translatable("research.requirement.arcana.foci_cast", requirement.getAmount()),
				Text.translatable("research.requirement.arcana.foci_cast.progress", done, requirement.getAmount())
		);
	}
}