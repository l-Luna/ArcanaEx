package arcana;

import arcana.aspects.Aspect;
import arcana.aspects.Aspects;
import arcana.blocks.*;
import arcana.client.particles.AspectParticleEffect;
import arcana.enchantments.ProjectingEnchantment;
import arcana.enchantments.WarpingCurseEnchantment;
import arcana.entities.ThrownAlumentumEntity;
import arcana.items.*;
import arcana.items.foci.EquivalentExchangeFocusItem;
import arcana.items.foci.FireFocusItem;
import arcana.items.foci.LightFocusItem;
import arcana.items.foci.PortableHoleFocusItem;
import arcana.screens.ArcaneCraftingScreenHandler;
import arcana.screens.KnowledgeableDropperScreenHandler;
import arcana.screens.ResearchTableScreenHandler;
import arcana.worldgen.SurfaceNodeFeature;
import arcana.worldgen.geodes.NodalGeodes;
import arcana.worldgen.greatwood.GreatwoodFoliagePlacer;
import arcana.worldgen.greatwood.GreatwoodSaplingGenerator;
import arcana.worldgen.greatwood.GreatwoodTree;
import arcana.worldgen.greatwood.GreatwoodTrunkPlacer;
import arcana.worldgen.silverwood.SilverwoodFoliagePlacer;
import arcana.worldgen.silverwood.SilverwoodSaplingGenerator;
import arcana.worldgen.silverwood.SilverwoodTree;
import arcana.worldgen.silverwood.SilverwoodTrunkPlacer;
import com.unascribed.lib39.fractal.api.ItemSubGroup;
import dev.emi.trinkets.api.TrinketItem;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.fabricmc.fabric.api.registry.StrippableBlockRegistry;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.*;
import net.minecraft.item.Item.Settings;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.foliage.FoliagePlacerType;
import net.minecraft.world.gen.placementmodifier.HeightmapPlacementModifier;
import net.minecraft.world.gen.trunk.TrunkPlacerType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToIntFunction;

import static arcana.Arcana.arcId;
import static arcana.blocks.ArcanaBlockSettings.BlockLayer.CUTOUT;
import static arcana.blocks.ArcanaBlockSettings.BlockLayer.TRANSLUCENT;
import static arcana.blocks.ArcanaBlockSettings.of;
import static arcana.items.CapItem.capProperties;
import static arcana.items.CoreItem.coreProperties;
import static net.minecraft.tag.BlockTags.AXE_MINEABLE;
import static net.minecraft.tag.BlockTags.PICKAXE_MINEABLE;

public final class ArcanaRegistry{
	
	public static class Tab{
		// tfw "illegal forward reference"
		public static final ItemGroup ARCANA = FabricItemGroupBuilder.build(
				arcId("group"),
				() -> new ItemStack(ARCANUM)
		);
		public static final ItemSubGroup MAIN = ItemSubGroup.create(ARCANA, arcId("main"));
		public static final ItemSubGroup EQUIPMENT = ItemSubGroup.create(ARCANA, arcId("equipment"));
		public static final ItemSubGroup CRYSTALS = ItemSubGroup.create(ARCANA, arcId("crystals"));
		public static final ItemSubGroup PHIALS = ItemSubGroup.create(ARCANA, arcId("phials"));
	}
	
	private static final Settings GROUPED = new Settings().group(Tab.MAIN);
	private static final Settings GROUPED_SINGLE = new Settings().group(Tab.MAIN).maxCount(1);
	
	// items...
	public static final Item SCRIBBLED_NOTES = new ScribbledNotesItem(GROUPED_SINGLE);
	public static final Item GOGGLES_OF_REVEALING = new GogglesOfRevealingItem(new Settings().group(Tab.MAIN).maxCount(1));
	public static final Item MONOCLE_OF_REVEALING = new TrinketItem(GROUPED_SINGLE);
	
	public static final Item ARCANUM = new ResearchBookItem(GROUPED_SINGLE, arcId("arcanum"));
	public static final Item CRIMSON_RITES = new ResearchBookItem(GROUPED_SINGLE, arcId("crimson_rites"));
	public static final Item TAINTED_CODEX = new ResearchBookItem(GROUPED_SINGLE, arcId("tainted_codex"));
	
	public static final Item RESEARCH_NOTES = new ResearchNotesItem(new Settings().maxCount(1), false);
	public static final Item COMPLETE_RESEARCH_NOTES = new ResearchNotesItem(new Settings().maxCount(1), true);
	
	public static final Item TOME_OF_SHARING = new TomeOfSharingItem(GROUPED_SINGLE);
	
	public static final Item CHEATERS_ARCANUM = new CheatersArcanumItem(GROUPED_SINGLE);
	
