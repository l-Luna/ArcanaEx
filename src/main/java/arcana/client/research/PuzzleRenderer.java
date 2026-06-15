package arcana.client.research;

import arcana.client.research.puzzles.ChemistryPuzzleRenderer;
import arcana.research.Puzzle;
import arcana.research.puzzles.Chemistry;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public interface PuzzleRenderer<T extends Puzzle>{
	
	// registry
	
	Map<Identifier, PuzzleRenderer<?>> renderers = new HashMap<>();
	
	static void setup(){
		renderers.put(Chemistry.TYPE, new ChemistryPuzzleRenderer());
	}
	
	@SuppressWarnings("unchecked")
	static <T extends Puzzle> PuzzleRenderer<T> get(T puzzle){
		return (PuzzleRenderer<T>)renderers.get(puzzle.type());
	}
	
	//
	
	void render(DrawContext ctx, T puzzle, NbtCompound notesTag, int screenWidth, int screenHeight, int mouseX, int mouseY);
	
	boolean onClick(int button, T puzzle, NbtCompound notesTag, int screenWidth, int screenHeight, int mouseX, int mouseY);
	
	void renderAfter(DrawContext ctx, T puzzle, NbtCompound notesTag, int screenWidth, int screenHeight, int mouseX, int mouseY);
	
	void renderComplete(DrawContext ctx, T puzzle, NbtCompound notesTag, int screenWidth, int screenHeight, int mouseX, int mouseY);
	
	default void onClose(){}
	
	default boolean drawItemTooltips(){
		return true;
	}
}