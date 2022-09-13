package arcana;

import arcana.aspects.Aspect;
import arcana.aspects.Aspects;
import arcana.blocks.*;
import arcana.enchantments.WarpingCurseEnchantment;
import arcana.items.*;
import arcana.items.foci.FireFocusItem;
import arcana.screens.ArcaneCraftingScreenHandler;
import arcana.screens.KnowledgeableDropperScreenHandler;
import arcana.screens.ResearchTableScreenHandler;
import arcana.worldgen.SurfaceNodeFeature;
import arcana.worldgen.geodes.NodalGeodes;
import arcana.worldgen.silverwood.SilverwoodSaplingGenerator;
import com.unascribed.lib39.fractal.api.ItemSubGroup;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.fabricmc.fabric.api.registry.StrippableBlockRegistry;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.item.Item.Settings;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.placementmodifier.HeightmapPlacementModifier;

import java.util.ArrayList;
import java.util.List;

import static arcana.Arcana.arcId;
import static arcana.blocks.ArcanaBlockSettings.BlockLayer.CUTOUT;
import static arcana.blocks.ArcanaBlockSettings.of;
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
	}
	
	private static final Settings GROUPED = new Settings().group(Tab.MAIN);
	private static final Settings GROUPED_SINGLE = new Settings().group(Tab.MAIN).maxCount(1);
	
	// items...
	public static final Item SCRIBBLED_NOTES = new ScribbledNotesItem(GROUPED_SINGLE);
	public static final Item GOGGLES_OF_REVEALING = new GogglesOfRevealingItem(new Settings().group(Tab.MAIN).maxCount(1));
	
	public static final Item ARCANUM = new ResearchBookItem(GROUPED_SINGLE, arcId("arcanum"));
	public static final Item CRIMSON_RITES = new ResearchBookItem(GROUPED_SINGLE, arcId("crimson_rites"));
	public static final Item TAINTED_CODEX = new ResearchBookItem(GROUPED_SINGLE, arcId("tainted_codex"));
	
	public static final Item RESEARCH_NOTES = new ResearchNotesItem(new Settings().maxCount(1), false);
	public static final Item COMPLETE_RESEARCH_NOTES = new ResearchNotesItem(new Settings().maxCount(1), true);
	
	public static final Item TOME_OF_SHARING = new TomeOfSharingItem(GROUPED_SINGLE);
	
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
	
	public static final Item ALCHEMICAL_IRON = new Item(GROUPED);
	public static final Item ALCHEMICAL_GOLD = new Item(GROUPED);
	public static final Item ALCHEMICAL_COPPER = new Item(GROUPED);
	public static final Item ALTERED_IRON = new Item(GROUPED);
	public static final Item ALUMENTUM = new Item(GROUPED);
	
	public static final CapItem IRON_WAND_CAP = new CapItem(GROUPED);
	public static final CapItem COPPER_WAND_CAP = new CapItem(GROUPED);
	public static final CapItem GOLD_WAND_CAP = new CapItem(GROUPED);
	public static final Cap MISSING_CAP = new Cap.Impl(arcId("missing"));
	
	public static final CoreItem BONE_WAND_CORE = new CoreItem(GROUPED, 1);
	public static final CoreItem BLAZE_WAND_CORE = new CoreItem(GROUPED);
	public static final CoreItem ICE_WAND_CORE = new CoreItem(GROUPED);
	public static final Core STICK_CORE = new Core.Impl(arcId("stick_wand_core"));
	public static final Core MISSING_CORE = new Core.Impl(arcId("missing"));
	
	public static final Item WAND = new WandItem(GROUPED_SINGLE);
	
	public static final Item FIRE_FOCUS = new FireFocusItem(GROUPED_SINGLE);
	public static final Item PRISMATIC_LIGHT_FOCUS = new FocusItem(GROUPED_SINGLE);
	
	// blocks...
	public static final Block ARCANE_CRAFTING_TABLE = new ArcaneCraftingTableBlock(of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).strength(3).nonOpaque());
	public static final Block CRUCIBLE = new CrucibleBlock(of(Material.METAL).dropsSelf().requiresTool(PICKAXE_MINEABLE).strength(2).nonOpaque());
	public static final Block RESEARCH_TABLE = new ResearchTableBlock(of(Material.WOOD).dropsSelf().renderLayer(CUTOUT).usesTool(AXE_MINEABLE).nonOpaque().strength(3));
	public static final Block KNOWLEDGEABLE_DROPPER = new KnowledgeableDropperBlock(of(Material.STONE).dropsSelf().requiresTool(PICKAXE_MINEABLE).strength(3));
	
	public static final Block NITOR = new NitorBlock(of(Material.DECORATION).dropsSelf().strength(0).luminance(15));
	
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
	
	// enchantments...
	public static Enchantment WARPING = new WarpingCurseEnchantment(Enchantment.Rarity.VERY_RARE, EquipmentSlot.values());
	
	// features...
	// TODO: move elsewhere? e.g. to each feature's class
	public static Feature<DefaultFeatureConfig> SURFACE_NODE_FEATURE = new SurfaceNodeFeature();
	public static ConfiguredFeature<?, ?> SURFACE_NODE_CONF_FEATURE = new ConfiguredFeature<>(SURFACE_NODE_FEATURE, DefaultFeatureConfig.INSTANCE);
	public static PlacedFeature SURFACE_NODE_PLACED_FEATURE = new PlacedFeature(
			RegistryEntry.of(SURFACE_NODE_CONF_FEATURE),
			List.of(HeightmapPlacementModifier.of(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES))
	);
	
	public static final List<Item> items = new ArrayList<>();
	public static final List<Block> blocks = new ArrayList<>();
	
	public static void setup(){
		// items + wand components
		register("scribbled_notes", SCRIBBLED_NOTES);
		register("goggles_of_revealing", GOGGLES_OF_REVEALING);
		
		register("arcanum", ARCANUM);
		register("crimson_rites", CRIMSON_RITES);
		register("tainted_codex", TAINTED_CODEX);
		
		register("research_notes", RESEARCH_NOTES);
		register("complete_research_notes", COMPLETE_RESEARCH_NOTES);
		
		register("tome_of_sharing", TOME_OF_SHARING);
		
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
		
		register("alchemical_iron", ALCHEMICAL_IRON);
		register("alchemical_gold", ALCHEMICAL_GOLD);
		register("alchemical_copper", ALCHEMICAL_COPPER);
		register("altered_iron", ALTERED_IRON);
		register("alumentum", ALUMENTUM);
		FuelRegistry.INSTANCE.add(ALUMENTUM, 1600 * 4); // 4x coal = half stack
		
		register("iron_wand_cap", IRON_WAND_CAP);
		register("copper_wand_cap", COPPER_WAND_CAP);
		register("gold_wand_cap", GOLD_WAND_CAP);
		registerCapOnly(MISSING_CAP);
		
		register("bone_wand_core", BONE_WAND_CORE);
		register("blaze_wand_core", BLAZE_WAND_CORE);
		register("ice_wand_core", ICE_WAND_CORE);
		registerCoreOnly(STICK_CORE);
		registerCoreOnly(MISSING_CORE);
		
		register("wand", WAND);
		
		register("prismatic_light_focus", PRISMATIC_LIGHT_FOCUS);
		register("fire_focus", FIRE_FOCUS);
		
		for(Aspect aspect : Aspects.getOrderedAspects()){
			CrystalItem crystalItem = new CrystalItem(new Settings().group(Tab.CRYSTALS), aspect);
			register("crystals/" + aspect.id().getPath(), crystalItem);
			Aspects.crystals.put(aspect, crystalItem);
		}
		
		// blocks
		register("arcane_crafting_table", ARCANE_CRAFTING_TABLE);
		register("crucible", CRUCIBLE);
		register("research_table", RESEARCH_TABLE, false);
		register("research_table", new ResearchTableItem(GROUPED)); // it's a block item, it doesn't count
		register("knowledgeable_dropper", KNOWLEDGEABLE_DROPPER);
		
		register("nitor", NITOR);
		
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
		
		for(Aspect primal : Aspects.primals){
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
			register("clusters/" + primal.id().getPath(), clusterBlock);
			Aspects.clusters.put(primal, clusterBlock);
		}
		
		// screen handlers
		register("arcane_crafting", ARCANE_CRAFTING_SCREEN_HANDLER);
		register("research_table", RESEARCH_TABLE_SCREEN_HANDLER);
		register("knowledgeable_dropper", KNOWLEDGEABLE_DROPPER_SCREEN_HANDLER);
		
		// block entities
		register("crucible", CRUCIBLE_BE);
		register("research_table", RESEARCH_TABLE_BE);
		register("knowledgeable_dropper", KNOWLEDGEABLE_DROPPER_BE);
		
		// enchantments
		register("warping", WARPING);
		
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
	
	private static void registerCapOnly(Cap cap){
		Cap.caps.put(cap.id(), cap);
	}
	
	private static void registerCoreOnly(Core core){
		Core.cores.put(core.id(), core);
	}
}