	public static final Item ARCANIUM_INGOT = new Item(GROUPED);
	public static final Item ARCANIUM_SWORD = new SwordItem(ArcanaToolMaterials.ARCANIUM, 3, -2.4f, new Settings().group(Tab.EQUIPMENT));
	public static final Item ARCANIUM_SHOVEL = new ShovelItem(ArcanaToolMaterials.ARCANIUM, 1.5f, -3, new Settings().group(Tab.EQUIPMENT));
	public static final Item ARCANIUM_PICKAXE = new PickaxeItem(ArcanaToolMaterials.ARCANIUM, 1, -2.8f, new Settings().group(Tab.EQUIPMENT));
	public static final Item ARCANIUM_AXE = new AxeItem(ArcanaToolMaterials.ARCANIUM, 5.5f, -3, new Settings().group(Tab.EQUIPMENT));
	public static final Item ARCANIUM_HOE = new HoeItem(ArcanaToolMaterials.ARCANIUM, -2, -1, new Settings().group(Tab.EQUIPMENT));
	public static final Item ARCANIUM_HELMET = new ArmorItem(ArcanaArmourMaterials.ARCANIUM, EquipmentSlot.HEAD, new Settings().group(Tab.EQUIPMENT));
	public static final Item ARCANIUM_CHESTPLATE = new ArmorItem(ArcanaArmourMaterials.ARCANIUM, EquipmentSlot.CHEST, new Settings().group(Tab.EQUIPMENT));
	public static final Item ARCANIUM_LEGGINGS = new ArmorItem(ArcanaArmourMaterials.ARCANIUM, EquipmentSlot.LEGS, new Settings().group(Tab.EQUIPMENT));
	public static final Item ARCANIUM_BOOTS = new ArmorItem(ArcanaArmourMaterials.ARCANIUM, EquipmentSlot.FEET, new Settings().group(Tab.EQUIPMENT));
	public static final Item ARCANIUM_RING = new ArcaniumRingItem(new Settings().group(Tab.EQUIPMENT));
	
	public static final Item BOOTS_OF_THE_TRAVELLER = new BootsOfTheTravellerItem(ArcanaArmourMaterials.BOOTS_OF_THE_TRAVELLER, new Settings().group(Tab.EQUIPMENT));
	public static final Item BOOTS_OF_THE_SAILOR = new BootsOfTheTravellerItem(ArcanaArmourMaterials.BOOTS_OF_THE_SAILOR, new Settings().group(Tab.EQUIPMENT));
	public static final Item BOOTS_OF_THE_REAPER = new BootsOfTheTravellerItem(ArcanaArmourMaterials.BOOTS_OF_THE_REAPER, new Settings().group(Tab.EQUIPMENT));
	
	public static final Item ALCHEMICAL_IRON = new Item(GROUPED);
	public static final Item ALCHEMICAL_GOLD = new Item(GROUPED);
	public static final Item ALCHEMICAL_COPPER = new Item(GROUPED);
	public static final Item ALTERED_IRON = new Item(GROUPED);
	public static final Item ALUMENTUM = new AlumentumItem(GROUPED);
	
	public static final Item WAND = new WandItem(GROUPED_SINGLE);
	
	// caps...
	public static final CapItem IRON_WAND_CAP = new CapItem(GROUPED, capProperties().capacity(5).complexity(3));
	
	public static final CapItem GOLD_WAND_CAP = new CapItem(GROUPED, capProperties().capacity(14).complexity(15));
	public static final CapItem COPPER_WAND_CAP = new CapItem(GROUPED, capProperties().capacity(10).complexity(12).strBonus(5));
	public static final CapItem LEATHER_WAND_CAP = new CapItem(GROUPED, capProperties().capacity(10).complexity(12).discountAll(8));
	
	public static final CapItem THAUMIUM_WAND_CAP = new CapItem(GROUPED, capProperties().capacity(25).complexity(35));
	public static final CapItem BAMBOO_WAND_CAP = new CapItem(GROUPED, capProperties().capacity(20).complexity(30).discountFor(Aspects.AIR, 12));
	public static final CapItem QUARTZ_WAND_CAP = new CapItem(GROUPED, capProperties().capacity(20).complexity(30).discountFor(Aspects.FIRE, 12));
	public static final CapItem PRISMARINE_WAND_CAP = new CapItem(GROUPED, capProperties().capacity(20).complexity(30).discountFor(Aspects.WATER, 12));
	public static final CapItem AMBER_WAND_CAP = new CapItem(GROUPED, capProperties().capacity(20).complexity(30).discountFor(Aspects.EARTH, 12));
	public static final CapItem HONEYCOMB_WAND_CAP = new CapItem(GROUPED, capProperties().capacity(20).complexity(30).discountFor(Aspects.ORDER, 12));
	// TODO: chaos elemental cap
	
	public static final CapItem NETHERITE_WAND_CAP = new CapItem(GROUPED, capProperties().capacity(55).complexity(75));
	public static final CapItem MECHANICAL_WAND_CAP = new CapItem(GROUPED, capProperties().capacity(40).complexity(55)/*.mechanical()?*/);
	public static final CapItem VOID_WAND_CAP = new CapItem(GROUPED, capProperties().capacity(50).complexity(80).warping(1));
	public static final CapItem CRIMSON_WAND_CAP = new CapItem(GROUPED, capProperties().capacity(75).complexity(70).warping(2));
	
	public static final Cap MISSING_CAP = new Cap.Impl(arcId("missing"), 0, 0);
	
	// cores...
	public static final Core STICK_CORE = new Core.Impl(arcId("stick_wand_core"), 20, 3);
	
