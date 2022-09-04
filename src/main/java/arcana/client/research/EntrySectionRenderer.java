package arcana.client.research;

import arcana.aspects.Aspect;
import arcana.client.research.sections.*;
import arcana.research.EntrySection;
import arcana.research.Research;
import arcana.research.sections.*;
import arcana.screens.ResearchEntryScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static arcana.screens.ResearchBookScreen.bookPrefix;

public interface EntrySectionRenderer<T extends EntrySection>{

	// registry
	
	Map<Identifier, EntrySectionRenderer<?>> renderers = new LinkedHashMap<>();
	
	static void setup(){
		renderers.put(TextSection.TYPE, new TextSectionRenderer());
		renderers.put(ImageSection.TYPE, new ImageSectionRenderer());
		renderers.put(CraftingRecipeSection.TYPE, new CraftingRecipeSectionRenderer());
		renderers.put(ArcaneCraftingRecipeSection.TYPE, new ArcaneCraftingRecipeSectionRenderer());
		renderers.put(CookingRecipeSection.TYPE, new CookingRecipeSectionRenderer());
		renderers.put(AlchemyRecipeSection.TYPE, new AlchemyRecipeSectionRenderer());
		renderers.put(WandInteractionSection.TYPE, new WandInteractionSectionRenderer());
		renderers.put(AspectCombosSection.TYPE, new AspectCombosSectionRenderer());
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
	
	@NotNull
	default MinecraftClient client(){
		return MinecraftClient.getInstance();
	}
	
	default TextRenderer textRenderer(){
		return client().textRenderer;
	}
	
	default ResearchEntryScreen screen(){
		return (ResearchEntryScreen)client().currentScreen;
	}
	
	default boolean onClick(T section, int pageIdx, int screenWidth, int screenHeight, double mouseX, double mouseY, boolean right){
		return false;
	}
	
	static Identifier overlayTexture(EntrySection section){
		var bookId = Research.getEntry(section.getIn()).category().book().id();
		return new Identifier(bookId.getNamespace(), bookPrefix + bookId.getPath() + ResearchEntryScreen.overlaySuffix);
	}
	
	default void tooltipArea(MatrixStack matrices, ItemStack stack, int mouseX, int mouseY, int areaX, int areaY){
		if(mouseX >= areaX && mouseX < areaX + 16 && mouseY >= areaY && mouseY < areaY + 16)
			drawTooltip(matrices, stack, mouseX, mouseY);
	}
	
	default void tooltipArea(MatrixStack matrices, Aspect aspect, int mouseX, int mouseY, int areaX, int areaY){
		if(mouseX >= areaX && mouseX < areaX + 16 && mouseY >= areaY && mouseY < areaY + 16)
			drawTooltip(matrices, aspect, mouseX, mouseY);
	}
	
	default void drawTooltip(MatrixStack matrices, Aspect aspect, int mouseX, int mouseY){
		drawTooltip(matrices, List.of(aspect.name()), mouseX, mouseY);
	}
	
	default void drawTooltip(MatrixStack matrices, ItemStack stack, int mouseX, int mouseY){
		screen().renderTooltip(matrices, screen().getTooltipFromItem(stack), stack.getTooltipData(), mouseX, mouseY);
	}
	
	default void drawTooltip(MatrixStack matrices, List<Text> tooltip, int mouseX, int mouseY){
		screen().renderTooltip(matrices, tooltip, mouseX, mouseY);
	}
}