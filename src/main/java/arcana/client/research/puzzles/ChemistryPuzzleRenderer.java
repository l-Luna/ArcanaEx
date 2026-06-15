package arcana.client.research.puzzles;

import arcana.aspects.Aspect;
import arcana.aspects.AspectMap;
import arcana.aspects.AspectStack;
import arcana.aspects.Aspects;
import arcana.client.AspectRenderHelper;
import arcana.client.research.PuzzleRenderer;
import arcana.network.PkChemistryClick;
import arcana.network.PkChemistryCombineAspects;
import arcana.research.puzzles.Chemistry;
import arcana.research.puzzles.Chemistry.HexOffset;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static arcana.Arcana.arcId;
import static arcana.research.puzzles.Chemistry.processHexes;
import static arcana.screens.ResearchTableScreen.bgHeight;
import static arcana.screens.ResearchTableScreen.bgWidth;

public class ChemistryPuzzleRenderer implements PuzzleRenderer<Chemistry>{
	
	private static final Identifier overlayTex = arcId("textures/research/chemistry_overlay.png");
	// not stored on tag
	private static Aspect selected = null;
	private static Aspect combineLeft, combineRight;
	// TODO: paginate aspect display
	
	public void render(DrawContext ctx, Chemistry puzzle, NbtCompound notesTag, int screenWidth, int screenHeight, int mouseX, int mouseY){
		int x = (screenWidth - bgWidth) / 2, y = (screenHeight - bgHeight) / 2;
		
		// draw stored aspects
		AspectMap aspects = AspectMap.fromNbt(notesTag.getCompound("stored_aspects"));
		List<AspectStack> stacks = aspects.asStacks();
		stacks.sort(Comparator.comparing(AspectStack::type));
		for(int i = 0; i < stacks.size(); i++){
			int xPos = x + 9 + (i % 6) * 17;
			int yPos = y + 33 + (i / 6) * 18;
			AspectRenderHelper.renderAspectStack(stacks.get(i), ctx, xPos, yPos, 0);
			if(within(mouseX, mouseY, xPos, yPos, 16))
				highlight(ctx, xPos, yPos, 16);
		}
		
		
		// draw bottom part of sidebar
		ctx.drawTexture(overlayTex, x + 9, y + 134, 0, 0, 192, 100, 26, 256, 256); // sidebar bg
		ctx.drawTexture(overlayTex, x + 34, y + 139, 0, 0, 218, 18, 20, 256, 256); // left combo slot
		ctx.drawTexture(overlayTex, x + 66, y + 139, 0, 0, 218, 18, 20, 256, 256); // right combo slot
		// combine button
		if(combineLeft == null || combineRight == null || Aspects.combined(combineLeft, combineRight).isEmpty())
			ctx.drawTexture(overlayTex, x + 55, y + 144, 0, 30, 179, 8, 10, 256, 256);
		else if(within(mouseX, mouseY, x + 55, y + 144, 8, 10))
			ctx.drawTexture(overlayTex, x + 55, y + 144, 0, 30, 159, 8, 10, 256, 256);
		else
			ctx.drawTexture(overlayTex, x + 55, y + 144, 0, 30, 169, 8, 10, 256, 256);
		// combo slot aspects and highlights
		if(combineLeft != null)
			AspectRenderHelper.renderAspect(combineLeft, ctx, x + 35, y + 141, 0);
		if(combineRight != null)
			AspectRenderHelper.renderAspect(combineRight, ctx, x + 67, y + 141, 0);
		if(within(mouseX, mouseY, x + 35, y + 141, 16))
			highlight(ctx, x + 35, y + 141, 16);
		if(within(mouseX, mouseY, x + 67, y + 141, 16))
			highlight(ctx, x + 67, y + 141, 16);
		
		// draw paper & hex grid
		ctx.drawTexture(overlayTex, x + 119, y + 19, 0, 0, 25, 198, 134, 256, 256);
		
		// hexes have 3 pixels of vertical overlap and 2 pixels of horizontal spacing
		int size = puzzle.getSize();
		int edgeHexes = (size - 1) * 6;
		int nodeGap = edgeHexes / puzzle.getNodes().size();
		
		NbtCompound gridTag = notesTag.getCompound("grid_aspects");
		Map<HexOffset, Aspect> grid = new HashMap<>(gridTag.getKeys().size() + puzzle.getNodes().size());
		// build grid & draw aspects
		processHexes(size, x, y, (xPos, yPos, turn, rx, ry) -> {
			if(turn % nodeGap == 0){
				Aspect node = puzzle.getNodes().get(turn / nodeGap);
				AspectRenderHelper.renderAspect(node, ctx, xPos + 2, yPos + 2, 1);
				grid.put(new HexOffset(rx, ry), node);
				return false;
			}
			
			if(puzzle.excludedByFlux(rx, ry))
				return false;
			
			ctx.drawTexture(overlayTex, xPos, yPos, 0, 0, 0, 20, 20, 256, 256);
			
			String hexId = rx + "," + ry;
			if(gridTag.contains(hexId)){
				Aspect aspect = Aspects.byName(gridTag.getString(hexId));
				AspectRenderHelper.renderAspect(aspect, ctx, xPos + 2, yPos + 2, 0);
				grid.put(new HexOffset(rx, ry), aspect);
			}
			
			return false; // keep processing
		});
		
		// draw connections
		processHexes(size, x, y, (xPos, yPos, turn, rx, ry) -> {
			HexOffset pos = new HexOffset(rx, ry);
			Aspect self = grid.get(pos);
			if(self != null)
				for(HexOffset offset : Chemistry.neighborsByRow(ry)){
					HexOffset neighborPos = pos.add(offset);
					Aspect neighbor = grid.get(neighborPos);
					if(neighbor != null && (self.equals(neighbor.left()) || self.equals(neighbor.right())))
						switch(offset.turn()){
							case 0 -> ctx.drawTexture(overlayTex, xPos + 17, yPos + 8, 0, 42, 11, 8, 5, 256, 256);
							case 1 -> ctx.drawTexture(overlayTex, xPos + 12, yPos + 15, 0, 37, 18, 7, 7, 256, 256);
							case 2 -> ctx.drawTexture(overlayTex, xPos + 1, yPos + 15, 0, 26, 18, 7, 7, 256, 256);
							case 3 -> ctx.drawTexture(overlayTex, xPos - 5, yPos + 8, 0, 20, 11, 8, 5, 256, 256);
							case 4 -> ctx.drawTexture(overlayTex, xPos + 1, yPos - 3, 0, 26, 0, 7, 7, 256, 256);
							case 5 -> ctx.drawTexture(overlayTex, xPos + 12, yPos - 3, 0, 37, 0, 7, 7, 256, 256);
						}
				}
			
			return false;
		});
		
		// draw highlights over everything else
		processHexes(size, x, y, (xPos, yPos, turn, rx, ry) -> {
			if(turn % nodeGap != 0)
				if(within(mouseX, mouseY, xPos + 1, yPos + 2, 18, 16) && !puzzle.excludedByFlux(rx, ry))
					highlight(ctx, xPos + 1, yPos + 2, 18, 16);
			return false;
		});
		
		if(selected != null)
			AspectRenderHelper.renderAspect(selected, ctx, mouseX, mouseY, 1000);
	}
	
