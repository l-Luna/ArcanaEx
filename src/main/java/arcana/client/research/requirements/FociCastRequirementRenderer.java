package arcana.client.research.requirements;

import arcana.cca_components.Researcher;
import arcana.client.research.RequirementRenderer;
import arcana.research.requirements.FociCastRequirement;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

import static arcana.Arcana.arcId;

public class FociCastRequirementRenderer implements RequirementRenderer<FociCastRequirement>{
	
	public static final Identifier FOCI_CAST_TEX = arcId("textures/research/foci_cast.png");
	
	public void render(DrawContext ctx, int x, int y, FociCastRequirement requirement, int time, float delta){
		ctx.drawTexture(FOCI_CAST_TEX, x, y, 101, 0, 0, 16, 16, 16, 16);
	}
	
	public List<? extends Text> tooltip(FociCastRequirement requirement, int time){
		int done = Researcher.from(client().player).getCastFociCount();
		return List.of(
				Text.translatable("research.requirement.arcana.foci_cast", requirement.getAmount()),
				Text.translatable("research.requirement.arcana.foci_cast.progress", done, requirement.getAmount())
		);
	}
}