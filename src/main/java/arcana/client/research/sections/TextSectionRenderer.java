package arcana.client.research.sections;

import arcana.client.research.EntrySectionRenderer;
import arcana.client.research.TextFormatter;
import arcana.client.research.TextFormatter.Paragraph;
import arcana.research.sections.TextSection;
import arcana.screens.ResearchEntryScreen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static arcana.screens.ResearchEntryScreen.heightOffset;
import static arcana.screens.ResearchEntryScreen.textScaling;

public class TextSectionRenderer implements EntrySectionRenderer<TextSection>{
	
	private final int pageHeight = (int)((ResearchEntryScreen.pageHeight / textScaling) + 1);
	
	private static final Map<TextSection, List<Paragraph>> textCache = new HashMap<>();
	private static final int paragraphSpacing = 6;
	
	public void render(MatrixStack matrices, TextSection section, int pageIdx, int screenWidth, int screenHeight, int mouseX, int mouseY, boolean right){
		List<Paragraph> paragraphs = format(section);
		matrices.push();
		matrices.scale(textScaling, textScaling, 1);
		int x = right ? ResearchEntryScreen.pageX + ResearchEntryScreen.rightXOffset : ResearchEntryScreen.pageX;
		float lineX = ((int)((screenWidth - 256) / 2f) + x) / textScaling;
		float curY = ((int)((screenHeight - 181) / 2f) + ResearchEntryScreen.pageY + heightOffset) / textScaling;
		// pick which paragraphs to display
		int curPage = 0;
		float curPageHeight = 0;
		for(int i = 0; i < paragraphs.size(); i++){
			Paragraph paragraph = paragraphs.get(i);
			if((curPageHeight + paragraph.getHeight()) < pageHeight){
				if(curPage == pageIdx){
					paragraph.render(matrices, (int)lineX, (int)curY, textScaling);
					curY += paragraph.getHeight() + 6;
				}
				curPageHeight += paragraph.getHeight() + paragraphSpacing;
			}else{
				curPage++;
				curPageHeight = 0;
				if(paragraph.getHeight() < pageHeight)
					// make sure this span gets added to the next line instead
					i--;
				else if(curPage == pageIdx){
					paragraph.render(matrices, (int)lineX, (int)curY, textScaling);
					curY += paragraph.getHeight() + 6;
				}
			}
			
		}
		matrices.pop();
	}
	
	public void renderAfter(MatrixStack matrices, TextSection section, int pageIdx, int screenWidth, int screenHeight, int mouseX, int mouseY, boolean right){
		// no-op
		// TODO: tooltip for aspect spans?
	}
	
	public int span(TextSection section, PlayerEntity player){
		List<Paragraph> paragraphs = format(section);
		int curPage = 1;
		float curPageHeight = 0;
		for(int i = 0; i < paragraphs.size(); i++){
			Paragraph paragraph = paragraphs.get(i);
			if((curPageHeight + paragraph.getHeight()) < pageHeight)
				curPageHeight += paragraph.getHeight() + paragraphSpacing;
			else{
				curPage++;
				curPageHeight = 0;
				if(paragraph.getHeight() < pageHeight)
					// make sure this span gets added to the next line instead
					i--;
			}
		}
		return curPage;
	}
	
	private static List<Paragraph> format(TextSection section){
		return textCache.computeIfAbsent(section, s -> TextFormatter.compile(getTranslatedText(s), s));
	}
	
	private static String getTranslatedText(TextSection section){
		return TextFormatter.process(I18n.translate(section.getText()), section).replace("{~sep}", "\n{~sep}\n");
	}
	
	public static void clearCache(){
		textCache.clear();
	}
	
	// TODO: config for text scale
}