	public boolean onClick(int button, Chemistry puzzle, NbtCompound notesTag, int screenWidth, int screenHeight, int mouseX, int mouseY){
		// select aspects
		int x = (screenWidth - bgWidth) / 2, y = (screenHeight - bgHeight) / 2;
		AspectMap aspects = AspectMap.fromNbt(notesTag.getCompound("stored_aspects"));
		List<AspectStack> stacks = aspects.asStacks();
		stacks.sort(Comparator.comparing(AspectStack::type));
		for(int i = 0; i < stacks.size(); i++){
			int xPos = x + 9 + (i % 6) * 17;
			int yPos = y + 33 + (i / 6) * 18;
			if(within(mouseX, mouseY, xPos, yPos, 16)){
				if(button == 0){
					selected = stacks.get(i).type();
					return true;
				}
			}
		}
		
		// bottom part of sidebar
		if(within(mouseX, mouseY, x + 35, y + 141, 16))
			if(button == 1)
				combineLeft = null;
			else if(button == 0 && selected != null)
				combineLeft = selected;
		
		if(within(mouseX, mouseY, x + 67, y + 141, 16))
			if(button == 1)
				combineRight = null;
			else if(button == 0 && selected != null)
				combineRight = selected;
		
		if(within(mouseX, mouseY, x + 55, y + 144, 8, 10) && button == 0)
			if(combineLeft != null && combineRight != null)
				if(Aspects.combined(combineLeft, combineRight).isPresent()){
					new PkChemistryCombineAspects(combineLeft, combineRight).sendToServer();
					if(aspects.get(combineLeft) == 1){
						if(selected == combineLeft) selected = null;
						if(combineRight == combineLeft) combineRight = null;
						combineLeft = null;
					}
					if(aspects.get(combineRight) == 1){
						if(selected == combineRight) selected = null;
						if(combineLeft == combineRight) combineLeft = null;
						combineRight = null;
					}
				}
		
		// right click to clear selection
		if(button == 1 && selected != null){
			selected = null;
			return true;
		}
		
		int size = puzzle.getSize();
		int nodeGap = (size - 1) * 6 / puzzle.getNodes().size();
		
		NbtCompound gridTag = notesTag.getCompound("grid_aspects");
		return processHexes(size, x, y, (xPos, yPos, turn, rx, ry) -> {
			if(turn % nodeGap == 0)
				return false;
			
			if(within(mouseX, mouseY, xPos + 1, yPos + 2, 18, 16)){
				String hexId = rx + "," + ry;
				if(selected != null && button == 0 && !gridTag.contains(hexId) && !puzzle.excludedByFlux(rx, ry)){
					new PkChemistryClick(hexId, selected).sendToServer();
					if(aspects.get(selected) == 1){
						if(combineLeft == selected) combineLeft = null;
						if(combineRight == selected) combineRight = null;
						selected = null; // used the last one
					}
					return true;
				}else if(selected == null && button == 1 && gridTag.contains(hexId)){
					new PkChemistryClick(hexId, selected).sendToServer();
					return true;
				}
			}
			
			return false;
		});
	}
	
