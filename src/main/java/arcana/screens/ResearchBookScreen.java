package arcana.screens;

import arcana.Arcana;
import arcana.client.ArcanaClient;
import arcana.client.RenderHelper;
import arcana.components.Researcher;
import arcana.research.*;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3f;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.*;

import static arcana.Arcana.arcId;
import static java.lang.Math.*;
import static net.minecraft.util.math.MathHelper.clamp;

public class ResearchBookScreen extends Screen{
	
	public static final String BOOK_PREFIX = "textures/gui/research/";
	public static final String BOOK_SUFFIX = "_book.png";
	
	private static final Identifier ICONS_TEX = arcId("textures/gui/research/research_bases.png");
	
	private static final int MAX_PAN = 512;
	private static final int ZOOM_MULTIPLIER = 2;
	
	private static boolean debug = false;
	private static int tab = 0;
	private static float zoom = .7f;
	private static float xPan = 0, yPan = 0;
	
	// fix nullability warnings
	@NotNull
	private final MinecraftClient client = MinecraftClient.getInstance();
	@NotNull
	private final PlayerEntity player = Objects.requireNonNull(client.player);
	
	private final Book book;
	private final List<Category> categories;
	private final Identifier texture;
	private final List<TooltipButton> buttons = new ArrayList<>();
	private final List<PinButton> pinButtons = new ArrayList<>();
	private final Arrows arrows = new Arrows();
	private boolean wasDragging = false;
	
	private static final Set<Entry> unreadEntries = new HashSet<>(), unreadAddendaEntries = new HashSet<>();
	private final Set<Entry> progressableEntries = new HashSet<>();
	private final Map<Category, Integer> categoryIcons = new HashMap<>();
	
	public ResearchBookScreen(@NotNull Book book){
		super(Text.literal(""));
		this.book = book;
		categories = book.categories();
		if(tab >= categories.size())
			tab = categories.size() - 1;
		if(tab < 0)
			tab = 0;
		texture = new Identifier(book.id().getNamespace(), BOOK_PREFIX + book.id().getPath() + BOOK_SUFFIX);
	}
	
	public static void notifyNewEntries(Set<Entry> newEntries){
		unreadEntries.addAll(newEntries);
		if(MinecraftClient.getInstance().currentScreen instanceof ResearchEntryScreen entryScreen)
			unreadEntries.remove(entryScreen.getEntry());
	}
	
	public static void notifyNewAddendaEntry(Entry newEntry){
		if(MinecraftClient.getInstance().currentScreen instanceof ResearchEntryScreen entryScreen && entryScreen.getEntry().equals(newEntry))
			return;
		unreadAddendaEntries.add(newEntry);
	}
	
	public static void resetNewEntries(){
		unreadEntries.clear();
		unreadAddendaEntries.clear();
	}
	
	protected void init(){
		super.init();
		int passed = 0;
		for(int i = 0; i < categories.size(); i++){
			Category category = categories.get(i);
			Entry required = Research.getEntry(category.requirement());
			if(required == null || Researcher.from(player).entryStage(required) == required.sections().size()){
				CategoryButton categoryButton = new CategoryButton((width - frameWidth()) / 2 - 12, 16 + ((height - frameHeight()) / 2) + 20 * passed, i, category);
				addDrawableChild(categoryButton);
				buttons.add(categoryButton);
				passed++;
			}
		}
		
		refreshPins();
		refreshProgressable();
	}
	
