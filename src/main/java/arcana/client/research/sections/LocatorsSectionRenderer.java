package arcana.client.research.sections;

import arcana.client.research.EntrySectionRenderer;
import arcana.items.foci.follow_me.*;
import arcana.research.sections.LocatorsSection;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.Structure;

import java.util.Optional;

import static arcana.Arcana.arcId;
import static arcana.client.research.sections.AbstractRecipeSectionRenderer.displayIdx;
import static arcana.screens.ResearchEntryScreen.*;

public class LocatorsSectionRenderer implements EntrySectionRenderer<LocatorsSection>{

	private static final Identifier TEXTURE = arcId("textures/research/follow_me_targets.png");
	
	public void render(DrawContext ctx, LocatorsSection section, int pageIdx, int screenWidth, int screenHeight, int mouseX, int mouseY, boolean right){
		int x = (right ? pageX + rightXOffset : pageX) + (screenWidth - 256) / 2 + 4;
		int y = pageY + (screenHeight - bgHeight) / 2 + 10 - heightOffset;
		
		getLocators(section).ifPresent(locators -> {
			for(int i = pageIdx * 5; i < locators.size() && i < (pageIdx + 1) * 5; i++){
				Locator locator = locators.get(i).value();
				ItemStack inputStack = AbstractRecipeSectionRenderer.displayStack(locator.input().getMatchingStacks());
				int dIdx = i - pageIdx * 5;
				int v = switch(locator.destination()){
					case StructureDestination __ -> 22;
					case BiomeDestination __ -> 43;
					case NodeDestination __ -> 64;
					case ExplorerMapTargetDestination __ -> 85;
					default -> 1;
				};
				ctx.drawTexture(TEXTURE, x - 4, y + 30 * dIdx - 2, 0, 0, v, 105, 20, 256, 256);
				int itemX = x - 2, itemY = y + 30 * dIdx;
				ctx.drawItem(inputStack, itemX, itemY);
				if(!locator.consumed()){
					//ctx.drawItemInSlot(textRenderer(), inputStack, x - 2, y + 30 * dIdx, "x");
					String lbl = "*";
					ctx.drawText(textRenderer(), lbl, itemX + 17 + 1 - textRenderer().getWidth(lbl), itemY + 12 + 1, 0xAAAAAA, false);
					ctx.drawText(textRenderer(), lbl, itemX + 17 - textRenderer().getWidth(lbl), itemY + 12, 0, false);
				}
				Text text = Text.translatable(switch(locator.destination()){
					case StructureDestination(RegistryEntryList.Named<Structure> structure) -> structure.getTag().id().toTranslationKey("tag.structure");
					case StructureDestination(RegistryEntryList<Structure> structure) -> structure.get(displayIdx(structure.size())).getKey().get().getValue().toTranslationKey("structure");
					case BiomeDestination(RegistryEntryList.Named<Biome> biomes) -> biomes.getTag().id().toTranslationKey("tag.biome");
					case BiomeDestination(RegistryEntryList<Biome> biomes) -> biomes.get(displayIdx(biomes.size())).getKey().get().getValue().toTranslationKey("biome");
					case NodeDestination(boolean onlyNew) when onlyNew -> "research.locators.new_node";
					case NodeDestination __ -> "research.locators.node";
					case ExplorerMapTargetDestination __ -> "research.locators.exploration_map_target";
					default -> "";
				});
				ctx.drawText(textRenderer(), text, x + 100 - textRenderer().getWidth(text), y + 30 * dIdx + 9, 0x111111, false);
			}
		});
	}
	
	public void renderAfter(DrawContext ctx, LocatorsSection section, int pageIdx, int screenWidth, int screenHeight, int mouseX, int mouseY, boolean right){
		int x = (right ? pageX + rightXOffset : pageX) + (screenWidth - 256) / 2 + 4;
		int y = pageY + (screenHeight - bgHeight) / 2 + 10 - heightOffset;
		
		getLocators(section).ifPresent(locators -> {
			for(int i = pageIdx * 5; i < locators.size() && i < (pageIdx + 1) * 5; i++){
				var locator = locators.get(i).value();
				int dIdx = i - pageIdx * 5;
				tooltipArea(ctx, AbstractRecipeSectionRenderer.displayStack(locator.input().getMatchingStacks()), mouseX, mouseY, x - 2, y + 30 * dIdx);
			}
		});
	}
	
	public int span(LocatorsSection section, PlayerEntity player){
		return getLocators(section).map(x -> Math.ceilDiv(x.size(), 5)).orElse(1);
	}
	
	private Optional<RegistryEntryList.Named<Locator>> getLocators(LocatorsSection section){
		TagKey<Locator> tag = section.getTag();
		return Locator.getRegistry(MinecraftClient.getInstance().world).getEntryList(tag);
	}
}