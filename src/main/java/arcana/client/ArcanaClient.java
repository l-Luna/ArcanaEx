package arcana.client;

import arcana.ArcanaRegistry;
import arcana.aspects.ItemAspectsTooltipData;
import arcana.aspects.WandAspectsTooltipData;
import arcana.client.research.EntrySectionRenderer;
import arcana.client.research.RequirementRenderer;
import arcana.research.Research;
import arcana.research.ResearchNetworking;
import arcana.screens.ArcaneCraftingScreen;
import arcana.screens.ResearchBookScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
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
import net.minecraft.client.item.TooltipData;
import net.minecraft.util.Identifier;

public final class ArcanaClient implements ClientModInitializer{
	
	public void onInitializeClient(){
		TooltipComponentCallback.EVENT.register(data ->
				data instanceof ItemAspectsTooltipData itd
						? new ItemAspectsTooltipComponent(itd.aspects(), dataToComponent(itd.inner()))
						: null);
		TooltipComponentCallback.EVENT.register(d ->
				d instanceof WandAspectsTooltipData w ? new WandAspectsTooltipComponent(w.wand()) : null);
		
		WorldRenderEvents.LAST.register(NodeRenderer::renderAll);
		
		ModelLoadingRegistry.INSTANCE.registerResourceProvider(__ -> new WandModel.Provider());
		
		ColorProviderRegistry.BLOCK.register(
				(state, world, pos, tintIndex) -> world != null && pos != null ? BiomeColors.getWaterColor(world, pos) : -1,
				ArcanaRegistry.CRUCIBLE
		);
		
		HandledScreens.register(ArcanaRegistry.ARCANE_CRAFTING_SCREEN_HANDLER, ArcaneCraftingScreen::new);
		
		BlockEntityRendererRegistry.register(ArcanaRegistry.CRUCIBLE_BE, context -> new CrucibleBlockEntityRenderer());
		
		ClientPlayNetworking.registerGlobalReceiver(ResearchNetworking.syncPacketId,
				(client, handler, buf, responseSender) -> ResearchNetworking.deserializeResearch(buf));
		EntrySectionRenderer.setup();
		RequirementRenderer.setup();
		
		// ensure text section caches are reset when reloading translations
		/*ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new IdentifiableResourceReloadListener(){
			public Identifier getFabricId(){ return arcId("text_section_reset"); }
			public CompletableFuture<Void> reload(Synchronizer _s, ResourceManager _m, Profiler _pp, Profiler _ap, Executor _pe, Executor _ae){
				TextSectionRenderer.clearCache();
				return CompletableFuture.completedFuture(null);
			}
		});*/
	}
	
	private static TooltipComponent dataToComponent(TooltipData data){
		if(data == null)
			return null;
		if(data instanceof BundleTooltipData btd)
			return new BundleTooltipComponent(btd);
		return TooltipComponentCallback.EVENT.invoker().getComponent(data);
	}
	
	// Reflectively invoked by ResearchBookItem::use
	public static void openBook(Identifier bookId){
		var client = MinecraftClient.getInstance();
		client.execute(() -> client.setScreen(new ResearchBookScreen(Research.getBook(bookId), null)));
	}
}