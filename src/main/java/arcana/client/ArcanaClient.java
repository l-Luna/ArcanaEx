package arcana.client;

import arcana.ArcanaRegistry;
import arcana.aspects.ItemAspectsTooltipData;
import arcana.aspects.WandAspectsTooltipData;
import arcana.screens.ArcaneCraftingScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.gui.tooltip.BundleTooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.item.BundleTooltipData;
import net.minecraft.client.item.TooltipData;

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
	}
	
	private static TooltipComponent dataToComponent(TooltipData data){
		if(data == null)
			return null;
		if(data instanceof BundleTooltipData btd)
			return new BundleTooltipComponent(btd);
		return TooltipComponentCallback.EVENT.invoker().getComponent(data);
	}
}