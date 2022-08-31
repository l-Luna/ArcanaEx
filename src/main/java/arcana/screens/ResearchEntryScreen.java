package arcana.screens;

import arcana.client.ArcanaClient;
import arcana.client.RenderHelper;
import arcana.client.research.EntrySectionRenderer;
import arcana.client.research.RequirementRenderer;
import arcana.components.Researcher;
import arcana.research.Entry;
import arcana.research.EntrySection;
import arcana.research.Requirement;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static arcana.screens.ResearchBookScreen.bookPrefix;

public class ResearchEntryScreen extends Screen{
	
	private static final String suffix = "_entry.png";
	public static final String overlaySuffix = "_entry_overlay.png";
	
	public static final int pageX = 17;
	public static final int pageY = 10;
	public static final int pageWidth = 105;
	public static final int pageHeight = 155;
	public static final int rightXOffset = 119;
	public static final int heightOffset = 10;
	public static final int bgHeight = 181;
	
	public static final float textScaling = .7f;
	
	public final Identifier bg;
	private final Entry entry;
	private int idx;
	
	private final @Nullable Screen parent;
	
	private ButtonWidget left, right, cont/*inue*/;
	
	protected ResearchEntryScreen(Entry entry, @Nullable Screen parent){
		super(Text.literal(""));
		this.entry = entry;
		this.parent = parent;
		Identifier bookKey = entry.in().in().id();
		bg = new Identifier(bookKey.getNamespace(), bookPrefix + bookKey.getPath() + suffix);
	}
	