	protected void refreshPins(){
		for(PinButton button : pinButtons){
			buttons.remove(button);
			remove(button);
		}
		pinButtons.clear();
		var pins = Researcher.from(player).getPinned();
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
	
	public void refreshProgressable(){
		progressableEntries.clear();
		categoryIcons.clear();
		Researcher researcher = Researcher.from(player);
		// don't incorrectly mark entries as progressable when opening the book for the first time
		if(!researcher.isEntryComplete(Research.getEntry(BuiltinResearch.rootEntry)))
			return;
		for(Category category : categories){
			int iconU = -1;
			for(Entry entry : category.entries()){
				int section = researcher.entryStage(entry);
				if(section < entry.sections().size() && entry.sections().get(section).getRequirements().stream().allMatch(x -> x.satisfiedBy(player)))
					progressableEntries.add(entry);
				if(researcher.canAccess(entry)){
					int entryIconU = pickIconU(entry);
					if(entryIconU != -1 && (entryIconU < iconU || iconU == -1))
						iconU = entryIconU;
				}
			}
			categoryIcons.put(category, iconU);
		}
	}
	
	private float xOffset(){
		return ((width / 2f) * (1 / zoom)) + (xPan / 2f);
	}
	
	private float yOffset(){
		return ((height / 2f) * (1 / zoom)) - (yPan / 2f);
	}
	
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
		
		int gx = (int)Math.floor((mouseX / zoom - xOffset()) / 30);
		int gy = (int)Math.floor((mouseY / zoom - yOffset()) / 30);
		if(debug){
			matrices.push();
			matrices.translate(0, 0, 300);
			textRenderer.draw(matrices, "X: %d / Y : %d".formatted(gx, gy), scX + 2, scY + 4, 0xFFFFFF);
		}
		
		if(debug || Arcana.CONFIG.alwaysShowResearchBookCursor){
			matrices.scale(zoom, zoom, zoom);
			RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
			RenderSystem.setShaderTexture(0, ICONS_TEX);
			drawTexture(matrices, (int)((gx*30 + xOffset()) + 1), (int)((gy*30 + yOffset()) + 1), 0, 78, 28, 28);
			matrices.pop();
		}
		
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
		
		int bgWidth = frameWidth() - 32;
		int bgHeight = frameHeight() - 34;
		float rawXScale = 1.2f * (bgWidth / 512f);
		float rawYScale = 1.2f * (bgHeight / 512f);
		float scale = Math.max(rawXScale, rawYScale);
		
		int screenX = (this.width - frameWidth()) / 2 + 16;
		int screenY = (this.height - frameHeight()) / 2 + 17;
		float maxSize = Math.max(bgWidth, bgHeight);
		float xSzDiff = scale * MAX_PAN * (maxSize - bgWidth) / maxSize;
		float ySzDiff = scale * MAX_PAN * (maxSize - bgHeight) / maxSize;
		// remap an area of size [-MAX_PAN, MAX_PAN] -> [szDiff, MAX_PAN * scale - size + szDiff]
		float u = (((-xPan / 2f + 256f) / MAX_PAN) * (MAX_PAN * scale - maxSize)) + xSzDiff / 2f;
		float v = (((yPan / 2f + 256f) / MAX_PAN) * (MAX_PAN * scale - maxSize)) + ySzDiff / 2f;
		// TODO: not completely correctly centred on the smaller axis though
		
		drawTexture(matrices, screenX, screenY, u, v, bgWidth, bgHeight, (int)Math.ceil(MAX_PAN * scale), (int)Math.ceil(MAX_PAN * scale));
	}
	
