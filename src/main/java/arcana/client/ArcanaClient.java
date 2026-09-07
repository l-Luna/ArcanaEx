package arcana.client;

import arcana.Arcana;
import arcana.ArcanaRegistry;
import arcana.ReflectivelyUtilized;
import arcana.aspects.*;
import arcana.blocks.ArcanaBlockSettings;
import arcana.blocks.be.WardedCampfireBlockEntity;
import arcana.cca_components.Researcher;
import arcana.client.ber.*;
import arcana.client.entity.*;
import arcana.client.particles.*;
import arcana.client.renderers.*;
import arcana.client.research.EntrySectionRenderer;
import arcana.client.research.PuzzleRenderer;
import arcana.client.research.RequirementRenderer;
import arcana.client.research.sections.TextSectionRenderer;
import arcana.client.tooltip.ItemAspectsTooltipComponent;
import arcana.client.tooltip.MagicMirrorTooltipComponent;
import arcana.client.tooltip.WandAspectsTooltipComponent;
import arcana.fluids.ArcanaFluid;
import arcana.items.MagicMirrorTooltipData;
import arcana.items.components.ArcanaDataComponents;
import arcana.items.components.StorageMapComponent;
import arcana.network.PkModifyPins;
import arcana.network.PkTryAdvance;
import arcana.research.*;
import arcana.screens.*;
import arcana.util.MathUtil;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.fabricmc.fabric.api.client.rendering.v1.*;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.block.BlockColorProvider;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.gui.tooltip.BundleTooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.CampfireBlockEntityRenderer;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.BundleTooltipData;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.biome.FoliageColors;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static arcana.Arcana.arcId;

public final class ArcanaClient implements ClientModInitializer{
	
	public static final Identifier SUPPRESSED_EFFECT_TEX_PATH = arcId("textures/gui/suppressed_effect.png");
	
	private static final int[] BALANCED_CRYSTAL_GRADIENT = new int[]{ 0xebd4b9, 0xedf2c2, 0xbcebc7, 0x6fdff2, 0xc7b9ed, 0xedb9e6, 0xf0c4c0 };
	
