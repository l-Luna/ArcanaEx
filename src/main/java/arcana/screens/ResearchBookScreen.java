package arcana.screens;

import arcana.client.RenderHelper;
import arcana.research.*;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static arcana.Arcana.arcId;
import static java.lang.Math.*;
import static net.minecraft.util.math.MathHelper.clamp;

public class ResearchBookScreen extends Screen{
	
	private static final String bookPrefix = "textures/gui/research/";
	private static final String bookSuffix = "_book.png";
	private static final Identifier arrowsAndBasesTexture = arcId("textures/gui/research/research_bases.png");
	
	private static final int MAX_PAN = 512;
	private static final int ZOOM_MULTIPLIER = 2;
	
	Book book;
	List<Category> categories;
	Identifier texture;
	List<TooltipButton> buttons = new ArrayList<>();
	Arrows arrows = new Arrows();
	
	@Nullable Screen parent;
	
	static int tab = 0;
	static float zoom = .7f;
	static float xPan = 0, yPan = 0;
	
	public ResearchBookScreen(@NotNull Book book, @Nullable Screen parent){
		super(Text.literal(""));
		this.book = book;
		this.parent = parent;
		categories = book.categoryList();
		texture = new Identifier(book.id().getNamespace(), bookPrefix + book.id().getPath() + bookSuffix);
	}
	
	protected void init(){
		super.init();
		for(int i = 0; i < categories.size(); i++){
			Category category = categories.get(i);
			CategoryButton categoryButton = new CategoryButton((width - frameWidth()) / 2 - 12, 16 + ((height - frameHeight()) / 2) + 20 * i, i, category);
			addDrawableChild(categoryButton);
			buttons.add(categoryButton);
		}
	}
	
	private float xOffset(){
		return ((width / 2f) * (1 / zoom)) + (xPan / 2f);
	}
	
	private float yOffset(){
		return ((height / 2f) * (1 / zoom)) - (yPan / 2f);
	}
	
	// TODO: config
	private int frameWidth(){
		return width - 30;
	}
	
