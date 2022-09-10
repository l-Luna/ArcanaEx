package arcana.client;

import arcana.ArcanaRegistry;
import arcana.ReflectivelyUtilized;
import arcana.aspects.Aspect;
import arcana.aspects.Aspects;
import arcana.aspects.ItemAspectsTooltipData;
import arcana.aspects.WandAspectsTooltipData;
import arcana.client.research.EntrySectionRenderer;
import arcana.client.research.PuzzleRenderer;
import arcana.client.research.RequirementRenderer;
import arcana.network.PkModifyPins;
import arcana.network.PkTryAdvance;
import arcana.research.Entry;
import arcana.research.Pin;
import arcana.research.Research;
import arcana.screens.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.gui.tooltip.BundleTooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.item.BundleTooltipData;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.item.TooltipData;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import static arcana.Arcana.arcId;

public final class ArcanaClient implements ClientModInitializer{
	
	public void onInitializeClient(){
		TooltipComponentCallback.EVENT.register(data ->
				data instanceof ItemAspectsTooltipData itd
						? new ItemAspectsTooltipComponent(itd.aspects(), dataToComponent(itd.inner()))
						: null);
		TooltipComponentCallback.EVENT.register(d ->
				d instanceof WandAspectsTooltipData w ? new WandAspectsTooltipComponent(w.wand()) : null);
		
		WorldRenderEvents.LAST.register(NodeRenderer::renderAll);
		ClientTickEvents.END_CLIENT_TICK.register(SwapFocusScreen::tryOpen);
		
		ModelLoadingRegistry.INSTANCE.registerResourceProvider(__ -> new WandModel.Provider());
		
		ColorProviderRegistry.BLOCK.register(
				(state, world, pos, tintIndex) -> world != null && pos != null ? BiomeColors.getWaterColor(world, pos) : -1,
				ArcanaRegistry.CRUCIBLE
		);
		ModelPredicateProviderRegistry.register(ArcanaRegistry.TOME_OF_SHARING, arcId("bound"), new TomeOfSharingPredicateProvider());
		
		HandledScreens.register(ArcanaRegistry.ARCANE_CRAFTING_SCREEN_HANDLER, ArcaneCraftingScreen::new);
		HandledScreens.register(ArcanaRegistry.RESEARCH_TABLE_SCREEN_HANDLER, ResearchTableScreen::new);
		HandledScreens.register(ArcanaRegistry.KNOWLEDGEABLE_DROPPER_SCREEN_HANDLER, KnowledgeableDropperScreen::new);
		
		BlockEntityRendererRegistry.register(ArcanaRegistry.CRUCIBLE_BE, context -> new CrucibleBlockEntityRenderer());
		BlockRenderLayerMap.INSTANCE.putBlock(ArcanaRegistry.RESEARCH_TABLE, RenderLayer.getCutout());
		
		EntrySectionRenderer.setup();
		RequirementRenderer.setup();
		PuzzleRenderer.setup();
		
		SwapFocusScreen.swapFocus = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.arcana.swap_focus",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_G,
				"category.arcana"
		));
	}
	
	private static TooltipComponent dataToComponent(TooltipData data){
		if(data == null)
			return null;
		if(data instanceof BundleTooltipData btd)
			return new BundleTooltipComponent(btd);
		return TooltipComponentCallback.EVENT.invoker().getComponent(data);
	}
	
	// Reflectively invoked by ResearchBookItem::use
	@ReflectivelyUtilized
	public static void openBook(Identifier bookId){
		var client = MinecraftClient.getInstance();
		client.execute(() -> client.setScreen(new ResearchBookScreen(Research.getBook(bookId), null)));
	}
	
	// Reflectively invoked by Researcher::applySyncPacket
	@ReflectivelyUtilized
	public static void refreshResearchEntryUi(){
		var client = MinecraftClient.getInstance();
		if(client.currentScreen instanceof ResearchEntryScreen entryScreen)
			entryScreen.updateButtons();
	}
	
	public static void sendTryAdvance(Entry entry){
		new PkTryAdvance(entry).sendToServer();
	}
	
	public static void sendModifyPins(Pin pin, boolean add){
		new PkModifyPins(pin, add).sendToServer();
	}
	
	public static Formatting colourForPrimal(Aspect aspect){
		if(aspect.equals(Aspects.AIR))
			return Formatting.YELLOW;
		if(aspect.equals(Aspects.FIRE))
			return Formatting.RED;
		if(aspect.equals(Aspects.WATER))
			return Formatting.BLUE;
		if(aspect.equals(Aspects.EARTH))
			return Formatting.GREEN;
		if(aspect.equals(Aspects.ORDER))
			return Formatting.GRAY;
		if(aspect.equals(Aspects.ENTROPY))
			return Formatting.DARK_GRAY;
		return Formatting.WHITE;
	}
}