	public void renderAfter(DrawContext ctx, Chemistry puzzle, NbtCompound notesTag, int screenWidth, int screenHeight, int mouseX, int mouseY){
		if(selected != null)
			return;
		
		int x = (screenWidth - bgWidth) / 2, y = (screenHeight - bgHeight) / 2;
		
		// draw stored aspects
		AspectMap aspects = AspectMap.fromNbt(notesTag.getCompound("stored_aspects"));
		List<AspectStack> stacks = aspects.asStacks();
		stacks.sort(Comparator.comparing(AspectStack::type));
		for(int i = 0; i < stacks.size(); i++){
			int xPos = x + 9 + (i % 6) * 17;
			int yPos = y + 33 + (i / 6) * 18;
			if(within(mouseX, mouseY, xPos, yPos, 16)){
				AspectRenderHelper.renderAspectTooltip(stacks.get(i).type(), ctx, mouseX, mouseY);
				return;
			}
		}
		
		// bottom of sidebar
		if(combineLeft != null && within(mouseX, mouseY, x + 35, y + 141, 16)){
			AspectRenderHelper.renderAspectTooltip(combineLeft, ctx, mouseX, mouseY);
			return;
		}
		
		if(combineRight != null && within(mouseX, mouseY, x + 67, y + 141, 16)){
			AspectRenderHelper.renderAspectTooltip(combineRight, ctx, mouseX, mouseY);
			return;
		}
		
		// grid tooltips
		int size = puzzle.getSize();
		int nodeGap = (size - 1) * 6 / puzzle.getNodes().size();
		NbtCompound gridTag = notesTag.getCompound("grid_aspects");
		processHexes(size, x, y, (xPos, yPos, turn, rx, ry) -> {
			if(turn % nodeGap == 0 && within(mouseX, mouseY, xPos + 1, yPos + 2, 18, 16)){
				Aspect node = puzzle.getNodes().get(turn / nodeGap);
				AspectRenderHelper.renderAspectTooltip(node, ctx, mouseX, mouseY);
				return true;
			}
			
			String hexId = rx + "," + ry;
			if(gridTag.contains(hexId) && within(mouseX, mouseY, xPos + 1, yPos + 2, 18, 16)){
				Aspect aspect = Aspects.byName(gridTag.getString(hexId));
				AspectRenderHelper.renderAspectTooltip(aspect, ctx, mouseX, mouseY);
				return true;
			}
			
			return false;
		});
	}
	