	private void renderEntries(MatrixStack matrices, float delta){
		matrices.push();
		matrices.scale(zoom, zoom, 1);
		float time = client.world.getTime() + delta;
		for(Entry entry : categories.get(tab).entries()){
			PageStyle style = style(entry);
			if(style != PageStyle.NONE){
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
				RenderSystem.setShaderTexture(0, ICONS_TEX);
				
				Vec2f baseUv = baseUv(entry);
				float mult = 1f;
				if(style == PageStyle.IN_PROGRESS)
					mult = (float)abs(sin(time / 5f) * 0.75f) + .25f;
				else if(style == PageStyle.PENDING)
					mult = 0.2f;
				RenderSystem.setShaderColor(mult, mult, mult, 1);
				drawTexture(matrices, x + 2, y + 2, (int)baseUv.x, (int)baseUv.y, 26, 26);
				RenderSystem.setShaderColor(1, 1, 1, 1);
				
				// render icons
				if(style != PageStyle.PENDING){
					int iconU = pickIconU(entry);
					if(iconU >= 0)
						RenderHelper.drawTexture(matrices, x + 20, y, 0, iconU, 107, 9, 9, 1, 1, 1);
				}
				
				if(!entry.icons().isEmpty()){
					int frames = entry.getIntMeta("icon_frames");
					if(entry.meta().contains("stacked_icons")){
						for(Icon icon : entry.icons())
							RenderHelper.renderIcon(matrices, icon, x + 7, y + 7, getZOffset(), zoom, frames);
					}else if(entry.meta().contains("detail_icons")){
						Icon main = entry.icons().get(0);
						RenderHelper.renderIcon(matrices, main, x + 6, y + 6, getZOffset(), zoom, frames);
						Icon detail = entry.icons().get(1 + (int)((time / 30) % (entry.icons().size() - 1)));
						float scale = 0.8f;
						float offset = 9 + 16 * (1 - scale);
						matrices.push();
						matrices.scale(scale, scale, 1);
						// TODO: fix irritating jitter (related to rounding in nested scaling?)
						// TODO: fix rendering over tooltips
						RenderHelper.renderIcon(matrices, detail, (int)Math.ceil((x+offset)), (int)Math.ceil((y+offset)), getZOffset()+1000, zoom*scale, frames);
						matrices.pop();
					}else{
						Icon icon = entry.icons().get((int)((time / 30) % entry.icons().size()));
						float u = style == PageStyle.PENDING ? 0.2f : 1f;
						RenderHelper.renderIcon(matrices, icon, x + 7, y + 7, getZOffset(), zoom, frames, u, u, u, 1);
					}
				}
				
				// render arrows
				RenderSystem.enableBlend();
				RenderSystem.setShader(GameRenderer::getPositionTexShader);
				RenderSystem.setShaderTexture(0, ICONS_TEX);
				for(Parent parent : entry.parents()){
					RenderSystem.setShaderColor(1, 1, 1, style == PageStyle.PENDING ? 0.2f : 1);
					Entry pEntry = Research.getEntry(parent.id());
					if(pEntry != null && parent.show() && pEntry.category().equals(entry.category()) && style(pEntry) != PageStyle.NONE){
						if(!parent.hasArrowhead())
							RenderSystem.setShaderColor(1, 1, 1, style == PageStyle.PENDING ? 0.2f : 0.6f);
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
									arrows.drawSizedLuCurve(matrices, entry.x(), pEntry.y(), large);
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
	
	private int pickIconU(Entry entry){
		int iconU = -1;
		if(unreadAddendaEntries.contains(entry))
			iconU = 0;
		else if(progressableEntries.contains(entry))
			iconU = 9;
		else if(unreadEntries.contains(entry))
			iconU = 18;
		return iconU;
	}
	
	private void renderFrame(DrawContext ctx){
		RenderSystem.setShaderColor(1, 1, 1, 1);
		RenderSystem.enableBlend();
		int fWidth = frameWidth(), fHeight = frameHeight();
		int x = (width - fWidth) / 2, y = (height - fHeight) / 2;
		RenderHelper.drawStretchableBox(ctx, texture, x, y, 0, 0, fWidth, fHeight, 69, 140);
		// decorations
		ctx.drawTexture(texture, (x + (fWidth / 2)) - 36, y, 140, 0, 72, 17);
		ctx.drawTexture(texture, (x + (fWidth / 2)) - 36, (y + fHeight) - 18, 140, 17, 72, 18);
		ctx.drawTexture(texture, x, (y + (fHeight / 2)) - 35, 140, 35, 17, 70);
		ctx.drawTexture(texture, x + fWidth - 17, (y + (fHeight / 2)) - 35, 157, 35, 17, 70);
	}
	
	private void renderEntryTooltip(MatrixStack matrices, int mouseX, int mouseY){
		for(Entry entry : categories.get(tab).entries()){
			if(hovering(entry, mouseX, mouseY)){
				PageStyle style = style(entry);
				if(style == PageStyle.COMPLETE || style == PageStyle.IN_PROGRESS
						|| (style == PageStyle.PENDING && !entry.meta().contains("hidden") && !wasDragging)){
					List<Text> lines = new ArrayList<>(2);
					lines.add(Text.translatable(entry.name()));
					if(entry.desc() != null && !entry.desc().isEmpty())
						lines.add(Text.translatable(entry.desc()).formatted(Formatting.GRAY));
					int warping = entry.warping();
					if(warping > 0 && warping <= 5)
						lines.add(Text.translatable("research.book.warping." + warping).formatted(Formatting.DARK_PURPLE));
					
					if(debug){
						lines.add(Text.literal(entry.id().toString()).formatted(Formatting.DARK_GRAY));
						for(String s : entry.meta())
							lines.add(Text.literal("- " + s).formatted(Formatting.DARK_GRAY));
					}
					
					renderTooltip(matrices, lines, mouseX, mouseY);
				}
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
		int scrx = (width - frameWidth()) / 2 + 16, scry = (height - frameHeight()) / 2 + 17;
		int visibleWidth = frameWidth() - 32, visibleHeight = frameHeight() - 34;
		if(mouseX >= scrx && mouseX <= scrx + visibleWidth && mouseY >= scry && mouseY <= scry + visibleHeight){
			for(Entry entry : categories.get(tab).entries()){
				PageStyle style;
				if(hovering(entry, (int)mouseX, (int)mouseY)){
					if(button == 0){
						if((style = style(entry)) == PageStyle.COMPLETE || style == PageStyle.IN_PROGRESS){
							// left/right (& other) click: open page
							client.setScreen(new ResearchEntryScreen(entry, this));
							unreadEntries.remove(entry);
							unreadAddendaEntries.remove(entry);
							return true;
						}
						break;
					}else if(button == 2 && style(entry) == PageStyle.IN_PROGRESS){
						// middle click: try advance
						ArcanaClient.sendTryAdvance(entry);
						return true;
					}
					break;
				}
			}
			
			if(debug && hasControlDown()){
				int gx = (int)Math.floor((mouseX / zoom - xOffset()) / 30);
				int gy = (int)Math.floor((mouseY / zoom - yOffset()) / 30);
				client.keyboard.setClipboard("""
						{
							"key": "arcana:XYZ",
							"name": "research.arcana.XYZ.title",
							"desc": "research.arcana.XYZ.desc",
							"icons": [
								"arcana:XYZ"
							],
							"category": "%s",
							"parents": [
								"arcana:ABC"
							],
							"x": %d,
							"y": %d,
							"sections": [
								{
									"type": "text",
									"content": "research.arcana.XYZ.stages.1",
									"requirements": []
								}
							]
						}
						""".formatted(categories.get(tab).id().toString(), gx, gy));
				player.sendMessage(Text.literal("Copied research skeleton to clipboard"));
				return true;
			}
			
			if(button == 0){
				wasDragging = true;
				return true;
			}
		}
		
		return super.mouseClicked(mouseX, mouseY, button);
	}
	
	public boolean mouseReleased(double mouseX, double mouseY, int button){
		if(button == 0)
			wasDragging = false;
		return super.mouseReleased(mouseX, mouseY, button);
	}
	
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY){
		if(wasDragging){
			xPan += ((float)deltaX * ZOOM_MULTIPLIER) / zoom;
			yPan -= ((float)deltaY * ZOOM_MULTIPLIER) / zoom;
			xPan = clamp(xPan, -MAX_PAN, MAX_PAN);
			yPan = clamp(yPan, -MAX_PAN, MAX_PAN);
		}
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
	
	public boolean keyPressed(int keyCode, int scanCode, int modifiers){
		if(super.keyPressed(keyCode, scanCode, modifiers))
			return true;
		if(client.options.inventoryKey.matchesKey(keyCode, scanCode)){
			client.setScreen(null);
			return true;
		}
		if(keyCode == GLFW.GLFW_KEY_F3){
			debug ^= true;
			return true;
		}
		return false;
	}
	
	public boolean shouldPause(){
		return false;
	}
	
	public PageStyle style(Entry entry){
		// locked entries are always locked
		if(entry.meta().contains("locked"))
			return PageStyle.PENDING;
		// if the page is at full progress, it's complete.
		Researcher r = Researcher.from(player);
		if(r.entryStage(entry) >= entry.sections().size())
			return PageStyle.COMPLETE;
		// if its progress is greater than zero, then it's in progress.
		if(r.entryStage(entry) > 0)
			return PageStyle.IN_PROGRESS;
		// if it has no parents *and* the "root" tag, it's available to do and in progress.
		if(entry.meta().contains("root") && entry.parents().isEmpty())
			return PageStyle.IN_PROGRESS;
		// if it does not have the "hidden" tag:
		if(!entry.meta().contains("hidden")){
			List<PageStyle> parentStyles = entry.parents().stream().map(parent -> Pair.of(Research.getEntry(parent.id()), parent)).map(p -> parentStyle(p.getFirst(), p.getSecond())).toList();
			// if all of its parents are complete, it is available to do and in progress.
			if(parentStyles.stream().allMatch(PageStyle.COMPLETE::equals))
				return PageStyle.IN_PROGRESS;
			// if at least one of its parents are in progress/completed, it's pending.
			if(parentStyles.stream().anyMatch(other -> PageStyle.IN_PROGRESS.equals(other) || PageStyle.COMPLETE.equals(other)))
				return PageStyle.PENDING;
		}
		// otherwise, its invisible
		return PageStyle.NONE;
	}
	
	public PageStyle parentStyle(Entry entry, Parent parent){
		if(entry == null){
			Arcana.LOGGER.warn("Tried to get the stage of a parent entry that doesn't exist: {} (from {})", parent.id().toString(), parent.asString());
			return PageStyle.PENDING;
		}
		Researcher r = Researcher.from(player);
		// if the parent is greater than required, consider it complete
		if(parent.stage() == -1){
			if(r.entryStage(entry) >= entry.sections().size())
				return PageStyle.COMPLETE;
		}else if(r.entryStage(entry) >= parent.stage())
			return PageStyle.COMPLETE;
		// if its progress is greater than zero, then its in progress.
		if(r.entryStage(entry) > 0)
			return PageStyle.IN_PROGRESS;
		// if it has no parents *and* the "root" tag, its available to do and in progress.
		if(entry.meta().contains("root") && entry.parents().isEmpty())
			return PageStyle.IN_PROGRESS;
		// if it does not have the "hidden" tag:
		if(!entry.meta().contains("hidden")){
			List<PageStyle> parentStyles = entry.parents().stream().map(p -> Pair.of(Research.getEntry(p.id()), p)).map(p -> parentStyle(p.getFirst(), p.getSecond())).toList();
			// if all of its parents are complete, it is available to do and in progress.
			if(parentStyles.stream().allMatch(PageStyle.COMPLETE::equals))
				return PageStyle.IN_PROGRESS;
			// if at least one of its parents are in progress/completed, it's pending.
			if(parentStyles.stream().anyMatch(other -> PageStyle.IN_PROGRESS.equals(other) || PageStyle.COMPLETE.equals(other)))
				return PageStyle.PENDING;
		}
		// otherwise, its invisible
		return PageStyle.NONE;
	}
	
	
	private Vec2f baseUv(Entry entry){
		int u = 52, v = 52;
		
		if(entry.meta().contains("tmp_base"))
			return new Vec2f(29, 80);
		
		if(entry.meta().contains("purple_base"))
			v = 0;
		else if(entry.meta().contains("yellow_base"))
			v = 26;
		else if(entry.meta().contains("no_base"))
			v = 230;
		
		if(entry.meta().contains("round_base"))
			u = 26;
		else if(entry.meta().contains("square_base"))
			// reassigning default value
			//noinspection DataFlowIssue
			u = 52;
		else if(entry.meta().contains("hexagon_base"))
			u = 78;
		else if(entry.meta().contains("spiky_base"))
			u = 0;
		
		return new Vec2f(u, v);
	}
	
	public enum PageStyle{
		COMPLETE,
		IN_PROGRESS,
		PENDING,
		NONE
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
			super(x, y, 16, 16, Text.literal(""), button -> tab = categoryIdx);
			this.categoryIdx = categoryIdx;
			this.category = category;
			visible = true;
		}
		
		public void render(MatrixStack matrices, int mouseX, int mouseY, float delta){
			hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
			if(visible){
				int xOffset = categoryIdx == tab ? 6 : (hovered) ? 4 : 0;
				int renderX = x - xOffset;
				RenderSystem.setShaderTexture(0, texture);
				drawTexture(matrices, renderX - 11, y - 1, 0, 158, 34 - (6 - xOffset), 18);
				RenderHelper.renderIcon(matrices, category.icon(), renderX, y, getZOffset());
				int iconU = categoryIcons.getOrDefault(category, -1);
				if(iconU != -1){
					RenderSystem.setShaderTexture(0, ICONS_TEX);
					RenderHelper.drawTexture(matrices, renderX - 7, y, 0, iconU, 107, 9, 9, 1, 1, 1);
				}
			}
		}
		
		public void renderAfter(MatrixStack matrices, int mouseX, int mouseY){
			if(hovered && visible){
				if(!category.entries().isEmpty()){
					Researcher researcher = Researcher.from(player);
					int sum = 0;
					for(Entry entry : category.entries())
						sum += researcher.entryStage(entry) >= entry.sections().size() ? 1 : 0;
					int percent = (sum * 100) / category.entries().size();
					List<Text> lines = new ArrayList<>(2);
					lines.add(Text.translatable(
							"research.book.category_with_completion",
							Text.translatable(category.name()),
							Text.literal(String.valueOf(percent))));
					if(debug)
						lines.add(Text.literal(category.id().toString()).formatted(Formatting.DARK_GRAY));
					ResearchBookScreen.this.renderTooltip(matrices, lines, mouseX, mouseY);
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
					Researcher from = Researcher.from(player);
					List<Integer> pinned = from.getPinned().get(pin.entry().id());
					if(pinned != null){
						from.removePinned(pin.entry().id(), pin.stage());
						ArcanaClient.sendModifyPins(pin, false);
					}
					// and remove this button
					ResearchBookScreen.this.refreshPins();
				}else{
					Entry entry = pin.entry();
					if(Researcher.from(player).entryStage(entry) >= pin.stage()){
						ResearchEntryScreen in = new ResearchEntryScreen(entry, client.currentScreen);
						int stageIndex = in.indexOfStage(pin.stage());
						in.idx = stageIndex % 2 == 0 ? stageIndex : stageIndex - 1;
						client.setScreen(in);
						unreadEntries.remove(entry);
						unreadAddendaEntries.remove(entry);
					}
				}
			});
			this.pin = pin;
		}
		
		public void render(MatrixStack matrices, int mouseX, int mouseY, float delta){
			hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
			if(visible){
				int xOffset = hovered ? 3 : 0;
				RenderSystem.setShaderTexture(0, texture);
				drawTexture(matrices, x - 2, y - 1, 6 - xOffset, 140, 34 - (6 - xOffset), 18);
				RenderHelper.renderIcon(matrices, pin.icon(), x + xOffset, y - 1, 0);
			}
		}
		
		public void renderAfter(MatrixStack matrices, int mouseX, int mouseY){
			if(pin.icon().stack() != null)
				if(hovered && visible){
					var stack = pin.icon().stack();
					List<Text> tooltips = new ArrayList<>(getTooltipFromItem(stack));
					tooltips.add(Text.translatable("research.entry.unpin").formatted(Formatting.AQUA));
					ResearchBookScreen.this.renderTooltip(matrices, tooltips, stack.getTooltipData(), mouseX, mouseY);
				}
		}
	}
}