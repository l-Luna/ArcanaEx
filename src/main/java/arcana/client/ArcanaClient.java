package arcana.client;

import arcana.ArcanaRegistry;
import arcana.ReflectivelyUtilized;
import arcana.aspects.Aspect;
import arcana.aspects.Aspects;
import arcana.aspects.ItemAspectsTooltipData;
import arcana.aspects.WandAspectsTooltipData;
import arcana.blocks.ArcanaBlockSettings;
import arcana.client.particles.EssentiaStreamParticle;
import arcana.client.particles.HungryNodeBlockParticle;
import arcana.client.particles.HungryNodeDiscParticle;
import arcana.client.particles.InfusionItemParticle;
import arcana.client.research.EntrySectionRenderer;
import arcana.client.research.PuzzleRenderer;
import arcana.client.research.RequirementRenderer;
import arcana.components.Researcher;
import arcana.network.PkModifyPins;
import arcana.network.PkTryAdvance;
import arcana.research.BuiltinResearch;
import arcana.research.Entry;
import arcana.research.Pin;
import arcana.research.Research;
import arcana.screens.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.*;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.color.world.FoliageColors;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.gui.tooltip.BundleTooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.item.BundleTooltipData;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.item.TooltipData;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.item.BlockItem;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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
		ModelLoadingRegistry.INSTANCE.registerAppender((manager, out) -> {
			out.accept(new ModelIdentifier(arcId("infusion_pillar_base"), ""));
			out.accept(new ModelIdentifier(arcId("infusion_pillar_upper"), ""));
		});
		ClientSpriteRegistryCallback.event(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE).register((atlasTexture, registry) -> {
			registry.register(WardedJarBlockEntityRenderer.topTexture);
			registry.register(WardedJarBlockEntityRenderer.sideTexture);
			registry.register(WardedJarBlockEntityRenderer.bottomTexture);
		});
		
		ColorProviderRegistry.BLOCK.register(
				(state, world, pos, tintIndex) -> world != null && pos != null ? BiomeColors.getWaterColor(world, pos) : -1,
				ArcanaRegistry.CRUCIBLE
		);
		ColorProviderRegistry.BLOCK.register(
				(state, world, pos, tintIndex) -> world != null && pos != null ? BiomeColors.getFoliageColor(world, pos) : FoliageColors.getDefaultColor(),
				ArcanaRegistry.GREATWOOD_LEAVES
		);
		
		ColorProviderRegistry.ITEM.register(
				(stack, tintIndex) -> {
					BlockState state = ((BlockItem)stack.getItem()).getBlock().getDefaultState();
					return ColorProviderRegistry.BLOCK.get(state.getBlock()).getColor(state, null, null, tintIndex);
				},
				ArcanaRegistry.GREATWOOD_LEAVES
		);
		ModelPredicateProviderRegistry.register(ArcanaRegistry.TOME_OF_SHARING, arcId("bound"), new TomeOfSharingPredicateProvider());
		
		HandledScreens.register(ArcanaRegistry.ARCANE_CRAFTING_SCREEN_HANDLER, ArcaneCraftingScreen::new);
		HandledScreens.register(ArcanaRegistry.RESEARCH_TABLE_SCREEN_HANDLER, ResearchTableScreen::new);
		HandledScreens.register(ArcanaRegistry.KNOWLEDGEABLE_DROPPER_SCREEN_HANDLER, KnowledgeableDropperScreen::new);
		
		BlockEntityRendererRegistry.register(ArcanaRegistry.CRUCIBLE_BE, ctx -> new CrucibleBlockEntityRenderer());
		BlockEntityRendererRegistry.register(ArcanaRegistry.PEDESTAL_BE, PedestalBlockEntityRenderer::new);
		BlockEntityRendererRegistry.register(ArcanaRegistry.INFUSION_PILLAR_BE, ctx -> new InfusionPillarBlockEntityRenderer());
		BlockEntityRendererRegistry.register(ArcanaRegistry.INFUSION_MATRIX_BE, ctx -> new InfusionMatrixBlockEntityRenderer());
		BlockEntityRendererRegistry.register(ArcanaRegistry.WARDED_JAR_BE, ctx -> new WardedJarBlockEntityRenderer());
		BlockEntityRendererRegistry.register(ArcanaRegistry.MYSTIC_MIST_BE, ctx -> new MysticMistBlockEntityRenderer());
		
		EntityRendererRegistry.register(ArcanaRegistry.THROWN_ALUMENTUM, ThrownAlumentumEntityRenderer::new);
		
		for(Block block : ArcanaRegistry.blocks)
			if(block.settings instanceof ArcanaBlockSettings abs)
				if(abs.getRenderLayer() != null)
					BlockRenderLayerMap.INSTANCE.putBlock(block, switch(abs.getRenderLayer()){
						case CUTOUT -> RenderLayer.getCutout();
						case OPAQUE -> RenderLayer.getSolid();
						case TRANSLUCENT -> RenderLayer.getTranslucent();
					});
		
		EntrySectionRenderer.setup();
		RequirementRenderer.setup();
		PuzzleRenderer.setup();
		
		SwapFocusScreen.swapFocus = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.arcana.swap_focus",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_G,
				"category.arcana"
		));
		
		ParticleFactoryRegistry.getInstance().register(ArcanaRegistry.HUNGRY_NODE_DISC, new HungryNodeDiscParticle.Factory());
		ParticleFactoryRegistry.getInstance().register(ArcanaRegistry.HUNGRY_NODE_BLOCK, new HungryNodeBlockParticle.Factory());
		ParticleFactoryRegistry.getInstance().register(ArcanaRegistry.INFUSION_ITEM, new InfusionItemParticle.Factory());
		ParticleFactoryRegistry.getInstance().register(ArcanaRegistry.ESSENTIA_STREAM, EssentiaStreamParticle.Factory::new);
	}
	
	private static TooltipComponent dataToComponent(TooltipData data){
		if(data == null)
			return null;
		if(data instanceof BundleTooltipData btd)
			return new BundleTooltipComponent(btd);
		return TooltipComponentCallback.EVENT.invoker().getComponent(data);
	}
	
	@ReflectivelyUtilized // by ResearchBookItem::use
	public static void openBook(Identifier bookId){
		var client = MinecraftClient.getInstance();
		client.execute(() -> client.setScreen(new ResearchBookScreen(Research.getBook(bookId), null)));
	}
	
	// TODO ugly
	private static Set<Identifier> notifyIfComplete;
	
	@ReflectivelyUtilized // by Researcher::applySyncPacket
	public static void preResearchUpdate(){
		var researcher = Researcher.from(MinecraftClient.getInstance().player);
		// don't trigger on every world load
		var root = Research.getEntry(BuiltinResearch.rootResearch);
		if(root != null && researcher.isEntryComplete(root)){
			notifyIfComplete = new HashSet<>(BuiltinResearch.infoResearch);
			notifyIfComplete.removeIf(x -> researcher.isEntryComplete(Research.getEntry(x)));
		}else
			notifyIfComplete = Collections.emptySet();
	}
	
	@ReflectivelyUtilized // by Researcher::applySyncPacket
	public static void postResearchUpdate(){
		var client = MinecraftClient.getInstance();
		if(client.currentScreen instanceof ResearchEntryScreen entryScreen)
			entryScreen.updateButtons();
		var researcher = Researcher.from(MinecraftClient.getInstance().player);
		if(researcher.isEntryComplete(Research.getEntry(BuiltinResearch.rootResearch)))
			for(Identifier identifier : notifyIfComplete){
				var entry = Research.getEntry(identifier);
				if(researcher.isEntryComplete(entry))
					MinecraftClient.getInstance().getToastManager().add(new ResearchUnlockedToast(entry));
			}
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