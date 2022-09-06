package arcana.screens;

import arcana.client.ArcanaClient;
import arcana.client.RenderHelper;
import arcana.components.Researcher;
import arcana.research.*;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static arcana.Arcana.arcId;
import static java.lang.Math.*;
import static net.minecraft.util.math.MathHelper.clamp;

public class ResearchBookScreen extends Screen{
	
	public static final String bookPrefix = "textures/gui/research/";
	
	private static final String bookSuffix = "_book.png";
	private static final Identifier arrowsAndBasesTexture = arcId("textures/gui/research/research_bases.png");
	
	private static final int MAX_PAN = 512;
	private static final int ZOOM_MULTIPLIER = 2;
	
	Book book;
	List<Category> categories;
	Identifier texture;
	List<TooltipButton> buttons = new ArrayList<>();
	List<PinButton> pinButtons = new ArrayList<>();
	Arrows arrows = new Arrows();
	
	@Nullable Screen parent;
	
	static int tab = 0;
	static float zoom = .7f;
	static float xPan = 0, yPan = 0;
	
	public ResearchBookScreen(@NotNull Book book, @Nullable Screen parent){
		super(Text.literal(""));
		this.book = book;
		this.parent = parent;
		categories = book.categories();
		texture = new Identifier(book.id().getNamespace(), bookPrefix + book.id().getPath() + bookSuffix);
	}
	
	protected void init(){
		super.init();
		int passed = 0;
		for(int i = 0; i < categories.size(); i++){
			Category category = categories.get(i);
			Entry required = Research.getEntry(category.requirement());
			if(required == null || Researcher.from(client.player).entryStage(required) == required.sections().size()){
				CategoryButton categoryButton = new CategoryButton((width - frameWidth()) / 2 - 12, 16 + ((height - frameHeight()) / 2) + 20 * passed, i, category);
				addDrawableChild(categoryButton);
				buttons.add(categoryButton);
				passed++;
			}
		}
		
		refreshPins();
	}
	
