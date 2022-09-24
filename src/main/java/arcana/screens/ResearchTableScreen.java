package arcana.screens;

import arcana.ArcanaRegistry;
import arcana.client.research.PuzzleRenderer;
import arcana.research.Puzzle;
import arcana.research.Research;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import static arcana.Arcana.arcId;

public class ResearchTableScreen extends HandledScreen<ResearchTableScreenHandler>{
	
	private static final Identifier texture = arcId("textures/gui/container/research_table.png");
	
	public static final int bgWidth = 338, bgHeight = 241;
	
	public ResearchTableScreen(ResearchTableScreenHandler handler, PlayerInventory inventory, Text title){
		super(handler, inventory, title);
	}
	
	protected void init(){
		backgroundWidth = 338;
		backgroundHeight = 241;
		super.init();
		titleY = -100;
	}
	
	protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY){
		RenderSystem.setShaderTexture(0, texture);
		drawRtTexture(matrices, x, y, 0, 0, 0, backgroundWidth, backgroundHeight);
	}
	
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta){
		renderBackground(matrices);
		super.render(matrices, mouseX, mouseY, delta);
		// don't draw item tooltips if e.g. an aspect is selected
		var notes = handler.slots.get(37).getStack();
		var nbt = notes.getNbt();
		if(!notes.isEmpty() && nbt != null && nbt.contains("puzzle_id")){
			Puzzle puzzle = Research.getPuzzle(new Identifier(nbt.getString("puzzle_id")));
			var renderer = PuzzleRenderer.get(puzzle);
			if(renderer == null || renderer.drawItemTooltips())
				drawMouseoverTooltip(matrices, mouseX, mouseY);
		}else
			drawMouseoverTooltip(matrices, mouseX, mouseY);
	}
	
	public static void drawRtTexture(MatrixStack matrices, int x, int y, int z, float u, float v, int width, int height){
		drawTexture(matrices, x, y, z, u, v, width, height, 338, 338);
	}
	
	protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY){
		// don't draw label
		// do draw selected aspect over items
		matrices.push();
		matrices.translate(-x, -y, 0);
		var notes = handler.slots.get(37).getStack();
		var nbt = notes.getNbt();
		if(!notes.isEmpty() && nbt != null && nbt.contains("puzzle_id")){
			Puzzle puzzle = Research.getPuzzle(new Identifier(nbt.getString("puzzle_id")));
			var renderer = PuzzleRenderer.get(puzzle);
			if(renderer != null){
				var data = nbt.getCompound("puzzle_data");
				if(notes.getItem() == ArcanaRegistry.RESEARCH_NOTES){
					renderer.render(matrices, puzzle, data, width, height, mouseX, mouseY);
					renderer.renderAfter(matrices, puzzle, data, width, height, mouseX, mouseY);
				}else if(notes.getItem() == ArcanaRegistry.COMPLETE_RESEARCH_NOTES)
					renderer.renderComplete(matrices, puzzle, data, width, height, mouseX, mouseY);
			}
		}
		matrices.pop();
	}
	
	public boolean mouseClicked(double mouseX, double mouseY, int button){
		super.mouseClicked(mouseX, mouseY, button);
		
		var notes = handler.slots.get(37).getStack();
		var nbt = notes.getNbt();
		if(!notes.isEmpty() && nbt != null && nbt.contains("puzzle_id") && notes.getItem() == ArcanaRegistry.RESEARCH_NOTES){
			Puzzle puzzle = Research.getPuzzle(new Identifier(nbt.getString("puzzle_id")));
			var renderer = PuzzleRenderer.get(puzzle);
			if(renderer != null)
				return renderer.onClick(button, puzzle, nbt.getCompound("puzzle_data"), width, height, (int)mouseX, (int)mouseY);
		}
		
		return false;
	}
	
	public void close(){
		var notes = handler.slots.get(37).getStack();
		var nbt = notes.getNbt();
		if(!notes.isEmpty() && nbt != null && nbt.contains("puzzle_id")){
			Puzzle puzzle = Research.getPuzzle(new Identifier(nbt.getString("puzzle_id")));
			var renderer = PuzzleRenderer.get(puzzle);
			if(renderer != null)
				renderer.onClose();
		}
		super.close();
	}
}