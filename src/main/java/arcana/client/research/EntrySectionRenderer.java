package arcana.client.research;

import arcana.client.research.sections.ImageSectionRenderer;
import arcana.client.research.sections.TextSectionRenderer;
import arcana.research.EntrySection;
import arcana.research.sections.ImageSection;
import arcana.research.sections.TextSection;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;

public interface EntrySectionRenderer<T extends EntrySection>{

	// registry
	
	Map<Identifier, EntrySectionRenderer<?>> renderers = new LinkedHashMap<>();
	
	static void setup(){
		renderers.put(TextSection.TYPE, new TextSectionRenderer());
		renderers.put(ImageSection.TYPE, new ImageSectionRenderer());
	}
	
	@SuppressWarnings("unchecked")
	static <T extends EntrySection> EntrySectionRenderer<T> get(Identifier type){
		return (EntrySectionRenderer<T>)renderers.get(type);
	}
	
	@SuppressWarnings("unchecked")
	static <T extends EntrySection> EntrySectionRenderer<T> get(T section){
		return (EntrySectionRenderer<T>)renderers.get(section.type());
	}
	
	//
	
	void render(MatrixStack matrices, T section, int pageIdx, int screenWidth, int screenHeight, int mouseX, int mouseY, boolean right);
	
	void renderAfter(MatrixStack matrices, T section, int pageIdx, int screenWidth, int screenHeight, int mouseX, int mouseY, boolean right);
	
	int span(T section, PlayerEntity player);
	
	// onClick...
	
	default MinecraftClient client(){
		return MinecraftClient.getInstance();
	}
	
	default TextRenderer textRenderer(){
		return client().textRenderer;
	}
	
	default boolean onClick(T section, int pageIdx, int screenWidth, int screenHeight, double mouseX, double mouseY, boolean right){
		return false;
	}
}