	protected void refreshPins(){
		for(PinButton button : pinButtons){
			buttons.remove(button);
			remove(button);
		}
		pinButtons.clear();
		var pins = Researcher.from(client.player).getPinned();
		int i = 0;
		for(var entryPins : pins.entrySet()){
			Entry entry = Research.getEntry(entryPins.getKey());
			if(entry != null && entry.category().book().equals(book)){
				for(Integer stage : entryPins.getValue()){
					if(stage < entry.sections().size()){
						Pin pin = entry.sections().get(stage).pins(stage, client.world, entry).findFirst().orElse(null);
						if(pin != null){
							PinButton pinButton = new PinButton((width + frameWidth()) / 2 + 1, 16 + ((height - frameHeight()) / 2) + i * 22, pin);
							addDrawableChild(pinButton);
							buttons.add(pinButton);
							pinButtons.add(pinButton);
							i++;
						}
					}
				}
			}
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
		return width - 60;
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
		for(Entry entry : categories.get(tab).entries()){
			PageStyle style = style(entry);
			if(style != PageStyle.none){
				int x = (int)(entry.x() * 30 + xOffset());
				int y = (int)(entry.y() * 30 + yOffset());
				
				// render warp effect
				int warping = entry.warping();
				if(warping > 0 && warping <= 5){
					matrices.push();
					matrices.translate(x + 15, y + 15, getZOffset());
					final int sq = 20;
					for(int i = 0; i < sq; i++){
						matrices.push();
						matrices.multiply(Quaternion.fromEulerXyzDegrees(new Vec3f(0, 0, (360f / sq) * i)));
						matrices.translate(sin(time / 20f) * warping + warping, 0, 0);
						fill(matrices, 0, 0, 12, 12, 0x11ff00ff);
						matrices.pop();
					}
					matrices.pop();
				}
				
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
				drawTexture(matrices, x + 2, y + 2, base % 4 * 26, base / 4 * 26, 26, 26);
				
				if(entry.icons().size() > 0){
					Icon icon = entry.icons().get((int)((time / 30) % entry.icons().size()));
					RenderHelper.renderIcon(matrices, icon, x + 7, y + 7, getZOffset(), zoom);
				}
				
				// render arrows
				RenderSystem.enableBlend();
				RenderSystem.setShader(GameRenderer::getPositionTexShader);
				RenderSystem.setShaderTexture(0, arrowsAndBasesTexture);
				RenderSystem.setShaderColor(1, 1, 1, 1);
				for(Parent parent : entry.parents()){
					Entry pEntry = Research.getEntry(parent.id());
					if(pEntry != null && parent.show() && pEntry.category().equals(entry.category()) && style(pEntry) != PageStyle.none){
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
									if(parent.hasArrowhead())
										arrows.drawDownArrowTo(matrices, entry);
								}else if(xdiff > 0 && ydiff < 0){
									arrows.drawSizedLuCurve(matrices, pEntry.x(), entry.y(), large);
									if(parent.hasArrowhead())
										arrows.drawUpArrowTo(matrices, entry);
								}else if(xdiff < 0 && ydiff > 0){
									arrows.drawSizedRdCurve(matrices, entry.x(), pEntry.y(), large);
									if(parent.hasArrowhead())
										arrows.drawDownArrowTo(matrices, entry);
								}else if(xdiff < 0 && ydiff < 0){
									arrows.drawSizedRuCurve(matrices, entry.x(), pEntry.y(), large);
									if(parent.hasArrowhead())
										arrows.drawUpArrowTo(matrices, entry);
								}
							}else{
								arrows.drawSizedHorizontalLine(matrices, entry.y(), entry.x(), pEntry.x(), large);
								arrows.drawSizedVerticalLine(matrices, pEntry.x(), pEntry.y(), entry.y(), large);
								if(xdiff > 0 && ydiff > 0){
									arrows.drawSizedRuCurve(matrices, pEntry.x(), entry.y(), large);
									if(parent.hasArrowhead())
										arrows.drawRightArrowTo(matrices, entry);
								}else if(xdiff > 0 && ydiff < 0){
									arrows.drawSizedRdCurve(matrices, pEntry.x(), entry.y(), large);
									if(parent.hasArrowhead())
										arrows.drawRightArrowTo(matrices, entry);
								}else if(xdiff < 0 && ydiff > 0){
									arrows.drawSizedLuCurve(matrices, pEntry.x(), entry.y(), large);
									if(parent.hasArrowhead())
										arrows.drawLeftArrowTo(matrices, entry);
								}else if(xdiff < 0 && ydiff < 0){
									arrows.drawSizedLdCurve(matrices, pEntry.x(), entry.y(), large);
									if(parent.hasArrowhead())
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
		for(Entry entry : categories.get(tab).entries()){
			if(hovering(entry, mouseX, mouseY)){
				List<Text> lines = new ArrayList<>(2);
				lines.add(Text.translatable(entry.name()));
				if(entry.desc() != null && !entry.desc().equals(""))
					lines.add(Text.translatable(entry.desc()).formatted(Formatting.GRAY));
				int warping = entry.warping();
				if(warping > 0 && warping <= 5)
					lines.add(Text.translatable("research.book.warping." + warping).formatted(Formatting.DARK_PURPLE));
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
		for(Entry entry : categories.get(tab).entries()){
			PageStyle style;
			if(hovering(entry, (int)mouseX, (int)mouseY)){
				if(button != 2){
					if((style = style(entry)) == PageStyle.complete || style == PageStyle.inProgress)
						// left/right (& other) click: open page
						MinecraftClient.getInstance().setScreen(new ResearchEntryScreen(entry, this));
				}else
					// middle click: try advance
					ArcanaClient.sendTryAdvance(entry);
				break;
			}
		}
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
	
	public PageStyle style(Entry entry){
		// locked entries are always locked
		if(entry.meta().contains("locked"))
			return PageStyle.pending;
		// if the page is at full progress, its complete.
		Researcher r = Researcher.from(MinecraftClient.getInstance().player);
		if(r.entryStage(entry) >= entry.sections().size())
			return PageStyle.complete;
		// if its progress is greater than zero, then its in progress.
		if(r.entryStage(entry) > 0)
			return PageStyle.inProgress;
		// if it has no parents *and* the "root" tag, its available to do and in progress.
		if(entry.meta().contains("root") && entry.parents().size() == 0)
			return PageStyle.inProgress;
		// if it does not have the "hidden" tag:
		if(!entry.meta().contains("hidden")){
			List<PageStyle> parentStyles = entry.parents().stream().map(parent -> Pair.of(Research.getEntry(parent.id()), parent)).map(p -> parentStyle(p.getFirst(), p.getSecond())).toList();
			// if all of its parents are complete, it is available to do and in progress.
			if(parentStyles.stream().allMatch(PageStyle.complete::equals))
				return PageStyle.inProgress;
			// if at least one of its parents are in progress, its pending.
			if(parentStyles.stream().anyMatch(PageStyle.inProgress::equals))
				return PageStyle.pending;
		}
		// otherwise, its invisible
		return PageStyle.none;
	}
	
	public PageStyle parentStyle(Entry entry, Parent parent){
		// if the parent is greater than required, consider it complete
		Objects.requireNonNull(entry, "Tried to get the stage of a parent entry that doesn't exist: " + parent.id().toString() + " (from " + parent.asString() + ")");
		Researcher r = Researcher.from(MinecraftClient.getInstance().player);
		if(parent.stage() == -1){
			if(r.entryStage(entry) >= entry.sections().size())
				return PageStyle.complete;
		}else if(r.entryStage(entry) >= parent.stage())
			return PageStyle.complete;
		// if its progress is greater than zero, then its in progress.
		if(r.entryStage(entry) > 0)
			return PageStyle.inProgress;
		// if it has no parents *and* the "root" tag, its available to do and in progress.
		if(entry.meta().contains("root") && entry.parents().size() == 0)
			return PageStyle.inProgress;
		// if it does not have the "hidden" tag:
		if(!entry.meta().contains("hidden")){
			List<PageStyle> parentStyles = entry.parents().stream().map(p -> Pair.of(Research.getEntry(p.id()), p)).map(p -> parentStyle(p.getFirst(), p.getSecond())).toList();
			// if all of its parents are complete, it is available to do and in progress.
			if(parentStyles.stream().allMatch(PageStyle.complete::equals))
				return PageStyle.inProgress;
			// if at least one of its parents are in progress, its pending.
			if(parentStyles.stream().anyMatch(PageStyle.inProgress::equals))
				return PageStyle.pending;
		}
		// otherwise, its invisible
		return PageStyle.none;
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
	
	private /* non-static */ class CategoryButton extends ButtonWidget implements TooltipButton{
		
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
				if(category.entries().size() > 0){
					Researcher researcher = Researcher.from(client.player);
					int sum = 0;
					for(Entry entry : category.entries())
						sum += researcher.entryStage(entry) >= entry.sections().size() ? 1 : 0;
					int percent = (sum * 100) / category.entries().size();
					ResearchBookScreen.this.renderTooltip(matrices, Text.translatable(
							"research.book.category_with_completion",
							Text.translatable(category.name()),
							Text.literal(String.valueOf(percent))),
							mouseX, mouseY);
				}else
					ResearchBookScreen.this.renderTooltip(matrices, Text.translatable(category.name()), mouseX, mouseY);
			}
		}
	}
	
	private /* non-static */ class PinButton extends ButtonWidget implements TooltipButton{
		
		private final Pin pin;
		
		public PinButton(int x, int y, Pin pin){
			super(x, y, 18, 18, Text.literal(""), b -> {
				if(hasControlDown()){
					// unpin
					Researcher from = Researcher.from(MinecraftClient.getInstance().player);
					List<Integer> pinned = from.getPinned().get(pin.entry().id());
					if(pinned != null){
						from.removePinned(pin.entry().id(), pin.stage());
						ArcanaClient.sendModifyPins(pin, false);
					}
					// and remove this button
					ResearchBookScreen thisScreen = (ResearchBookScreen)client.getInstance().currentScreen;
					thisScreen.refreshPins();
				}else{
					Entry entry = pin.entry();
					if(Researcher.from(MinecraftClient.getInstance().player).entryStage(entry) >= pin.stage()){
						ResearchEntryScreen in = new ResearchEntryScreen(entry, MinecraftClient.getInstance().currentScreen);
						int stageIndex = in.indexOfStage(pin.stage());
						in.idx = stageIndex % 2 == 0 ? stageIndex : stageIndex - 1;
						MinecraftClient.getInstance().setScreen(in);
					}
				}
			});
			this.pin = pin;
		}
		
		public void render(MatrixStack matrices, int mouseX, int mouseY, float delta){
			if(visible){
				int xOffset = hovered ? 3 : 0;
				RenderSystem.setShaderTexture(0, texture);
				drawTexture(matrices, x - 2, y - 1, 6 - xOffset, 140, 34 - (6 - xOffset), 18);
				RenderHelper.renderIcon(matrices, pin.icon(), x + xOffset, y - 1, 0);
			}
		}
		
		public void renderAfter(MatrixStack matrices, int mouseX, int mouseY){
			hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
			if(pin.icon().stack() != null)
				if(hovered){
					var stack = pin.icon().stack();
					List<Text> tooltips = new ArrayList<>(getTooltipFromItem(stack));
					tooltips.add(Text.translatable("research.entry.unpin").formatted(Formatting.AQUA));
					ResearchBookScreen.this.renderTooltip(matrices, tooltips, stack.getTooltipData(), mouseX, mouseY);
				}
		}
	}
}