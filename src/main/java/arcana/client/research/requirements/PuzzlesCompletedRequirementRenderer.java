package arcana.client.research.requirements;

import arcana.client.research.RequirementRenderer;
import arcana.legacy_components.Researcher;
import arcana.research.requirements.PuzzlesCompletedRequirement;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

import static arcana.Arcana.arcId;

public class PuzzlesCompletedRequirementRenderer implements RequirementRenderer<PuzzlesCompletedRequirement>{
	
	public static final Identifier COMPLETE_RESEARCH_NOTE_TEX = arcId("textures/gui/research/complete_research_notes.png");
	
	public void render(DrawContext ctx, int x, int y, PuzzlesCompletedRequirement requirement, int time, float delta){
		ctx.drawTexture(COMPLETE_RESEARCH_NOTE_TEX, x, y, 101, 0, 0, 16, 16, 16, 16);
	}
	
	public List<? extends Text> tooltip(PuzzlesCompletedRequirement requirement, int time){
		int done = Researcher.from(client().player).getCompletedVisiblePuzzleCount();
		return List.of(
				Text.translatable("research.requirement.arcana.puzzles_completed", requirement.getAmount()),
				Text.translatable("research.requirement.arcana.puzzles_completed.progress", done, requirement.getAmount())
		);
	}
}