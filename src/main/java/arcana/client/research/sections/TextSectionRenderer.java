package arcana.client.research.sections;

import arcana.Arcana;
import arcana.client.research.EntrySectionRenderer;
import arcana.client.research.TextFormatter;
import arcana.client.research.TextFormatter.Paragraph;
import arcana.research.sections.TextSection;
import arcana.screens.ResearchEntryScreen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static arcana.screens.ResearchEntryScreen.*;

public class TextSectionRenderer implements EntrySectionRenderer<TextSection>{
	
	private static final Map<TextSection, List<Paragraph>> TEXT_CACHE = new HashMap<>();
	private static final int PARAGRAPH_SPACING = 6;
	
	public void render(DrawContext ctx, TextSection section, int pageIdx, int screenWidth, int screenHeight, int mouseX, int mouseY, boolean right){
		MatrixStack matrices = ctx.getMatrices();
		List<Paragraph> paragraphs = format(section);
		matrices.push();
		matrices.scale(scaling(), scaling(), 1);
		int x = right ? pageX + rightXOffset : pageX;
		float lineX = ((int)((screenWidth - 256) / 2f) + x) / scaling();
		float curY = ((int)((screenHeight - bgHeight) / 2f) + pageY - heightOffset) / scaling();
		// pick which paragraphs to display
		int curPage = 0;
		float curPageHeight = 0;
		for(int i = 0; i < paragraphs.size(); i++){
			Paragraph paragraph = paragraphs.get(i);
			if((curPageHeight + paragraph.getHeight()) < pageHeight()){
				if(curPage == pageIdx){
					paragraph.render(ctx, (int)lineX, (int)curY, scaling());
					curY += paragraph.getHeight() + 6;
				}
				curPageHeight += paragraph.getHeight() + PARAGRAPH_SPACING;
			}else{
				curPage++;
				curPageHeight = 0;
				if(paragraph.getHeight() < pageHeight())
					// make sure this span gets added to the next line instead
					i--;
				else if(curPage == pageIdx){
					paragraph.render(ctx, (int)lineX, (int)curY, scaling());
					curY += paragraph.getHeight() + 6;
				}
			}
			
		}
		matrices.pop();
	}
	
	public void renderAfter(DrawContext ctx, TextSection section, int pageIdx, int screenWidth, int screenHeight, int mouseX, int mouseY, boolean right){
		// no-op
		// TODO: tooltip for aspect spans?
	}
	
	public int span(TextSection section, PlayerEntity player){
		List<Paragraph> paragraphs = format(section);
		int curPage = 1;
		float curPageHeight = 0;
		for(int i = 0; i < paragraphs.size(); i++){
			Paragraph paragraph = paragraphs.get(i);
			if((curPageHeight + paragraph.getHeight()) < pageHeight())
				curPageHeight += paragraph.getHeight() + PARAGRAPH_SPACING;
			else{
				curPage++;
				curPageHeight = 0;
				if(paragraph.getHeight() < pageHeight())
					// make sure this span gets added to the next line instead
					i--;
			}
		}
		return curPage;
	}
	
	private static String getTranslatedText(TextSection section){
		return TextFormatter.process(I18n.translate(section.getText()), section).replace("{~sep}", "\n{~sep}\n");
	}
	
	private int pageHeight(){
		return (int)((ResearchEntryScreen.pageHeight / scaling()) + 1);
	}
	
	private static float scaling(){
		return Arcana.CONFIG.textScaling;
	}
	
	public static List<Paragraph> format(TextSection section){
		return TEXT_CACHE.computeIfAbsent(section, s -> TextFormatter.compile(getTranslatedText(s), s));
	}
	
	public static void clearCache(){
		TEXT_CACHE.clear();
	}
}