	private int frameHeight(){
		return height - 30;
	}
	
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta){
		renderBackground(matrices);
		RenderSystem.enableBlend();
		super.render(matrices, mouseX, mouseY, delta);
		
		int scX = (width - frameWidth()) / 2 + 16, scY = (height - frameHeight()) / 2 + 17;
		DrawableHelper.enableScissor(scX, scY, scX + frameWidth() - 32, scY + frameHeight() - 34);
		
		renderResearchBackground(matrices);
		renderEntries(matrices, delta);
		
		RenderSystem.disableScissor();
		
		setZOffset(299);
		renderFrame(matrices);
		setZOffset(0);
		renderEntryTooltip(matrices, mouseX, mouseY);
		
		buttons.forEach(button -> button.renderAfter(matrices, mouseX, mouseY));
		RenderSystem.enableBlend();
	}
	
	private void renderResearchBackground(MatrixStack matrices){
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, categories.get(tab).bg());
		
		float xScale = 1024f / (512 + 32 - frameWidth());
		float yScale = 1024f / (512 + 34 - frameHeight());
		float scale = Math.max(xScale, yScale);
		
		int width = frameWidth() - 32;
		float xOffset = xScale == scale ? 0 : (512 - (width + 1024 / scale)) / 2;
		int height = frameHeight() - 34;
		float yOffset = yScale == scale ? 0 : (512 - (height + 1024 / scale)) / 2;
		int x = (this.width - frameWidth()) / 2 + 16;
		int y = (this.height - frameHeight()) / 2 + 17;
		
		drawTexture(matrices, x, y, (-xPan + MAX_PAN) / scale + xOffset, (yPan + MAX_PAN) / scale + yOffset, width, height, MAX_PAN, MAX_PAN);
	}
	
	private void renderEntries(MatrixStack matrices, float delta){
		matrices.push();
		matrices.scale(zoom, zoom, 1);
		var time = MinecraftClient.getInstance().world.getTime() + delta;
		for(Entry entry : categories.get(tab).entries().values()){
			PageStyle style = style(entry);
			if(style != PageStyle.none){
				// render base
				RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
				RenderSystem.setShaderTexture(0, arrowsAndBasesTexture);
				
				int base = base(entry);
				float mult = 1f;
				if(style == PageStyle.inProgress)
					mult = (float)abs(sin(time / 5f) * 0.75f) + .25f;
				else if(style == PageStyle.pending)
					mult = 0.2f;
				RenderSystem.setShaderColor(mult, mult, mult, 1);
				drawTexture(matrices, (int)(entry.x() * 30 + xOffset() + 2), (int)(entry.y() * 30 + yOffset() + 2), base % 4 * 26, base / 4 * 26, 26, 26);
				
				if(entry.icons().size() > 0){
					Icon icon = entry.icons().get((int)((time / 30) % entry.icons().size()));
					int x = (int)(entry.x() * 30 + xOffset() + 7);
					int y = (int)(entry.y() * 30 + yOffset() + 7);
					RenderHelper.renderIcon(matrices, icon, x, y, getZOffset(), zoom);
				}
				
				// render arrows
				RenderSystem.enableBlend();
				RenderSystem.setShader(GameRenderer::getPositionTexShader);
				RenderSystem.setShaderTexture(0, arrowsAndBasesTexture);
				for(Parent parent : entry.parents()){
					Entry pEntry = Research.getEntry(parent.id());
					if(pEntry != null && parent.show() && pEntry.in().equals(entry.in()) && style(pEntry) != PageStyle.none){
						int xdiff = entry.x() - pEntry.x();
						int ydiff = entry.y() - pEntry.y();
						if(xdiff == 0){
							arrows.drawVerticalLine(matrices, entry.x(), entry.y(), pEntry.y());
							if(parent.hasArrowhead()){
								if(ydiff > 0)
									arrows.drawDownArrowTo(matrices, entry);
								else
									arrows.drawUpArrowTo(matrices, entry);
							}
						}else if(ydiff == 0){
							arrows.drawHorizontalLine(matrices, entry.y(), entry.x(), pEntry.x());
							if(parent.hasArrowhead()){
								if(xdiff > 0)
									arrows.drawRightArrowTo(matrices, entry);
								else
									arrows.drawLeftArrowTo(matrices, entry);
							}
						}else{
							boolean large = abs(xdiff) > 1 && abs(ydiff) > 1;
							if(parent.showReverse()){
								arrows.drawSizedVerticalLine(matrices, entry.x(), entry.y(), pEntry.y(), large);
								arrows.drawSizedHorizontalLine(matrices, pEntry.y(), pEntry.x(), entry.x(), large);
								if(xdiff > 0 && ydiff > 0){
									arrows.drawSizedLdCurve(matrices, entry.x(), pEntry.y(), large);
									arrows.drawDownArrowTo(matrices, entry);
								}else if(xdiff > 0 && ydiff < 0){
									arrows.drawSizedLuCurve(matrices, pEntry.x(), entry.y(), large);
									arrows.drawUpArrowTo(matrices, entry);
								}else if(xdiff < 0 && ydiff > 0){
									arrows.drawSizedRdCurve(matrices, entry.x(), pEntry.y(), large);
									arrows.drawDownArrowTo(matrices, entry);
								}else if(xdiff < 0 && ydiff < 0){
									arrows.drawSizedRuCurve(matrices, entry.x(), pEntry.y(), large);
									arrows.drawUpArrowTo(matrices, entry);
								}
							}else{
								arrows.drawSizedHorizontalLine(matrices, entry.y(), entry.x(), pEntry.x(), large);
								arrows.drawSizedVerticalLine(matrices, pEntry.x(), pEntry.y(), entry.y(), large);
								if(xdiff > 0 && ydiff > 0){
									arrows.drawSizedRuCurve(matrices, pEntry.x(), entry.y(), large);
									arrows.drawRightArrowTo(matrices, entry);
								}else if(xdiff > 0 && ydiff < 0){
									arrows.drawSizedRdCurve(matrices, pEntry.x(), entry.y(), large);
									arrows.drawRightArrowTo(matrices, entry);
								}else if(xdiff < 0 && ydiff > 0){
									arrows.drawSizedLuCurve(matrices, pEntry.x(), entry.y(), large);
									arrows.drawLeftArrowTo(matrices, entry);
								}else if(xdiff < 0 && ydiff < 0){
									arrows.drawSizedLdCurve(matrices, pEntry.x(), entry.y(), large);
									arrows.drawLeftArrowTo(matrices, entry);
								}
							}
						}
					}
				}
			}
		}
		matrices.pop();
	}
	
	private void renderFrame(MatrixStack matrices){
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, texture);
		RenderSystem.enableBlend();
		int fWidth = frameWidth(), fHeight = frameHeight();
		int x = (width - fWidth) / 2, y = (height - fHeight) / 2;
		RenderHelper.drawStretchableBox(matrices, x, y, 0, 0, fWidth, fHeight, 69, 140);
		// decorations
		drawTexture(matrices, (x + (fWidth / 2)) - 36, y, 140, 0, 72, 17);
		drawTexture(matrices, (x + (fWidth / 2)) - 36, (y + fHeight) - 18, 140, 17, 72, 18);
		drawTexture(matrices, x, (y + (fHeight / 2)) - 35, 140, 35, 17, 70);
		drawTexture(matrices, x + fWidth - 17, (y + (fHeight / 2)) - 35, 157, 35, 17, 70);
	}
	
	private void renderEntryTooltip(MatrixStack matrices, int mouseX, int mouseY){
		for(Entry entry : categories.get(tab).entries().values()){
			if(hovering(entry, mouseX, mouseY)){
				List<Text> lines = new ArrayList<>(2);
				lines.add(Text.translatable(entry.name()));
				if(entry.desc() != null && !entry.desc().equals(""))
					lines.add(Text.translatable(entry.desc()).formatted(Formatting.GRAY));
				renderTooltip(matrices, lines, mouseX, mouseY);
				break;
			}
		}
	}
	
	private boolean hovering(Entry entry, int mouseX, int mouseY){
		int x = (int)((entry.x() * 30 + xOffset() + 2) * zoom);
		int y = (int)((entry.y() * 30 + yOffset() + 2) * zoom);
		int scrx = (width - frameWidth()) / 2 + 16, scry = (height - frameHeight()) / 2 + 17;
		int visibleWidth = frameWidth() - 32, visibleHeight = frameHeight() - 34;
		return mouseX >= x && mouseX <= x + (26 * zoom) && mouseY >= y && mouseY <= y + (26 * zoom) && mouseX >= scrx && mouseX <= scrx + visibleWidth && mouseY >= scry && mouseY <= scry + visibleHeight;
	}
	
	public boolean mouseClicked(double mouseX, double mouseY, int button){
		// TODO: open research entry UI...
		return super.mouseClicked(mouseX, mouseY, button);
	}
	
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY){
		xPan += (deltaX * ZOOM_MULTIPLIER) / zoom;
		yPan -= (deltaY * ZOOM_MULTIPLIER) / zoom;
		xPan = clamp(xPan, -MAX_PAN, MAX_PAN);
		yPan = clamp(yPan, -MAX_PAN, MAX_PAN);
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}
	
	public boolean mouseScrolled(double mouseX, double mouseY, double scroll){
		float amnt = 1.2f;
		if((scroll < 0 && zoom > 0.5) || (scroll > 0 && zoom < 1))
			zoom *= scroll > 0 ? amnt : 1 / amnt;
		if(zoom > 1f)
			zoom = 1f;
		return super.mouseScrolled(mouseX, mouseY, scroll);
	}
	
	public boolean shouldPause(){
		return false;
	}
	
	private PageStyle style(Entry entry){
		// TODO: check research completion
		return PageStyle.complete;
	}
	
	private int base(Entry entry){
		int base = 8;
		if(entry.meta().contains("purple_base"))
			base = 0;
		else if(entry.meta().contains("yellow_base"))
			base = 4;
		else if(entry.meta().contains("no_base"))
			return 12;
		
		if(entry.meta().contains("round_base"))
			return base + 1;
		else if(entry.meta().contains("square_base"))
			return base + 2;
		else if(entry.meta().contains("hexagon_base"))
			return base + 3;
		else if(entry.meta().contains("spiky_base"))
			return base;
		return base + 2;
	}
	
	public enum PageStyle{
		complete,
		inProgress,
		pending,
		none
	}
	
	private final /* non-static */ class Arrows{
		
		// TODO: cleanup
		// it's overly granular and could be simplified by taking Entrys instead of ints more often
		
		int gX2SX(int gX){
			return (int)((gX * 30 + xOffset()));
		}
		
		int gY2SY(int gY){
			return (int)((gY * 30 + yOffset()));
		}
		
		void drawHorizontalSegment(MatrixStack stack, int gX, int gY){
			drawTexture(stack, gX2SX(gX), gY2SY(gY), 104, 0, 30, 30);
		}
		
		void drawVerticalSegment(MatrixStack stack, int gX, int gY){
			drawTexture(stack, gX2SX(gX), gY2SY(gY), 134, 0, 30, 30);
		}
		
		void drawHorizontalLine(MatrixStack stack, int y, int startGX, int endGX){
			int temp = startGX;
			// *possibly* swap them
			startGX = min(startGX, endGX);
			endGX = max(endGX, temp);
			// *exclusive*
			for(int j = startGX + 1; j < endGX; j++){
				drawHorizontalSegment(stack, j, y);
			}
		}
		
		void drawVerticalLine(MatrixStack stack, int x, int startGY, int endGY){
			int temp = startGY;
			// *possibly* swap them
			startGY = min(startGY, endGY);
			endGY = max(endGY, temp);
			// *exclusive*
			for(int j = startGY + 1; j < endGY; j++)
				drawVerticalSegment(stack, x, j);
		}
		
		void drawHorizontalLineMinus1(MatrixStack stack, int y, int startGX, int endGX){
			int temp = startGX;
			// take one
			if(startGX > endGX)
				endGX++;
			else
				endGX--;
			// *possibly* swap them
			startGX = min(startGX, endGX);
			endGX = max(endGX, temp);
			// *exclusive*
			for(int j = startGX + 1; j < endGX; j++)
				drawHorizontalSegment(stack, j, y);
		}
		
		void drawVerticalLineMinus1(MatrixStack stack, int x, int startGY, int endGY){
			int temp = startGY;
			// take one
			if(startGY > endGY)
				endGY++;
			else
				endGY--;
			// *possibly* swap them
			startGY = min(startGY, endGY);
			endGY = max(endGY, temp);
			// *exclusive*
			for(int j = startGY + 1; j < endGY; j++)
				drawVerticalSegment(stack, x, j);
		}
		
		void drawLuCurve(MatrixStack stack, int gX, int gY){
			drawTexture(stack, gX2SX(gX), gY2SY(gY), 164, 0, 30, 30);
		}
		
		void drawRuCurve(MatrixStack stack, int gX, int gY){
			drawTexture(stack, gX2SX(gX), gY2SY(gY), 194, 0, 30, 30);
		}
		
		void drawLdCurve(MatrixStack stack, int gX, int gY){
			drawTexture(stack, gX2SX(gX), gY2SY(gY), 224, 0, 30, 30);
		}
		
		void drawRdCurve(MatrixStack stack, int gX, int gY){
			drawTexture(stack, gX2SX(gX), gY2SY(gY), 104, 30, 30, 30);
		}
		
		// we offset the large curves so that they can be placed in the same way as the small ones
		void drawLargeLuCurve(MatrixStack stack, int gX, int gY){
			drawTexture(stack, gX2SX(gX - 1), gY2SY(gY - 1), 134, 30, 60, 60);
		}
		
		void drawLargeRuCurve(MatrixStack stack, int gX, int gY){
			drawTexture(stack, gX2SX(gX), gY2SY(gY - 1), 194, 30, 60, 60);
		}
		
		void drawLargeLdCurve(MatrixStack stack, int gX, int gY){
			drawTexture(stack, gX2SX(gX - 1), gY2SY(gY), 134, 90, 60, 60);
		}
		
		void drawLargeRdCurve(MatrixStack stack, int gX, int gY){
			drawTexture(stack, gX2SX(gX), gY2SY(gY), 194, 90, 60, 60);
		}
		
		// selects from regular/large curves and regular/minus-1 lines
		void drawSizedLuCurve(MatrixStack stack, int gX, int gY, boolean large){
			if(large)
				drawLargeLuCurve(stack, gX, gY);
			else
				drawLuCurve(stack, gX, gY);
		}
		
		void drawSizedRuCurve(MatrixStack stack, int gX, int gY, boolean large){
			if(large)
				drawLargeRuCurve(stack, gX, gY);
			else
				drawRuCurve(stack, gX, gY);
		}
		
		void drawSizedLdCurve(MatrixStack stack, int gX, int gY, boolean large){
			if(large)
				drawLargeLdCurve(stack, gX, gY);
			else
				drawLdCurve(stack, gX, gY);
		}
		
		void drawSizedRdCurve(MatrixStack stack, int gX, int gY, boolean large){
			if(large)
				drawLargeRdCurve(stack, gX, gY);
			else
				drawRdCurve(stack, gX, gY);
		}
		
		void drawSizedVerticalLine(MatrixStack stack, int x, int startGY, int endGY, boolean large){
			if(large)
				drawVerticalLineMinus1(stack, x, startGY, endGY);
			else
				drawVerticalLine(stack, x, startGY, endGY);
		}
		
		void drawSizedHorizontalLine(MatrixStack stack, int y, int startGX, int endGX, boolean large){
			if(large)
				drawHorizontalLineMinus1(stack, y, startGX, endGX);
			else
				drawHorizontalLine(stack, y, startGX, endGX);
		}
		
		void drawDownArrow(MatrixStack stack, int gX, int gY){
			drawTexture(stack, gX2SX(gX), gY2SY(gY) + 1, 104, 60, 30, 30);
		}
		
		void drawUpArrow(MatrixStack stack, int gX, int gY){
			drawTexture(stack, gX2SX(gX), gY2SY(gY) - 1, 104, 120, 30, 30);
		}
		
		void drawLeftArrow(MatrixStack stack, int gX, int gY){
			drawTexture(stack, gX2SX(gX) - 1, gY2SY(gY), 104, 90, 30, 30);
		}
		
		void drawRightArrow(MatrixStack stack, int gX, int gY){
			drawTexture(stack, gX2SX(gX) + 1, gY2SY(gY), 104, 150, 30, 30);
		}
		
		// offsets based on arrow type
		void drawDownArrowTo(MatrixStack stack, Entry entry){
			drawDownArrow(stack, entry.x(), entry.y() - 1);
		}
		
		void drawUpArrowTo(MatrixStack stack, Entry entry){
			drawUpArrow(stack, entry.x(), entry.y() + 1);
		}
		
		void drawLeftArrowTo(MatrixStack stack, Entry entry){
			drawLeftArrow(stack, entry.x() + 1, entry.y());
		}
		
		void drawRightArrowTo(MatrixStack stack, Entry entry){
			drawRightArrow(stack, entry.x() - 1, entry.y());
		}
	}
	
	private interface TooltipButton{
		
		void renderAfter(MatrixStack matrices, int mouseX, int mouseY);
	}
	
	/* non-static */ class CategoryButton extends ButtonWidget implements TooltipButton{
		
		int categoryIdx;
		Category category;
		
		public CategoryButton(int x, int y, int categoryIdx, Category category){
			super(x, y, 16, 16, Text.literal(""), button -> {
				/*if(MinecraftClient.getInstance().currentScreen instanceof ResearchBookScreen rbs)
					rbs.ca*/
				tab = categoryIdx;
			});
			this.categoryIdx = categoryIdx;
			this.category = category;
			visible = true;
		}
		
		public void render(MatrixStack matrices, int mouseX, int mouseY, float delta){
			if(visible){
				int drawX = x - (categoryIdx == tab ? 6 : (hovered) ? 4 : 0);
				RenderHelper.renderIcon(matrices, category.icon(), drawX, y, getZOffset());
				hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
			}
		}
		
		public void renderAfter(MatrixStack matrices, int mouseX, int mouseY){
			if(hovered){
				// TODO: show % completion
				ResearchBookScreen.this.renderTooltip(matrices, Text.translatable(category.name()), mouseX, mouseY);
			}
		}
	}
}