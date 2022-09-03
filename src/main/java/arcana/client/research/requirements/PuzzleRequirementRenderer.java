package arcana.client.research.requirements;

import arcana.client.research.RequirementRenderer;
import arcana.research.Puzzle;
import arcana.research.Research;
import arcana.research.puzzles.Fieldwork;
import arcana.research.requirements.PuzzleRequirement;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

import static arcana.Arcana.arcId;

public class PuzzleRequirementRenderer implements RequirementRenderer<PuzzleRequirement>{
	
	public static final Identifier FIELDWORK_TEX = arcId("textures/gui/research/fieldwork.png");
	public static final Identifier RESEARCH_NOTE_TEX = arcId("textures/gui/research/research_note.png");
	
	public void render(MatrixStack matrices, int x, int y, PuzzleRequirement requirement, int time, float delta){
		Puzzle puzzle = Research.getPuzzle(requirement.getPuzzleId());
		RenderSystem.setShaderTexture(0, puzzle instanceof Fieldwork ? FIELDWORK_TEX : RESEARCH_NOTE_TEX);
		DrawableHelper.drawTexture(matrices, x, y, 101, 0, 0, 16, 16, 16, 16);
	}
	
	public List<Text> tooltip(PuzzleRequirement requirement, int time){
		Puzzle puzzle = Research.getPuzzle(requirement.getPuzzleId());
		List<Text> ret = new ArrayList<>(3);
		if(I18n.hasTranslation(puzzle.desc()))
			ret.add(Text.translatable(puzzle.desc()));
		else{
			var t = puzzle.type();
			ret.add(Text.translatable("research." + t.getNamespace() + "." + t.getPath().replace("/", ".")));
		}
		if(!(puzzle instanceof Fieldwork)){
			ret.add(Text.translatable("research.entry.get_note.1").formatted(Formatting.AQUA));
			Formatting color = canGetNote() ? Formatting.GRAY : Formatting.RED;
			ret.add(Text.translatable("research.entry.get_note.2").formatted(color));
		}
		return ret;
	}
	
	private static boolean canGetNote(){
		return true; // TODO: check for scribing tools & paper
	}
}