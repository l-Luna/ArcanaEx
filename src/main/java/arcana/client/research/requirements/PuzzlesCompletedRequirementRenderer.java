package arcana.client.research.requirements;

import arcana.client.research.RequirementRenderer;
import arcana.components.Researcher;
import arcana.research.requirements.PuzzlesCompletedRequirement;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.util.List;

public class PuzzlesCompletedRequirementRenderer implements RequirementRenderer<PuzzlesCompletedRequirement>{
	
	public void render(MatrixStack matrices, int x, int y, PuzzlesCompletedRequirement requirement, int time, float delta){
		RenderSystem.setShaderTexture(0, PuzzleRequirementRenderer.RESEARCH_NOTE_TEX);
		DrawableHelper.drawTexture(matrices, x, y, 101, 0, 0, 16, 16, 16, 16);
	}
	
	public List<? extends Text> tooltip(PuzzlesCompletedRequirement requirement, int time){
		int done = Researcher.from(client().player).getCompletedPuzzleCount();
		return List.of(
				Text.translatable("research.requirement.arcana.puzzles_completed", requirement.getAmount()),
				Text.translatable("research.requirement.arcana.puzzles_completed.progress", done, requirement.getAmount())
		);
	}
}