	public static final CoreItem GREATWOOD_WAND_CORE = new CoreItem(GROUPED, coreProperties().capacity(45).strength(10));
	public static final CoreItem NETHER_STEM_WAND_CORE = new CoreItem(GROUPED, coreProperties().capacity(37).strength(8).cmplxBonus(6));
	public static final CoreItem VARNISHED_WOOD_WAND_CORE = new CoreItem(GROUPED, coreProperties().capacity(37).strength(8).discountAll(12));
	
	public static final CoreItem SILVERWOOD_WAND_CORE = new CoreItem(GROUPED, coreProperties().capacity(78).strength(20));
	public static final CoreItem SUGAR_CANE_WAND_CORE = new CoreItem(GROUPED, coreProperties().capacity(63).strength(15).discountFor(Aspects.AIR, 16));
	public static final CoreItem BLAZE_WAND_CORE = new CoreItem(GROUPED, coreProperties().capacity(63).strength(15).discountFor(Aspects.FIRE, 16));
	public static final CoreItem ICE_WAND_CORE = new CoreItem(GROUPED, coreProperties().capacity(63).strength(15).discountFor(Aspects.WATER, 16));
	public static final CoreItem OBSIDIAN_WAND_CORE = new CoreItem(GROUPED, coreProperties().capacity(63).strength(15).discountFor(Aspects.EARTH, 16));
	public static final CoreItem ARCANE_STONE_WAND_CORE = new CoreItem(GROUPED, coreProperties().capacity(63).strength(15).discountFor(Aspects.ORDER, 16));
	public static final CoreItem BONE_WAND_CORE = new CoreItem(GROUPED, coreProperties().capacity(63).strength(15).discountFor(Aspects.ENTROPY, 16));
	
	public static final CoreItem ARCANIUM_WAND_CORE = new CoreItem(GROUPED, coreProperties().capacity(130).strength(40));
	public static final CoreItem MECHANICAL_WAND_CORE = new CoreItem(GROUPED, coreProperties().capacity(100).strength(30));
	public static final CoreItem TAINTED_WAND_CORE = new CoreItem(GROUPED, coreProperties().capacity(110).strength(58).warping(1));
	public static final CoreItem ELDRITCH_WAND_CORE = new CoreItem(GROUPED, coreProperties().capacity(165).strength(35).warping(2));
	
	public static final Core MISSING_CORE = new Core.Impl(arcId("missing"), 0, 0);
	
	// foci...
	public static final Item FIRE_FOCUS = new FireFocusItem(GROUPED_SINGLE);
	public static final Item PORTABLE_HOLE_FOCUS = new PortableHoleFocusItem(GROUPED_SINGLE);
	public static final Item LIGHT_FOCUS = new LightFocusItem(GROUPED_SINGLE);
	public static final Item PRISMATIC_LIGHT_FOCUS = new FocusItem(GROUPED_SINGLE);
	public static final Item EQUIVALENT_EXCHANGE_FOCUS = new EquivalentExchangeFocusItem(GROUPED_SINGLE);
	
	// other...?
	public static final Item EMPTY_PHIAL = new PhialItem(new Settings().group(Tab.PHIALS), null);
	public static final Item PRIMORDIAL_PEARL = new PrimordialPearlItem(new Settings().group(Tab.MAIN).maxCount(1).rarity(Rarity.EPIC));
	