	public void renderComplete(DrawContext ctx, Chemistry puzzle, NbtCompound notesTag, int screenWidth, int screenHeight, int mouseX, int mouseY){
		// only draw the grid
		int x = (screenWidth - bgWidth) / 2, y = (screenHeight - bgHeight) / 2;
		ctx.drawTexture(overlayTex, x + 119, y + 19, 0, 0, 25, 198, 134, 256, 256);
		
		int size = puzzle.getSize();
		int nodeGap = (size - 1) * 6 / puzzle.getNodes().size();
		NbtCompound gridTag = notesTag.getCompound("grid_aspects");
		Map<HexOffset, Aspect> grid = new HashMap<>(gridTag.getKeys().size() + puzzle.getNodes().size());
		processHexes(size, x, y, (xPos, yPos, turn, rx, ry) -> {
			if(turn % nodeGap == 0){
				Aspect node = puzzle.getNodes().get(turn / nodeGap);
				AspectRenderHelper.renderAspect(node, ctx, xPos + 2, yPos + 2, 1);
				grid.put(new HexOffset(rx, ry), node);
				return false;
			}
			
			String hexId = rx + "," + ry;
			if(gridTag.contains(hexId)){
				ctx.drawTexture(overlayTex, xPos, yPos, 0, 0, 0, 20, 20, 256, 256);
				Aspect aspect = Aspects.byName(gridTag.getString(hexId));
				AspectRenderHelper.renderAspect(aspect, ctx, xPos + 2, yPos + 2, 0);
				grid.put(new HexOffset(rx, ry), aspect);
			}
			
			return false;
		});
		
		// draw connections
		processHexes(size, x, y, (xPos, yPos, turn, rx, ry) -> {
			HexOffset pos = new HexOffset(rx, ry);
			Aspect self = grid.get(pos);
			if(self != null)
				for(HexOffset offset : Chemistry.neighborsByRow(ry)){
					HexOffset neighborPos = pos.add(offset);
					Aspect neighbor = grid.get(neighborPos);
					if(neighbor != null && (self.equals(neighbor.left()) || self.equals(neighbor.right())))
						switch(offset.turn()){
							case 0 -> ctx.drawTexture(overlayTex, xPos + 17, yPos + 8, 0, 42, 11, 8, 5, 256, 256);
							case 1 -> ctx.drawTexture(overlayTex, xPos + 12, yPos + 15, 0, 37, 18, 7, 7, 256, 256);
							case 2 -> ctx.drawTexture(overlayTex, xPos + 1, yPos + 15, 0, 26, 18, 7, 7, 256, 256);
							case 3 -> ctx.drawTexture(overlayTex, xPos - 5, yPos + 8, 0, 20, 11, 8, 5, 256, 256);
							case 4 -> ctx.drawTexture(overlayTex, xPos + 1, yPos - 3, 0, 26, 0, 7, 7, 256, 256);
							case 5 -> ctx.drawTexture(overlayTex, xPos + 12, yPos - 3, 0, 37, 0, 7, 7, 256, 256);
						}
				}
			
			return false;
		});
		
		// tooltips
		selected = combineLeft = combineRight = null;
		renderAfter(ctx, puzzle, notesTag, screenWidth, screenHeight, mouseX, mouseY);
	}
	
	public void onClose(){
		selected = combineLeft = combineRight = null;
	}
	
	public boolean drawItemTooltips(){
		return selected == null;
	}
	
	private static boolean within(int mouseX, int mouseY, int x, int y, int size){
		return within(mouseX, mouseY, x, y, size, size);
	}
	
	private static boolean within(int mouseX, int mouseY, int x, int y, int width, int height){
		return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
	}
	
	private static void highlight(DrawContext ctx, int x, int y, int size){
		highlight(ctx, x, y, size, size);
	}
	
	private static void highlight(DrawContext ctx, int x, int y, int width, int height){
		ctx.fill(x, y, x + width, y + height, 0x44FFFFFF);
	}
}