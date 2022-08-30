package arcana.screens;

import arcana.client.RenderHelper;
import arcana.client.research.EntrySectionRenderer;
import arcana.client.research.RequirementRenderer;
import arcana.research.Entry;
import arcana.research.EntrySection;
import arcana.research.Requirement;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

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
	
	public Identifier bg;
	private Entry entry;
	private int idx;
	
	private @Nullable Screen parent;
	
	private ButtonWidget left, right, cont/*inue*/, ret/*urn*/;
	
	protected ResearchEntryScreen(Entry entry, @Nullable Screen parent){
		super(Text.literal(""));
		this.entry = entry;
		this.parent = parent;
		Identifier bookKey = entry.in().in().id();
		bg = new Identifier(bookKey.getNamespace(), bookPrefix + bookKey.getPath() + suffix);
	}
	
	protected void init(){
		int x = width / 2 - 6;
		int y = (height - 181) / 2 + 190 + heightOffset;
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
		ret = addDrawableChild(new ReturnToBookButton(width / 2 - 7, (height - 181) / 2 - 26, b -> MinecraftClient.getInstance().setScreen(parent)));
		// TODO: continue button
		// pins...
		updateButtons();
	}
	
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta){
		renderBackground(matrices);
		super.render(matrices, mouseX, mouseY, delta);
		RenderSystem.setShaderTexture(0, bg);
		drawTexture(matrices, (width - 256) / 2, (height - 181) / 2 + heightOffset, 0, 0, 256, 181);
		
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
		/*Researcher r = Researcher.from(MinecraftClient.getInstance().player);
		if(r.entryStage(entry) < entry.sections().size() && entry.sections().get(r.entryStage(entry)).getRequirements().size() > 0){
			List<Requirement> requirements = entry.sections().get(r.entryStage(entry)).getRequirements();
			final int y = (height - 181) / 2 + 180;
			final int reqWidth = 20;
			final int baseX = (width / 2) - (reqWidth * requirements.size() / 2);
			for(int i = 0, size = requirements.size(); i < size; i++){
				Requirement requirement = requirements.get(i);
				renderer(requirement).render(stack, baseX + i * reqWidth + 2, y, requirement, getMinecraft().player.ticksExisted, partialTicks, getMinecraft().player);
				renderAmount(stack, requirement, baseX + i * reqWidth + 2, y, requirement.getAmount(), requirement.satisfied(getMinecraft().player));
			}
			// Show tooltips
			for(int i = 0, size = requirements.size(); i < size; i++)
				if(mouseX >= 20 * i + baseX + 2 && mouseX <= 20 * i + baseX + 18 && mouseY >= y && mouseY <= y + 18){
					List<Component> tooltip = renderer(requirements.get(i)).tooltip(requirements.get(i), getMinecraft().player);
					List<String> lines = new ArrayList<>();
					for(int i1 = 0, tooltipSize = tooltip.size(); i1 < tooltipSize; i1++){
						String s = tooltip.get(i1).getString();
						s = (i1 == 0 ? ChatFormatting.WHITE : ChatFormatting.GRAY) + s;
						lines.add(s);
					}
					GuiUtils.drawHoveringText(stack, lines.stream().map(Component::literal).collect(Collectors.toList()), mouseX, mouseY, width, height, -1, getMinecraft().fontRenderer);
					break;
				}
		}*/
		
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
	
	private void updateButtons(){
		left.visible = canTurnLeft();
		right.visible = canTurnRight();
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
		return true;
		// cant use getMinecraft here because this is called from ResearchBookScreen before this is set
		//return Researcher.from(MinecraftClient.getInstance().player).entryStage(entry) >= entry.sections().indexOf(section);
	}
	
	private <T extends Requirement> RequirementRenderer<T> renderer(T requirement){
		return RequirementRenderer.get(requirement);
	}
	
	private void renderAmount(MatrixStack stack, Requirement requirement, int x, int y, int amount, boolean complete){
		/*if(renderer(requirement).shouldDrawTickOrCross(requirement, amount)){
			//display tick or cross
			getMinecraft().getTextureManager().bindTexture(bg);
			RenderSystem.color4f(1f, 1f, 1f, 1f);
			// ensure it renders over items
			setBlitOffset(300);
			drawTexturedModalRect(stack, x + 10, y + 9, complete ? 0 : 8, 247, 8, 9);
			setBlitOffset(0);
		}else{
			String s = String.valueOf(amount);
			IRenderTypeBuffer.Impl buffer = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());
			Matrix4f matrix = TransformationMatrix.identity().getMatrix();
			matrix.translate(new Vector3f(0, 0, 300));
			getMinecraft().fontRenderer.renderString(s, (float)(x + 17 - getMinecraft().fontRenderer.getStringWidth(s)), (float)(y + 9), complete ? 0xaaffaa : 0xffaaaa, true, matrix, buffer, false, 0, 15728880);
			buffer.finish();
		}*/
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