	protected void init(){
		int x = width / 2 - 6;
		int y = (height - bgHeight) / 2 + 190 - heightOffset;
		int dist = 127;
		left = addDrawableChild(new ChangePageButton(x - dist, y, false, button -> {
			if(canTurnLeft())
				idx -= 2;
			updateButtons();
		}));
		right = addDrawableChild(new ChangePageButton(x + dist, y, true, button -> {
			if(canTurnRight())
				idx += 2;
			updateButtons();
		}));
		var mc = MinecraftClient.getInstance();
		addDrawableChild(new ReturnToBookButton(width / 2 - 7, (height - 181) / 2 - 26, b -> mc.setScreen(parent)));
		String text = I18n.translate("research.entry.continue");
		var w = mc.textRenderer.getWidth(text);
		cont = addDrawableChild(new ButtonWidget(x - w / 2 + 2, y + 15, w + 10, 16, Text.literal(text), button -> {
			ArcanaClient.sendTryAdvance(entry);
		}){
			public void render(MatrixStack matrices, int mouseX, int mouseY, float delta){
				var player = mc.player;
				var researcher = Researcher.from(player);
				active = researcher.entryStage(entry) < entry.sections().size() && entry.sections().get(researcher.entryStage(entry)).getRequirements().stream().allMatch(it -> it.satisfiedBy(player));
				super.render(matrices, mouseX, mouseY, delta);
			}
		});
		// pins...
		updateButtons();
	}
	
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta){
		renderBackground(matrices);
		super.render(matrices, mouseX, mouseY, delta);
		RenderSystem.setShaderTexture(0, bg);
		drawTexture(matrices, (width - 256) / 2, (height - 181) / 2 - heightOffset, 0, 0, 256, 181);
		
		// Main rendering
		if(totalLength() > idx){
			EntrySection section = getSectionAtIndex(idx);
			if(section != null)
				EntrySectionRenderer.get(section).render(matrices, section, sectionIndex(idx), width, height, mouseX, mouseY, false);
		}
		if(totalLength() > idx + 1){
			EntrySection section = getSectionAtIndex(idx + 1);
			if(section != null)
				EntrySectionRenderer.get(section).render(matrices, section, sectionIndex(idx + 1), width, height, mouseX, mouseY, true);
		}
		
		// Requirements
		var player = MinecraftClient.getInstance().player;
		Researcher r = Researcher.from(player);
		if(r.entryStage(entry) < entry.sections().size() && entry.sections().get(r.entryStage(entry)).getRequirements().size() > 0){
			List<Requirement> requirements = entry.sections().get(r.entryStage(entry)).getRequirements();
			final int y = (height - bgHeight) / 2 + 175;
			final int reqWidth = 20;
			final int baseX = (width / 2) - (reqWidth * requirements.size() / 2);
			for(int i = 0, size = requirements.size(); i < size; i++){
				Requirement requirement = requirements.get(i);
				renderer(requirement).render(matrices, baseX + i * reqWidth + 2, y, requirement, (int)player.world.getTime(), delta);
				renderAmount(matrices, requirement, baseX + i * reqWidth + 2, y, requirement.getAmount(), requirement.satisfiedBy(player));
			}
			// Show tooltips
			for(int i = 0, size = requirements.size(); i < size; i++)
				if(mouseX >= 20 * i + baseX + 2 && mouseX <= 20 * i + baseX + 18 && mouseY >= y && mouseY <= y + 18){
					List<Text> tooltip = renderer(requirements.get(i)).tooltip(requirements.get(i), (int)player.world.getTime());
					List<Text> lines = new ArrayList<>();
					for(int tIdx = 0, tooltipSize = tooltip.size(); tIdx < tooltipSize; tIdx++){
						Text s = tooltip.get(tIdx);
						s.getWithStyle(s.getStyle().withFormatting(tIdx == 0 ? Formatting.WHITE : Formatting.GRAY));
						lines.add(s);
					}
					renderTooltip(matrices, lines, mouseX, mouseY);
					break;
				}
		}
		
		// After-renders (such as tooltips)
		if(totalLength() > idx){
			EntrySection section = getSectionAtIndex(idx);
			if(section != null)
				EntrySectionRenderer.get(section).renderAfter(matrices, section, sectionIndex(idx), width, height, mouseX, mouseY, false);
		}
		if(totalLength() > idx + 1){
			EntrySection section = getSectionAtIndex(idx + 1);
			if(section != null)
				EntrySectionRenderer.get(section).renderAfter(matrices, section, sectionIndex(idx + 1), width, height, mouseX, mouseY, true);
		}
	}
	
	public void updateButtons(){
		left.visible = canTurnLeft();
		right.visible = canTurnRight();
		Researcher researcher = Researcher.from(MinecraftClient.getInstance().player);
		cont.visible = researcher.entryStage(entry) < getVisibleSections().size();
		// pins...
	}
	
	private boolean canTurnRight(){
		return idx < totalLength() - 2;
	}
	
	private boolean canTurnLeft(){
		return idx > 0;
	}
	
	private int totalLength(){
		return entry.sections().stream().filter(this::visible).mapToInt(this::span).sum();
	}
	
	// What entry we're looking at
	private EntrySection getSectionAtIndex(int index){
		if(index == 0)
			return entry.sections().get(0);
		int cur = 0;
		for(EntrySection section : getVisibleSections()){
			if(cur <= index && cur + span(section) > index)
				return section;
			cur += span(section);
		}
		return null;
	}
	
	// How far along in the entry we are
	private int sectionIndex(int index){
		int cur = 0;
		for(EntrySection section : getVisibleSections()){
			if(cur <= index && cur + span(section) > index)
				return index - cur;
			cur += span(section);
		}
		return 0; // throw/show an error
	}
	
	// Index of the given stage
	@SuppressWarnings("unused") // used by pins
	int indexOfStage(int stage){
		int cur = 0;
		List<EntrySection> sections = getVisibleSections();
		for(int i = 0, size = sections.size(); i < size; i++){
			EntrySection section = sections.get(i);
			if(i == stage)
				return cur;
			cur += span(section);
		}
		return 0; // throw/show an error
	}
	
	private List<EntrySection> getVisibleSections(){
		return entry.sections().stream().filter(this::visible).collect(Collectors.toList());
	}
	
	private boolean visible(EntrySection section){
		return  Researcher.from(MinecraftClient.getInstance().player).entryStage(entry) >= entry.sections().indexOf(section);
	}
	
	private <T extends Requirement> RequirementRenderer<T> renderer(T requirement){
		return RequirementRenderer.get(requirement);
	}
	
	private void renderAmount(MatrixStack stack, Requirement requirement, int x, int y, int amount, boolean complete){
		if(renderer(requirement).shouldDrawTickOrCross(requirement, amount)){
			//display tick or cross
			RenderSystem.setShaderTexture(0, bg);
			// ensure it renders over items
			setZOffset(300);
			drawTexture(stack, x + 10, y + 8, complete ? 0 : 8, 247, 8, 9);
			setZOffset(0);
		}else{
			String s = String.valueOf(amount);
			var text = MinecraftClient.getInstance().textRenderer;
			stack.push();
			stack.translate(0, 0, 300);
			text.draw(stack, s, x + 17 - text.getWidth(s), y + 9, complete ? 0xAAFFAA : 0xEE9999);
			stack.pop();
		}
	}
	
	private int span(EntrySection section){
		return EntrySectionRenderer.get(section).span(section, MinecraftClient.getInstance().player);
	}
	
	public boolean shouldPause(){
		return false;
	}
	
	private class ChangePageButton extends ButtonWidget{
		
		private final boolean right;
		
		public ChangePageButton(int x, int y, boolean right, PressAction onPress){
			super(x, y, 12, 6, Text.literal(""), onPress);
			this.right = right;
		}
		
		public void render(MatrixStack matrices, int mouseX, int mouseY, float delta){
			if(visible){
				hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
				float mult = hovered ? 1f : 0.5f;
				int texX = right ? 12 : 0;
				int texY = 185;
				RenderSystem.setShaderTexture(0, bg);
				RenderHelper.drawTexture(matrices, x, y, getZOffset(), texX, texY, width, height, mult, mult, mult);
			}
		}
	}
	
	private class ReturnToBookButton extends ButtonWidget{
		
		public ReturnToBookButton(int x, int y, PressAction onPress){
			super(x, y, 15, 8, Text.literal(""), onPress);
		}
		
		public void render(MatrixStack matrices, int mouseX, int mouseY, float delta){
			if(visible){
				hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
				float mult = hovered ? 1f : 0.5f;
				int texX = 41;
				int texY = 204;
				RenderSystem.setShaderTexture(0, bg);
				RenderHelper.drawTexture(matrices, x, y, getZOffset(), texX, texY, width, height, mult, mult, mult);
			}
		}
	}
	
	// class PinButton extends ButtonWidget{ ... }
}