	public void onInitializeClient(){
		Arcana.CONFIG.registerCallback(config -> TextSectionRenderer.clearCache());
		
		TooltipComponentCallback.EVENT.register(data ->
				data instanceof ItemAspectsTooltipData(List<AspectStack> aspects, TooltipData inner)
						? new ItemAspectsTooltipComponent(aspects, dataToComponent(inner))
						: null);
		TooltipComponentCallback.EVENT.register(d -> {
			// TODO: combine data/component classes (like holding jug)? use marker interface for pass through?
			if(d instanceof WandAspectsTooltipData(ItemStack wand))
				return new WandAspectsTooltipComponent(wand);
			if(d instanceof MagicMirrorTooltipData(UUID tag))
				return new MagicMirrorTooltipComponent(tag);
			if(d instanceof StorageMapComponent h)
				return h;
			return null;
		});
		ItemTooltipCallback.EVENT.register(arcId("early"), (stack, ctx, type, lines) -> {
			if(stack.get(ArcanaDataComponents.FRAGILE) != null)
				lines.add(1, Text.translatable("tooltip.arcana.fragile").formatted(Formatting.GRAY));
		});
		
		WorldRenderEvents.LAST.register(NodeRenderer::render);
		WorldRenderEvents.BEFORE_BLOCK_OUTLINE.register((ctx1, hit) -> {
			PlaneProjectionRenderer.renderPlaneProjection(ctx1, hit);
			return true;
		});
		HudRenderCallback.EVENT.register(FocusSwitcherRenderer::renderHud);
		HudRenderCallback.EVENT.register(HudRenderer::renderHud);
		HudRenderCallback.EVENT.register(RunicShieldingRenderer::renderOverlay);
		ClientTickEvents.START_CLIENT_TICK.register(FocusSwitcherRenderer::tick);
		
		ModelLoadingPlugin.register(new WandModel.Provider());
		
		ModelLoadingPlugin.register(ctx -> {
			ctx.addModels(arcId("item/crimson_leech_attacking"));
			ctx.addModels(arcId("block/infusion_matrix_active"));
			
			ctx.addModels(InfusionPillarBlockEntityRenderer.BASE_ID);
			ctx.addModels(InfusionPillarBlockEntityRenderer.UPPER_ID);
			ctx.addModels(InfusionPillarBlockEntityRenderer.PEAK_ID);
		});
		// TODO: veiling
		/*CoreShaderRegistrationCallback.EVENT.register(context -> {
			context.register(arcId("particle_turbulent"), ArcanaShaders.FX, shader -> ArcanaShaders.fxTurbulent = shader);
		});*/
		
		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new IdentifiableResourceReloadListener(){
			public Identifier getFabricId(){
				return arcId("clear_cache");
			}
			public CompletableFuture<Void> reload(Synchronizer synchronizer, ResourceManager manager, Profiler prepareProfiler, Profiler applyProfiler, Executor prepareExecutor, Executor applyExecutor){
				synchronizer.whenPrepared(null);
				TextSectionRenderer.clearCache();
				return CompletableFuture.completedFuture(null);
			}
		});
		// a strange event, but it's close enough
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			ResearchBookScreen.resetNewEntries();
		});
		
		ColorProviderRegistry.BLOCK.register(
				(state, world, pos, tintIndex) -> world != null && pos != null ? BiomeColors.getWaterColor(world, pos) : -1,
				ArcanaRegistry.CRUCIBLE
		);
		ColorProviderRegistry.BLOCK.register(
				(state, world, pos, tintIndex) -> world != null && pos != null ? BiomeColors.getFoliageColor(world, pos) : FoliageColors.getDefaultColor(),
				ArcanaRegistry.GREATWOOD_LEAVES
		);
		BlockColorProvider balancedCrystalColourer = (state, world, pos, tintIndex) -> {
			if(pos != null)
				return 0xFF000000 | MathUtil.interpGradient((float)(Math.abs(pos.getX() + pos.getZ() + 3*Math.sin((pos.getX() - pos.getZ()) / 6f)) / 4f), BALANCED_CRYSTAL_GRADIENT);
			return -1;
		};
		ColorProviderRegistry.BLOCK.register(balancedCrystalColourer, ArcanaRegistry.BALANCED_CRYSTAL);
		ColorProviderRegistry.BLOCK.register(balancedCrystalColourer, ArcanaRegistry.BALANCED_CRYSTAL_PILLAR);
		ColorProviderRegistry.BLOCK.register(balancedCrystalColourer, ArcanaRegistry.CRYSTAL_EMBEDDED_ROCK);
		ColorProviderRegistry.BLOCK.register(balancedCrystalColourer, ArcanaRegistry.NORITE);
		ColorProviderRegistry.BLOCK.register(balancedCrystalColourer, ArcanaRegistry.EXPOSED_NORITE);
		
		ColorProviderRegistry.ITEM.register(
				(stack, tintIndex) -> {
					BlockState state = ((BlockItem)stack.getItem()).getBlock().getDefaultState();
					return ColorProviderRegistry.BLOCK.get(state.getBlock()).getColor(state, null, null, tintIndex);
				},
				ArcanaRegistry.GREATWOOD_LEAVES
		);
		ModelPredicateProviderRegistry.register(ArcanaRegistry.TOME_OF_SHARING, arcId("bound"), new TomeOfSharingPredicateProvider());
		ModelPredicateProviderRegistry.register(ArcanaRegistry.CRIMSON_LONGBOW, arcId("pull"), (stack, w, e, s)
				-> e == null ? 0 : e.getActiveItem() != stack ? 0 : (stack.getMaxUseTime(e) - e.getItemUseTimeLeft()) / 20f);
		ModelPredicateProviderRegistry.register(ArcanaRegistry.CRIMSON_LONGBOW, arcId("pulling"), (stack, w, e, s)
				-> e == null ? 0 : e.isUsingItem() && e.getActiveItem() == stack ? 1 : 0);
		ModelPredicateProviderRegistry.register(ArcanaRegistry.HOLDING_JUG, arcId("empty"), (stack, w, e, s)
				-> stack.getOrDefault(ArcanaDataComponents.HOLDING_JUG_CONTENTS, StorageMapComponent.DEFAULT).stored().isEmpty() ? 1 : 0);
		
		HandledScreens.register(ArcanaRegistry.ARCANE_CRAFTING_SCREEN_HANDLER, ArcaneCraftingScreen::new);
		HandledScreens.register(ArcanaRegistry.RESEARCH_TABLE_SCREEN_HANDLER, ResearchTableScreen::new);
		HandledScreens.register(ArcanaRegistry.KNOWLEDGEABLE_DROPPER_SCREEN_HANDLER, KnowledgeableDropperScreen::new);
		HandledScreens.register(ArcanaRegistry.ARCANE_FURNACE_SCREEN_HANDLER, ArcaneFurnaceScreen::new);
		HandledScreens.register(ArcanaRegistry.DISTILLERY_PATHFINDER_SCREEN_HANDLER, DistilleryPathfinderScreen::new);
		HandledScreens.register(ArcanaRegistry.CRYSTALLIZATION_PRESS_SCREEN_HANDLER, CrystallizationPressScreen::new);
		HandledScreens.register(ArcanaRegistry.FOCUS_POUCH_SCREEN_HANDLER, FocusPouchScreen::new);
		
		BlockEntityRendererRegistry.register(ArcanaRegistry.CRUCIBLE_BE, ctx -> new CrucibleBlockEntityRenderer());
		BlockEntityRendererRegistry.register(ArcanaRegistry.PEDESTAL_BE, PedestalBlockEntityRenderer::new);
		BlockEntityRendererRegistry.register(ArcanaRegistry.INFUSION_PILLAR_BE, ctx -> new InfusionPillarBlockEntityRenderer());
		BlockEntityRendererRegistry.register(ArcanaRegistry.INFUSION_MATRIX_BE, ctx -> new InfusionMatrixBlockEntityRenderer());
		BlockEntityRendererRegistry.register(ArcanaRegistry.WARDED_JAR_BE, ctx -> new WardedJarBlockEntityRenderer());
		BlockEntityRendererRegistry.register(ArcanaRegistry.VOID_JAR_BE, ctx -> new WardedJarBlockEntityRenderer());
		BlockEntityRendererRegistry.register(ArcanaRegistry.MYSTIC_MIST_BE, ctx -> new MysticMistBlockEntityRenderer());
		BlockEntityRendererRegistry.register(ArcanaRegistry.ESSENTIA_VALVE_BE, ctx -> new EssentiaValveBlockEntityRenderer());
		BlockEntityRendererRegistry.register(ArcanaRegistry.ALEMBIC_BE, ctx -> new AlembicBlockEntityRenderer());
		BlockEntityRendererRegistry.register(ArcanaRegistry.THAUMIC_HALO_BE, ctx -> new ThaumicHaloBlockEntityRenderer());
		// ohhhh but the variance! the variance! it's so bad!
		BlockEntityRendererRegistry.register(ArcanaRegistry.WARDED_CAMPFIRE_BE, ctx ->
				(BlockEntityRenderer<WardedCampfireBlockEntity>)(BlockEntityRenderer<?>)new CampfireBlockEntityRenderer(ctx));
		
		for(ArcanaFluid fluid : ArcanaRegistry.STILL_FLUIDS){
			FluidRenderHandlerRegistry.INSTANCE.register(
					fluid.getStill(),
					fluid.getFlowing(),
					new SimpleFluidRenderHandler(
							arcId(fluid.getTexturePath()),
							arcId(fluid.getTexturePath() + "_flowing")
					)
			);
			BlockRenderLayerMap.INSTANCE.putFluids(RenderLayer.getTranslucent(), fluid.getStill(), fluid.getFlowing());
		}
		
		EntityRendererRegistry.register(ArcanaRegistry.THROWN_ALUMENTUM, ThrownAlumentumEntityRenderer::new);
		EntityRendererRegistry.register(ArcanaRegistry.THROWN_TAINT_BOTTLE, FlyingItemEntityRenderer::new);
		EntityRendererRegistry.register(ArcanaRegistry.PRISMATIC_ORB, ctx -> new MagicOrbEntityRenderer<>(ctx, List.of(
				0xECF8FE, 0xF8F8FF, 0xDAEAFE, 0xFDFDFF, 0xF5EDFF, 0xECD9FE
		)));
		EntityRendererRegistry.register(ArcanaRegistry.FLAME_ORB, ctx -> new MagicOrbEntityRenderer<>(ctx, List.of(
				0xFFFFFF, 0xEFA95D, 0xFBF780, 0xED8A50, 0xF4F2C6, 0xF4F2C6
		)));
		EntityRendererRegistry.register(ArcanaRegistry.CRIMSON_KNIGHT, ctx -> new CrimsonEntityRenderer<>(ctx, "knight"));
		EntityRendererRegistry.register(ArcanaRegistry.CRIMSON_ARCHER, ctx -> new CrimsonEntityRenderer<>(ctx, "archer"));
		EntityRendererRegistry.register(ArcanaRegistry.CRIMSON_PROTECTOR, ctx -> new CrimsonEntityRenderer<>(ctx, "protector"));
		EntityRendererRegistry.register(ArcanaRegistry.CRIMSON_MISSIONARY, ctx -> new CrimsonEntityRenderer<>(ctx, "missionary"));
		EntityRendererRegistry.register(ArcanaRegistry.CRIMSON_JESTER, ctx -> new CrimsonEntityRenderer<>(ctx, "jester"));
		EntityRendererRegistry.register(ArcanaRegistry.CRIMSON_HEAVY_KNIGHT, ctx -> new CrimsonEntityRenderer<>(ctx, "heavy_knight"));
		EntityRendererRegistry.register(ArcanaRegistry.WISP, ctx -> new WispLikeEntityRenderer<>(ctx, 3, 21, false, 1, null));
		EntityRendererRegistry.register(ArcanaRegistry.TAINTED_WISP, ctx -> new WispLikeEntityRenderer<>(ctx, 3, 6, false, 1.1f, arcId("textures/entity/tainted_wisp.png")));
		EntityRendererRegistry.register(ArcanaRegistry.PURE_WISP, ctx -> new WispLikeEntityRenderer<>(ctx, 2, 34, false, 0.8f, arcId("textures/entity/pure_wisp.png")));
		EntityRendererRegistry.register(ArcanaRegistry.COAGULATION, ctx -> new WispLikeEntityRenderer<>(ctx, 2, 12, true, 0.5f, null));
		EntityRendererRegistry.register(ArcanaRegistry.LESSER_WISP, ctx -> new WispLikeEntityRenderer<>(ctx, 1, 8, false, 0.33f, arcId("textures/entity/lesser_wisp.png")));
		EntityRendererRegistry.register(ArcanaRegistry.ZOMBIE_THAUMATURGE, ctx -> new BipedGeoEntityRenderer<>(ctx, new PlainGeoModel<>(arcId("geo/zombie_thaumaturge.geo.json"), arcId("textures/entity/zombie_thaumaturge.png"), arcId("animations/zombie_thaumaturge.animation.json"))));
		
		for(Block block : ArcanaRegistry.BLOCKS)
			if(block.getSettings() instanceof ArcanaBlockSettings abs)
				if(abs.getRenderLayer() != null)
					BlockRenderLayerMap.INSTANCE.putBlock(block, switch(abs.getRenderLayer()){
						case CUTOUT -> RenderLayer.getCutout();
						case OPAQUE -> RenderLayer.getSolid();
						case TRANSLUCENT -> RenderLayer.getTranslucent();
					});
		
		EntrySectionRenderer.setup();
		RequirementRenderer.setup();
		PuzzleRenderer.setup();
		
		ParticleFactoryRegistry.getInstance().register(ArcanaRegistry.TAINT_BUBBLE, spr -> new SimpleSpriteParticle.Factory(spr, 0.02f, 0).lifetime(50).scale(2).randomAngle());
		ParticleFactoryRegistry.getInstance().register(ArcanaRegistry.FLAME, spr -> new SimpleSpriteParticle.Factory(spr, 0, 0.06f));
		ParticleFactoryRegistry.getInstance().register(ArcanaRegistry.LIGHTNING, spr -> new SimpleSpriteParticle.Factory(spr, 0, 0.04f).lifetime(40).randomAngle());
		ParticleFactoryRegistry.getInstance().register(ArcanaRegistry.TAINT_SPORE, spr -> new SimpleSpriteParticle.Factory(spr, -0.003f, 0.04f).lifetime(16, 80).collidable(false).scale(0.2f, 0.8f).tint(0.8f, 0, 0.9f));
		ParticleFactoryRegistry.getInstance().register(ArcanaRegistry.CLAW, spr -> new SimpleSpriteParticle.Factory(spr, 0, 0).scale(3, 3.4f).shrink(0.02f).lifetime(5, 8));
		ParticleFactoryRegistry.getInstance().register(ArcanaRegistry.CLAW_RUBY, spr -> new SimpleSpriteParticle.Factory(spr, 0, 0).scale(3, 3.4f).shrink(0.02f).lifetime(5, 8));
		ParticleFactoryRegistry.getInstance().register(ArcanaRegistry.PRISM_GLITTER, spr -> new SimpleSpriteParticle.Factory(spr, 0.0125f, 0.09f).scale(0.75f).lifetime(60, 72).shrink(0.001f).randomSprite().randomAngle());
		ParticleFactoryRegistry.getInstance().register(ArcanaRegistry.UPWIND, spr -> new SimpleSpriteParticle.Factory(spr, 0, 0.03f).scale(0.75f).lifetime(16));
		
		ParticleFactoryRegistry.getInstance().register(ArcanaRegistry.WARDING_EFFECT, u -> new CubeParticle.Factory(u, 1, 1, 1));
		ParticleFactoryRegistry.getInstance().register(ArcanaRegistry.INFESTED_EFFECT, u -> new CubeParticle.Factory(u, 0.1f, 0.1f, 0.1f));
		
		ParticleFactoryRegistry.getInstance().register(ArcanaRegistry.HUNGRY_NODE_DISC, new HungryNodeDiscParticle.Factory());
		ParticleFactoryRegistry.getInstance().register(ArcanaRegistry.HUNGRY_NODE_BLOCK, new HungryNodeBlockParticle.Factory());
		ParticleFactoryRegistry.getInstance().register(ArcanaRegistry.INFUSION_ITEM, new InfusionItemParticle.Factory());
		ParticleFactoryRegistry.getInstance().register(ArcanaRegistry.ESSENTIA_STREAM, EssentiaStreamParticle.Factory::new);
	}
	
	private static TooltipComponent dataToComponent(TooltipData data){
		if(data == null)
			return null;
		if(data instanceof BundleTooltipData btd)
			return new BundleTooltipComponent(btd.contents());
		return TooltipComponentCallback.EVENT.invoker().getComponent(data);
	}
	
	@ReflectivelyUtilized // by ResearchBookItem::use
	public static void openBook(Identifier bookId){
		MinecraftClient.getInstance().execute(() -> MinecraftClient.getInstance().setScreen(new ResearchBookScreen(Research.getBook(bookId))));
	}
	
	@ReflectivelyUtilized // by DirectResearchEntryItem::use
	public static void openEntry(Identifier entryId){
		MinecraftClient.getInstance().execute(() -> MinecraftClient.getInstance().setScreen(new ResearchEntryScreen(Research.getEntry(entryId), null)));
	}
	
	@ReflectivelyUtilized // by Researcher::applySyncPacket
	public static void postResearchUpdate(Set<Addendum> newAddenda, Set<Entry> newEntries){
		MinecraftClient client = MinecraftClient.getInstance();
		Screen screen = client.currentScreen;
		if(screen instanceof ResearchEntryScreen entryScreen)
			entryScreen.updateButtons();
		else if(screen instanceof ResearchBookScreen bookScreen)
			bookScreen.refreshProgressable();
		
		ResearchBookScreen.notifyNewEntries(newEntries);
		
		Researcher researcher = Researcher.from(client.player);
		if(researcher.isEntryComplete(Research.getEntry(BuiltinResearch.rootEntry))){
			for(Addendum addendum : newAddenda){
				Entry owner = addendum.owner();
				ResearchBookScreen.notifyNewAddendaEntry(owner);
				if(researcher.isEntryComplete(owner))
					MinecraftClient.getInstance().getToastManager().add(new ResearchUnlockedToast(owner, true));
			}
			
			for(Entry entry : newEntries)
				if(entry.meta().contains("notify"))
					MinecraftClient.getInstance().getToastManager().add(new ResearchUnlockedToast(entry, false));
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
	
	public static float osc(int period){
		// TODO: use client time instead of world time to fix issues when lagging/on servers/out of worlds; fix jittering when paused
		MinecraftClient client = MinecraftClient.getInstance();
		if(client.world == null)
			return 0;
		return MathUtil.osc(client.world, period, client.getRenderTickCounter().getTickDelta(true));
	}
}