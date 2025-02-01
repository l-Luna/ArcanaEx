package arcana.datagen;

import arcana.client.research.CustomTextStyle;
import arcana.client.research.RequirementRenderer;
import arcana.client.research.TextFormatter;
import arcana.client.research.sections.TextSectionRenderer;
import arcana.research.*;
import arcana.research.puzzles.Fieldwork;
import arcana.research.requirements.PuzzleRequirement;
import arcana.research.requirements.PuzzlesCompletedRequirement;
import arcana.research.requirements.XpRequirement;
import arcana.research.sections.TextSection;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.data.DataWriter;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// this isn't a data generator, but it's put in this package for organizational reasons
// triggered by the `/arcana-research generate-docs` command

public class ArcanaDocsProvider{
	
	public static void booksToDocs(){
		StringBuilder there = new StringBuilder();
		there.append("<h1>Arcana Research</h1>\n");
		for(Book book : Research.books.values())
			bookToDocs(book, there);
		try{
			Files.writeString(Path.of("./docs/generated.html"), there, StandardOpenOption.CREATE);
		}catch(IOException e){
			throw new UncheckedIOException(e);
		}
	}
	
	private static void bookToDocs(Book book, StringBuilder where){
		where.append("<h2>").append(book.id()).append("</h2>\n");
		for(Category category : book.categories()){
			where.append("<h3>").append(I18n.translate(category.name())).append("</h3>\n");
			for(Entry entry : category.entries()){
				where.append("<h4>").append(I18n.translate(entry.name())).append("</h4>\n");
				entryToDocs(entry, where);
			}
		}
	}
	
	private static void entryToDocs(Entry entry, StringBuilder where){
		List<EntrySection> sections = entry.sections();
		for(int i = 0; i < sections.size(); i++){
			EntrySection section = sections.get(i);
			sectionToDocs(section, where);
			if(i != sections.size() - 1){
				where.append("<p><img class=\"requirements-icon\" src=\"then.png\" alt=\"requirements\" >");
				for(Requirement requirement : section.getRequirements())
					requirementToDocs(requirement, where);
				where.append("</p>");
			}
		}
	}
	
	private static void sectionToDocs(EntrySection section, StringBuilder where){
		if(section instanceof TextSection ts){
			for(TextFormatter.Paragraph paragraph : TextSectionRenderer.format(ts)){
				paragraphToDocs(paragraph, where);
				where.append("\n\n");
			}
		}else
			where.append("Oops: ").append(section.getClass().getName());
	}
	
	private static void paragraphToDocs(TextFormatter.Paragraph paragraph, StringBuilder where){
		if(paragraph instanceof TextFormatter.SpanParagraph sp){
			where.append("<p");
			if(sp.centred)
				where.append(" class=\"centred\"");
			where.append(">");
			for(TextFormatter.Span span : sp.spans){
				spanToDocs(span, where);
				where.append(" ");
			}
			where.append("</p>");
		}
		if(paragraph instanceof TextFormatter.SeparatorParagraph)
			where.append("<p><img class=\"separator-paragraph\" src=\"separator.png\" alt=\"paragraph separator\" /></p>");
	}
	
	private static void spanToDocs(TextFormatter.Span span, StringBuilder where){
		if(span instanceof TextFormatter.TextSpan ts){
			where.append("<span class=\"").append(customStyleToClasses(ts.renderStyle)).append("\">");
			where.append(ts.text);
			where.append("</span>");
		}
		if(span instanceof TextFormatter.AspectSpan as)
			where.append("<img class=\"aspect-icon\" src=\"icons/aspects/%s.png\" alt=\"%s\" />".formatted(as.aspect.id().getPath(), as.aspect.id()));
		if(span instanceof TextFormatter.MultiSpan ms)
			for(TextFormatter.Span inner : ms.spans)
				spanToDocs(inner, where);
	}
	
	private static <Req extends Requirement> void requirementToDocs(Req requirement, StringBuilder where){
		RequirementRenderer<Req> renderer = RequirementRenderer.get(requirement);
		where.append("<span>");
		if(requirement instanceof PuzzleRequirement pr){
			Puzzle p = Research.getPuzzle(pr.getPuzzleId());
			if(p instanceof Fieldwork)
				where.append("<img class=\"requirement-icon\" src=\"icons/fieldwork.png\" alt=\"fieldwork icon\" />");
			else
				where.append("<img class=\"requirement-icon\" src=\"icons/research_notes.png\" alt=\"research notes icon\" />");
		}
		if(requirement instanceof PuzzlesCompletedRequirement)
			where.append("<img class=\"requirement-icon\" src=\"icons/complete_research_notes.png\" alt=\"completed research icon\" />");
		if(requirement instanceof XpRequirement)
			where.append("<img class=\"requirement-icon\" src=\"icons/xp.png\" alt=\"xp icon\" />");
		where.append("<span class=\"requirement-tooltip\">");
		textsToDocs(renderer.tooltip(requirement, 0), where);
		where.append("</span>");
		where.append("</span>");
	}
	
	private static void textsToDocs(List<? extends Text> texts, StringBuilder where){
		int size = texts.size();
		if(size > 0){
			textToDocs(texts.get(0), where);
			if(size > 1){
				where.append(" (");
				for(int i = 1; i < size; i++){
					textToDocs(texts.get(i), where);
					if(i != size - 1)
						where.append(", ");
				}
				where.append(")");
			}
		}
	}
	
	private static void textToDocs(Text text, StringBuilder where){
		text.visit((style, asString) -> {
			where.append("<span class=\"").append(styleToClasses(style)).append("\">");
			where.append(asString);
			where.append("</span>");
			return Optional.empty();
		}, Style.EMPTY);
	}
	
	private static String styleToClasses(Style style){
		List<String> classes = new ArrayList<>();
		if(style.isBold())
			classes.add("bold");
		if(style.isItalic())
			classes.add("italic");
		if(style.isUnderlined())
			classes.add("underlined");
		if(style.isStrikethrough())
			classes.add("strikethrough");
		return String.join(" ", classes);
	}
	
	private static String customStyleToClasses(CustomTextStyle style){
		List<String> classes = new ArrayList<>();
		if(style.isBold())
			classes.add("bold");
		if(style.isItalics())
			classes.add("italic");
		if(style.isUnderline())
			classes.add("underlined");
		if(style.isStrikethrough())
			classes.add("strikethrough");
		if(style.isWavy())
			classes.add("wavy");
		return String.join(" ", classes);
	}
	
	public void run(DataWriter writer) throws IOException{
		booksToDocs();
	}
	
	public String getName(){
		return "Arcana Web Docs";
	}
}