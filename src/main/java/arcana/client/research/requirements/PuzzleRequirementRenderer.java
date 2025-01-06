package arcana.client.research.requirements;

import arcana.client.research.RequirementRenderer;
import arcana.components.Researcher;
import arcana.network.PkGetNote;
import arcana.research.BuiltinResearch;
import arcana.research.Puzzle;
import arcana.research.Research;
import arcana.research.puzzles.Fieldwork;
import arcana.research.requirements.PuzzleRequirement;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;
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
	
	public List<? extends Text> tooltip(PuzzleRequirement requirement, int time){
		PlayerEntity player = client().player;
		var researcher = Researcher.from(player);
		Puzzle puzzle = Research.getPuzzle(requirement.getPuzzleId());
		// if research in general hasn't been unlocked & this isn't the tutorial puzzle or fieldwork, don't offer doing research
		if(!(researcher.isEntryComplete(Research.getEntry(BuiltinResearch.researchTutorialEntry))
				|| puzzle.id().equals(BuiltinResearch.researchTutorialPuzzle)
				|| puzzle instanceof Fieldwork))
			return List.of(Text.translatable("research.entry.cant_solve").formatted(Formatting.RED));
		// if they already have the puzzle completed, don't offer doing it again
		List<MutableText> ret = tooltipForPuzzle(puzzle);
		if(researcher.isPuzzleComplete(puzzle))
			ret.add(Text.translatable("research.entry.already_solved").formatted(Formatting.GREEN));
		// if they already have the note in their inventory, don't offer to give it to them again
		else if(PuzzleRequirement.alreadyHasNote(puzzle, player))
			ret.add(Text.translatable("research.entry.already_has_note").formatted(Formatting.GRAY));
		else if(!(puzzle instanceof Fieldwork)){
			ret.add(Text.translatable("research.entry.get_note.1").formatted(Formatting.AQUA));
			Formatting color = PkGetNote.canGetNote(player) ? Formatting.GRAY : Formatting.RED;
			ret.add(Text.translatable("research.entry.get_note.2").formatted(color));
		}
		return ret;
	}
	
	public static List<MutableText> tooltipForPuzzle(Puzzle puzzle){
		List<MutableText> ret = new ArrayList<>(3);
		if(I18n.hasTranslation(puzzle.desc()))
			ret.add(Text.translatable(puzzle.desc()));
		else{
			var t = puzzle.type();
			ret.add(Text.translatable("puzzle." + t.getNamespace() + "." + t.getPath().replace("/", ".")));
		}
		return ret;
	}
}