	// blocks...
	public static final Block ARCANE_CRAFTING_TABLE = new ArcaneCraftingTableBlock(of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).sounds(BlockSoundGroup.WOOD).strength(3).nonOpaque());
	public static final Block CRUCIBLE = new CrucibleBlock(of(Material.METAL).dropsSelf().requiresTool(PICKAXE_MINEABLE).sounds(BlockSoundGroup.METAL).strength(2).nonOpaque());
	public static final Block RESEARCH_TABLE = new ResearchTableBlock(of(Material.WOOD).dropsSelf().renderLayer(CUTOUT).usesTool(AXE_MINEABLE).nonOpaque().strength(3));
	public static final Block KNOWLEDGEABLE_DROPPER = new KnowledgeableDropperBlock(of(Material.STONE).dropsSelf().requiresTool(PICKAXE_MINEABLE).strength(3));
	
	public static final Block ARCANE_FURNACE = new ArcaneFurnaceBlock(of(Material.STONE).dropsSelf().requiresTool(PICKAXE_MINEABLE).strength(3).luminance(whenLit(14)));
	public static final Block ALEMBIC = new AlembicBlock(of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).strength(3).sounds(BlockSoundGroup.WOOD));
	public static final Block ESSENTIA_TUBE = new EssentiaTubeBlock(of(Material.METAL).dropsSelf().usesTool(PICKAXE_MINEABLE).strength(1).sounds(BlockSoundGroup.METAL));
	public static final Block ESSENTIA_VALVE = new EssentiaTubeBlock(of(Material.METAL).dropsSelf().usesTool(PICKAXE_MINEABLE).strength(1).sounds(BlockSoundGroup.METAL));
	public static final Block ESSENTIA_WINDOW = new EssentiaTubeBlock(of(Material.GLASS).dropsSelf().usesTool(PICKAXE_MINEABLE).strength(.7f).sounds(BlockSoundGroup.GLASS));
	public static final Block ESSENTIA_PUMP = new EssentiaPumpBlock(of(Material.METAL).dropsSelf().usesTool(PICKAXE_MINEABLE).strength(1.1f).sounds(BlockSoundGroup.METAL));
	public static final Block WARDED_JAR = new WardedJarBlock(of(Material.GLASS).dropsSelf().renderLayer(TRANSLUCENT).strength(.9f).sounds(BlockSoundGroup.GLASS));
	
	public static final Block INFUSION_PILLAR = new InfusionPillarBlock(of(Material.STONE).dropsSelf().requiresTool(PICKAXE_MINEABLE).nonOpaque().strength(4));
	public static final Block INFUSION_MATRIX = new InfusionMatrixBlock(of(Material.STONE).dropsSelf().requiresTool(PICKAXE_MINEABLE).nonOpaque().strength(5));
	
	public static final Block NITOR = new NitorBlock(of(Material.DECORATION).dropsSelf().strength(0).luminance(15));
	public static final Block HARDENED_GLASS = new GlassBlock(of(Material.GLASS).dropsSelf().usesTool(PICKAXE_MINEABLE).renderLayer(CUTOUT).strength(3, 10).sounds(BlockSoundGroup.GLASS).nonOpaque().allowsSpawning(Blocks::never).solidBlock(Blocks::never).suffocates(Blocks::never).blockVision(Blocks::never));
	public static final Block LUMINIFEROUS_GLASS = new GlassBlock(of(Material.GLASS).dropsSelf().usesTool(PICKAXE_MINEABLE).renderLayer(TRANSLUCENT).luminance(15).strength(.6f).sounds(BlockSoundGroup.GLASS).nonOpaque().allowsSpawning(Blocks::never).solidBlock(Blocks::never).suffocates(Blocks::never).blockVision(Blocks::never));
	public static final Block STATIC_GLASS = new StaticGlassBlock(of(Material.GLASS).dropsSelf().usesTool(PICKAXE_MINEABLE).renderLayer(TRANSLUCENT).strength(.6f).sounds(BlockSoundGroup.GLASS).nonOpaque().allowsSpawning(Blocks::never).solidBlock(Blocks::never).suffocates(Blocks::never).blockVision(Blocks::never));
	public static final Block PAVING_STONE_OF_TRAVEL = new PavingStoneOfTravelBlock(of(Material.STONE).dropsSelf().requiresTool(PICKAXE_MINEABLE).strength(3, 7));
	public static final Block PAVING_STONE_OF_WARDING = new PavingStoneOfWardingBlock(of(Material.STONE).dropsSelf().requiresTool(PICKAXE_MINEABLE).strength(3.5f, 7));
	public static final Block PEDESTAL = new PedestalBlock(of(Material.STONE).dropsSelf().requiresTool(PICKAXE_MINEABLE).strength(3).nonOpaque());
	public static final Block ARCANE_LEVITATOR = new ArcaneLevitatorBlock(of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).sounds(BlockSoundGroup.WOOD).strength(2));
	
	public static final Block ARCANIUM_BLOCK = new Block(of(Material.METAL, MapColor.PINK).dropsSelf().requiresTool(PICKAXE_MINEABLE).strength(5, 6).sounds(BlockSoundGroup.METAL));
	public static final Block ARCANE_STONE = new Block(of(Material.STONE).dropsSelf().requiresTool(PICKAXE_MINEABLE).strength(3, 7));
	public static final Block ARCANE_STONE_BRICKS = new Block(of(Material.STONE).dropsSelf().requiresTool(PICKAXE_MINEABLE).strength(3.5f, 7));
	
	public static final Block SILVERWOOD_SAPLING = new SaplingBlock(new SilverwoodSaplingGenerator(), of(Material.PLANT).dropsSelf().renderLayer(CUTOUT).noCollision().ticksRandomly().breakInstantly().sounds(BlockSoundGroup.GRASS));
	public static final Block SILVERWOOD_LOG = new PillarBlock(of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).strength(2).sounds(BlockSoundGroup.WOOD));
	public static final Block SILVERWOOD_LEAVES = new LeavesBlock(of(Material.LEAVES).renderLayer(CUTOUT).strength(.2f).ticksRandomly().sounds(BlockSoundGroup.GRASS).nonOpaque().allowsSpawning(Blocks::canSpawnOnLeaves).suffocates(Blocks::never).blockVision(Blocks::never));
	public static final Block SILVERWOOD_PLANKS = new Block(of(Material.WOOD).dropsSelf().strength(2, 3).sounds(BlockSoundGroup.WOOD));
	
	public static final Block SILVERWOOD_WOOD = new PillarBlock(of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).strength(2).sounds(BlockSoundGroup.WOOD));
	public static final Block STRIPPED_SILVERWOOD_LOG = new PillarBlock(of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).strength(2).sounds(BlockSoundGroup.WOOD));
	public static final Block STRIPPED_SILVERWOOD_WOOD = new PillarBlock(of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).strength(2).sounds(BlockSoundGroup.WOOD));
	
	public static final Block GREATWOOD_SAPLING = new SaplingBlock(new GreatwoodSaplingGenerator(), of(Material.PLANT).dropsSelf().renderLayer(CUTOUT).noCollision().ticksRandomly().breakInstantly().sounds(BlockSoundGroup.GRASS));
	public static final Block GREATWOOD_LOG = new PillarBlock(of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).strength(2).sounds(BlockSoundGroup.WOOD));
	public static final Block GREATWOOD_LEAVES = new LeavesBlock(of(Material.LEAVES).renderLayer(CUTOUT).strength(.2f).ticksRandomly().sounds(BlockSoundGroup.GRASS).nonOpaque().allowsSpawning(Blocks::canSpawnOnLeaves).suffocates(Blocks::never).blockVision(Blocks::never));
	public static final Block GREATWOOD_PLANKS = new Block(of(Material.WOOD).dropsSelf().strength(2, 3).sounds(BlockSoundGroup.WOOD));
	
	public static final Block GREATWOOD_WOOD = new PillarBlock(of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).strength(2).sounds(BlockSoundGroup.WOOD));
	public static final Block STRIPPED_GREATWOOD_LOG = new PillarBlock(of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).strength(2).sounds(BlockSoundGroup.WOOD));
	public static final Block STRIPPED_GREATWOOD_WOOD = new PillarBlock(of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).strength(2).sounds(BlockSoundGroup.WOOD));
	
	public static final Block LIGHT_BLOCK = new LightFocusBlock(of(Material.DECORATION).dropsNothing().breakInstantly().ticksRandomly().luminance(state -> 7 + state.get(LightFocusBlock.life)));
	
	// screen handlers...
	public static final ScreenHandlerType<ArcaneCraftingScreenHandler> ARCANE_CRAFTING_SCREEN_HANDLER
			= new ScreenHandlerType<>(ArcaneCraftingScreenHandler::new);
	public static final ScreenHandlerType<ResearchTableScreenHandler> RESEARCH_TABLE_SCREEN_HANDLER
			= new ScreenHandlerType<>(ResearchTableScreenHandler::new);
	public static final ScreenHandlerType<KnowledgeableDropperScreenHandler> KNOWLEDGEABLE_DROPPER_SCREEN_HANDLER
			= new ScreenHandlerType<>(KnowledgeableDropperScreenHandler::new);
	
	// block entities...
	public static BlockEntityType<CrucibleBlockEntity> CRUCIBLE_BE = FabricBlockEntityTypeBuilder
			.create(CrucibleBlockEntity::new, CRUCIBLE)
			.build();
	public static BlockEntityType<ResearchTableBlockEntity> RESEARCH_TABLE_BE = FabricBlockEntityTypeBuilder
			.create(ResearchTableBlockEntity::new, RESEARCH_TABLE)
			.build();
	public static BlockEntityType<KnowledgeableDropperBlockEntity> KNOWLEDGEABLE_DROPPER_BE = FabricBlockEntityTypeBuilder
			.create(KnowledgeableDropperBlockEntity::new, KNOWLEDGEABLE_DROPPER)
			.build();
	public static BlockEntityType<PedestalBlockEntity> PEDESTAL_BE = FabricBlockEntityTypeBuilder
			.create(PedestalBlockEntity::new, PEDESTAL)
			.build();
	public static BlockEntityType<ArcaneLevitatorBlockEntity> ARCANE_LEVITATOR_BE = FabricBlockEntityTypeBuilder
			.create(ArcaneLevitatorBlockEntity::new, ARCANE_LEVITATOR)
			.build();
	public static BlockEntityType<InfusionPillarBlockEntity> INFUSION_PILLAR_BE = FabricBlockEntityTypeBuilder
			.create(InfusionPillarBlockEntity::new, INFUSION_PILLAR)
			.build();
	public static BlockEntityType<InfusionMatrixBlockEntity> INFUSION_MATRIX_BE = FabricBlockEntityTypeBuilder
			.create(InfusionMatrixBlockEntity::new, INFUSION_MATRIX)
			.build();
	public static BlockEntityType<WardedJarBlockEntity> WARDED_JAR_BE = FabricBlockEntityTypeBuilder
			.create(WardedJarBlockEntity::new, WARDED_JAR)
			.build();
	
	// enchantments...
	public static Enchantment WARPING = new WarpingCurseEnchantment(Enchantment.Rarity.VERY_RARE, EquipmentSlot.values());
	public static Enchantment PROJECTING = new ProjectingEnchantment();
	
	// features...
	// TODO: move elsewhere? e.g. to each feature's class
	public static Feature<DefaultFeatureConfig> SURFACE_NODE_FEATURE = new SurfaceNodeFeature();
	public static ConfiguredFeature<?, ?> SURFACE_NODE_CONF_FEATURE = new ConfiguredFeature<>(SURFACE_NODE_FEATURE, DefaultFeatureConfig.INSTANCE);
	public static PlacedFeature SURFACE_NODE_PLACED_FEATURE = new PlacedFeature(
			RegistryEntry.of(SURFACE_NODE_CONF_FEATURE),
			List.of(HeightmapPlacementModifier.of(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES))
	);
	
	// particle types...
	public static ParticleType<BlockStateParticleEffect> HUNGRY_NODE_DISC = FabricParticleTypes.complex(BlockStateParticleEffect.PARAMETERS_FACTORY);
	public static ParticleType<BlockStateParticleEffect> HUNGRY_NODE_BLOCK = FabricParticleTypes.complex(BlockStateParticleEffect.PARAMETERS_FACTORY);
	public static ParticleType<ItemStackParticleEffect> INFUSION_ITEM = FabricParticleTypes.complex(ItemStackParticleEffect.PARAMETERS_FACTORY);
	public static ParticleType<AspectParticleEffect> ESSENTIA_STREAM = FabricParticleTypes.complex(AspectParticleEffect.PARAMETERS_FACTORY);
	
	// entities...
	public static final EntityType<ThrownAlumentumEntity> THROWN_ALUMENTUM = FabricEntityTypeBuilder
			.<ThrownAlumentumEntity>create(SpawnGroup.MISC, ThrownAlumentumEntity::new)
			.build();
	
	public static final List<Item> items = new ArrayList<>();
	public static final List<Block> blocks = new ArrayList<>();
	
	public static void setup(){
		// items + wand components
		register("scribbled_notes", SCRIBBLED_NOTES);
		register("goggles_of_revealing", GOGGLES_OF_REVEALING);
		register("monocle_of_revealing", MONOCLE_OF_REVEALING);
		
		register("arcanum", ARCANUM);
		register("crimson_rites", CRIMSON_RITES);
		register("tainted_codex", TAINTED_CODEX);
		
		register("research_notes", RESEARCH_NOTES);
		register("complete_research_notes", COMPLETE_RESEARCH_NOTES);
		
		register("tome_of_sharing", TOME_OF_SHARING);
		
		register("cheaters_arcanum", CHEATERS_ARCANUM);
		
		register("arcanium_ingot", ARCANIUM_INGOT);
		register("arcanium_sword", ARCANIUM_SWORD);
		register("arcanium_shovel", ARCANIUM_SHOVEL);
		register("arcanium_pickaxe", ARCANIUM_PICKAXE);
		register("arcanium_axe", ARCANIUM_AXE);
		register("arcanium_hoe", ARCANIUM_HOE);
		register("arcanium_helmet", ARCANIUM_HELMET);
		register("arcanium_chestplate", ARCANIUM_CHESTPLATE);
		register("arcanium_leggings", ARCANIUM_LEGGINGS);
		register("arcanium_boots", ARCANIUM_BOOTS);
		register("arcanium_ring", ARCANIUM_RING);
		
		register("boots_of_the_traveller", BOOTS_OF_THE_TRAVELLER);
		register("boots_of_the_sailor", BOOTS_OF_THE_SAILOR);
		register("boots_of_the_reaper", BOOTS_OF_THE_REAPER);
		
		register("alchemical_iron", ALCHEMICAL_IRON);
		register("alchemical_gold", ALCHEMICAL_GOLD);
		register("alchemical_copper", ALCHEMICAL_COPPER);
		register("altered_iron", ALTERED_IRON);
		register("alumentum", ALUMENTUM);
		FuelRegistry.INSTANCE.add(ALUMENTUM, 1600 * 4); // 4x coal = half stack
		
		register("wand", WAND);
		
		register("iron_wand_cap", IRON_WAND_CAP);
		register("gold_wand_cap", GOLD_WAND_CAP);
		register("copper_wand_cap", COPPER_WAND_CAP);
		register("leather_wand_cap", LEATHER_WAND_CAP);
		register("thaumium_wand_cap", THAUMIUM_WAND_CAP);
		register("bamboo_wand_cap", BAMBOO_WAND_CAP);
		register("quartz_wand_cap", QUARTZ_WAND_CAP);
		register("prismarine_wand_cap", PRISMARINE_WAND_CAP);
		register("amber_wand_cap", AMBER_WAND_CAP);
		register("honeycomb_wand_cap", HONEYCOMB_WAND_CAP);
		register("netherite_wand_cap", NETHERITE_WAND_CAP);
		register("mechanical_wand_cap", MECHANICAL_WAND_CAP);
		register("void_wand_cap", VOID_WAND_CAP);
		register("crimson_wand_cap", CRIMSON_WAND_CAP);
		registerCapOnly(MISSING_CAP);
		
		registerCoreOnly(STICK_CORE);
		register("greatwood_wand_core", GREATWOOD_WAND_CORE);
		register("nether_stem_wand_core", NETHER_STEM_WAND_CORE);
		register("varnished_wood_wand_core", VARNISHED_WOOD_WAND_CORE);
		register("silverwood_wand_core", SILVERWOOD_WAND_CORE);
		register("sugar_cane_wand_core", SUGAR_CANE_WAND_CORE);
		register("blaze_wand_core", BLAZE_WAND_CORE);
		register("ice_wand_core", ICE_WAND_CORE);
		register("obsidian_wand_core", OBSIDIAN_WAND_CORE);
		register("arcane_stone_wand_core", ARCANE_STONE_WAND_CORE);
		register("bone_wand_core", BONE_WAND_CORE);
		register("arcanium_wand_core", ARCANIUM_WAND_CORE);
		register("mechanical_wand_core", MECHANICAL_WAND_CORE);
		register("tainted_wand_core", TAINTED_WAND_CORE);
		register("eldritch_wand_core", ELDRITCH_WAND_CORE);
		registerCoreOnly(MISSING_CORE);
		
		register("fire_focus", FIRE_FOCUS);
		register("portable_hole_focus", PORTABLE_HOLE_FOCUS);
		register("light_focus", LIGHT_FOCUS);
		register("prismatic_light_focus", PRISMATIC_LIGHT_FOCUS);
		register("equivalent_exchange_focus", EQUIVALENT_EXCHANGE_FOCUS);
		
		register("empty_phial", EMPTY_PHIAL);
		register("primordial_pearl", PRIMORDIAL_PEARL);
		
		for(Aspect aspect : Aspects.getOrderedAspects()){
			var shortName = aspect.id().getPath();
			CrystalItem crystalItem = new CrystalItem(new Settings().group(Tab.CRYSTALS), aspect);
			register("crystals/" + shortName, crystalItem);
			Aspects.crystals.put(aspect, crystalItem);
			
			PhialItem phialItem = new PhialItem(new Settings().group(Tab.PHIALS), aspect);
			register("phials/" + shortName, phialItem);
			Aspects.phials.put(aspect, phialItem);
		}
		
		// blocks
		register("arcane_crafting_table", ARCANE_CRAFTING_TABLE);
		register("crucible", CRUCIBLE);
		register("research_table", RESEARCH_TABLE, false);
		register("research_table", new ResearchTableItem(GROUPED)); // it's a block item, it doesn't count
		register("knowledgeable_dropper", KNOWLEDGEABLE_DROPPER);
		
		register("arcane_furnace", ARCANE_FURNACE);
		register("alembic", ALEMBIC);
		register("essentia_tube", ESSENTIA_TUBE);
		register("essentia_valve", ESSENTIA_VALVE);
		register("essentia_window", ESSENTIA_WINDOW);
		register("essentia_pump", ESSENTIA_PUMP);
		register("warded_jar", WARDED_JAR);
		
		register("infusion_pillar", INFUSION_PILLAR);
		register("infusion_matrix", INFUSION_MATRIX);
		
		register("nitor", NITOR);
		register("hardened_glass", HARDENED_GLASS);
		register("luminiferous_glass", LUMINIFEROUS_GLASS);
		register("static_glass", STATIC_GLASS);
		register("paving_stone_of_travel", PAVING_STONE_OF_TRAVEL);
		register("paving_stone_of_warding", PAVING_STONE_OF_WARDING);
		register("pedestal", PEDESTAL);
		register("arcane_levitator", ARCANE_LEVITATOR);
		
		register("arcanium_block", ARCANIUM_BLOCK);
		register("arcane_stone", ARCANE_STONE);
		register("arcane_stone_bricks", ARCANE_STONE_BRICKS);
		
		register("silverwood_sapling", SILVERWOOD_SAPLING);
		register("silverwood_log", SILVERWOOD_LOG);
		register("silverwood_leaves", SILVERWOOD_LEAVES);
		register("silverwood_planks", SILVERWOOD_PLANKS);
		
		register("silverwood_wood", SILVERWOOD_WOOD);
		register("stripped_silverwood_log", STRIPPED_SILVERWOOD_LOG);
		register("stripped_silverwood_wood", STRIPPED_SILVERWOOD_WOOD);
		StrippableBlockRegistry.register(SILVERWOOD_LOG, STRIPPED_SILVERWOOD_LOG);
		StrippableBlockRegistry.register(SILVERWOOD_WOOD, STRIPPED_SILVERWOOD_WOOD);
		
		register("greatwood_sapling", GREATWOOD_SAPLING);
		register("greatwood_log", GREATWOOD_LOG);
		register("greatwood_leaves", GREATWOOD_LEAVES);
		register("greatwood_planks", GREATWOOD_PLANKS);
		
		register("greatwood_wood", GREATWOOD_WOOD);
		register("stripped_greatwood_log", STRIPPED_GREATWOOD_LOG);
		register("stripped_greatwood_wood", STRIPPED_GREATWOOD_WOOD);
		StrippableBlockRegistry.register(GREATWOOD_LOG, STRIPPED_GREATWOOD_LOG);
		StrippableBlockRegistry.register(GREATWOOD_WOOD, STRIPPED_GREATWOOD_WOOD);
		
		for(Aspect primal : Aspects.hasCluster){
			CrystalClusterBlock clusterBlock = new CrystalClusterBlock(
					of(Material.GLASS)
							.renderLayer(CUTOUT)
							.usesTool(PICKAXE_MINEABLE)
							.nonOpaque()
							.noCollision()
							.ticksRandomly()
							.sounds(BlockSoundGroup.AMETHYST_CLUSTER)
							.strength(1.5f)
							.luminance(5),
					primal);
			var shortName = primal.id().getPath();
			register("clusters/" + shortName, clusterBlock);
			Aspects.clusters.put(primal, clusterBlock);
			
			ClusterSeedItem seed = new ClusterSeedItem(clusterBlock, GROUPED, primal);
			register("cluster_seeds/" + shortName, seed);
			Aspects.clusterSeeds.put(primal, seed);
		}
		
		register("light_block", LIGHT_BLOCK, false);
		
		// screen handlers
		register("arcane_crafting", ARCANE_CRAFTING_SCREEN_HANDLER);
		register("research_table", RESEARCH_TABLE_SCREEN_HANDLER);
		register("knowledgeable_dropper", KNOWLEDGEABLE_DROPPER_SCREEN_HANDLER);
		
		// block entities
		register("crucible", CRUCIBLE_BE);
		register("research_table", RESEARCH_TABLE_BE);
		register("knowledgeable_dropper", KNOWLEDGEABLE_DROPPER_BE);
		register("pedestal", PEDESTAL_BE);
		register("arcane_levitator", ARCANE_LEVITATOR_BE);
		register("infusion_pillar", INFUSION_PILLAR_BE);
		register("infusion_matrix", INFUSION_MATRIX_BE);
		register("warded_jar", WARDED_JAR_BE);
		
		// enchantments
		register("warping", WARPING);
		register("projecting", PROJECTING);
		
		// features
		register("surface_node", SURFACE_NODE_FEATURE);
		register("surface_node", SURFACE_NODE_CONF_FEATURE);
		register("surface_node", SURFACE_NODE_PLACED_FEATURE);
		
		register("nodal_geode", NodalGeodes.NODAL_GEODE_FEATURE);
		register("air_geode", NodalGeodes.AIR_GEODE);
		register("air_geode", NodalGeodes.PLACED_AIR_GEODE);
		register("fire_geode", NodalGeodes.FIRE_GEODE);
		register("fire_geode", NodalGeodes.PLACED_FIRE_GEODE);
		register("water_geode", NodalGeodes.WATER_GEODE);
		register("water_geode", NodalGeodes.PLACED_WATER_GEODE);
		register("earth_geode", NodalGeodes.EARTH_GEODE);
		register("earth_geode", NodalGeodes.PLACED_EARTH_GEODE);
		register("order_geode", NodalGeodes.ORDER_GEODE);
		register("order_geode", NodalGeodes.PLACED_ORDER_GEODE);
		register("entropy_geode", NodalGeodes.ENTROPY_GEODE);
		register("entropy_geode", NodalGeodes.PLACED_ENTROPY_GEODE);
		
		register("silverwood_foliage", SilverwoodFoliagePlacer.TYPE);
		register("silverwood_trunk", SilverwoodTrunkPlacer.TYPE);
		register("silverwood_tree", SilverwoodTree.SILVERWOOD_TREE);
		register("silverwood_tree", SilverwoodTree.SCATTERED_SILVERWOOD_TREE);
		
		register("greatwood_foliage", GreatwoodFoliagePlacer.TYPE);
		register("greatwood_trunk", GreatwoodTrunkPlacer.TYPE);
		register("greatwood_tree", GreatwoodTree.GREATWOOD_TREE);
		register("greatwood_tree", GreatwoodTree.SCATTERED_GREATWOOD_TREE);
		
		// particle types
		register("hungry_node_disc", HUNGRY_NODE_DISC);
		register("hungry_node_block", HUNGRY_NODE_BLOCK);
		register("infusion_item", INFUSION_ITEM);
		register("essentia_stream", ESSENTIA_STREAM);
		
		// entity types
		register("thrown_alumentum", THROWN_ALUMENTUM);
	}
	
	private static void register(String name, Item item){
		Registry.register(Registry.ITEM, arcId(name), item);
		items.add(item);
		if(item instanceof Cap c)
			registerCapOnly(c);
		if(item instanceof Core c)
			registerCoreOnly(c);
	}
	
	private static void register(String name, Block block){
		register(name, block, true);
	}
	
	private static void register(String name, Block block, boolean andItem){
		Registry.register(Registry.BLOCK, arcId(name), block);
		blocks.add(block);
		if(andItem)
			register(name, new BlockItem(block, GROUPED));
	}
	
	private static void register(String name, ScreenHandlerType<?> type){
		Registry.register(Registry.SCREEN_HANDLER, arcId(name), type);
	}
	
	private static void register(String name, BlockEntityType<?> type){
		Registry.register(Registry.BLOCK_ENTITY_TYPE, arcId(name), type);
	}
	
	private static void register(String name, Enchantment enchantment){
		Registry.register(Registry.ENCHANTMENT, arcId(name), enchantment);
	}
	
	private static void register(String name, Feature<?> feature){
		Registry.register(Registry.FEATURE, arcId(name), feature);
	}
	
	private static void register(String name, ConfiguredFeature<?, ?> feature){
		Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, arcId(name), feature);
	}
	
	private static void register(String name, PlacedFeature feature){
		Registry.register(BuiltinRegistries.PLACED_FEATURE, arcId(name), feature);
	}
	
	private static void register(String name, FoliagePlacerType<?> foliagePlacer){
		Registry.register(Registry.FOLIAGE_PLACER_TYPE, arcId(name), foliagePlacer);
	}
	
	private static void register(String name, TrunkPlacerType<?> trunkPlacer){
		Registry.register(Registry.TRUNK_PLACER_TYPE, arcId(name), trunkPlacer);
	}
	
	private static void register(String name, ParticleType<?> particleType){
		Registry.register(Registry.PARTICLE_TYPE, arcId(name), particleType);
	}
	
	private static void register(String name, EntityType<?> entityType){
		Registry.register(Registry.ENTITY_TYPE, arcId(name), entityType);
	}
	
	private static void registerCapOnly(Cap cap){
		Cap.caps.put(cap.id(), cap);
	}
	
	private static void registerCoreOnly(Core core){
		Core.cores.put(core.id(), core);
	}
	
	private static ToIntFunction<BlockState> whenLit(int litLevel){
		return state -> state.get(Properties.LIT) ? litLevel : 0;
	}
}