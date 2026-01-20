package arcana;

import arcana.aspects.Aspect;
import arcana.aspects.Aspects;
import arcana.blocks.*;
import arcana.blocks.be.*;
import arcana.blocks.tainted.TaintedBlock;
import arcana.blocks.tainted.TaintedFallingBlock;
import arcana.blocks.tainted.TaintedSnowyBlock;
import arcana.blocks.tubes.*;
import arcana.client.particles.AspectParticleEffect;
import arcana.client.particles.CubeParticleEffect;
import arcana.components.RunicShielding;
import arcana.effects.*;
import arcana.enchantments.LootSwapEnchantment;
import arcana.enchantments.ProjectingEnchantment;
import arcana.enchantments.RunicShieldingEnchantment;
import arcana.enchantments.WarpingCurseEnchantment;
import arcana.entities.PrismaticOrbEntity;
import arcana.entities.ThrownAlumentumEntity;
import arcana.entities.ThrownTaintBottleEntity;
import arcana.entities.crimson.*;
import arcana.entities.wisps.CoagulationEntity;
import arcana.entities.wisps.PureWispEntity;
import arcana.entities.wisps.TaintedWispEntity;
import arcana.entities.wisps.WispEntity;
import arcana.fluids.ArcanaFluid;
import arcana.fluids.PutrefactionFluid;
import arcana.fluids.TaintGooFluid;
import arcana.items.*;
import arcana.items.creative.FluxSpongeItem;
import arcana.items.creative.NodePlacerItem;
import arcana.items.creative.NodeRemoverItem;
import arcana.items.creative.TaintConverterItem;
import arcana.items.foci.*;
import arcana.screens.*;
import arcana.util.TagGiftEntry;
import arcana.worldgen.HangingNodeFeature;
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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import com.unascribed.lib39.fractal.api.ItemSubGroup;
import com.unascribed.lib39.weld.api.BigBlock;
import com.unascribed.lib39.weld.api.BigBlockItem;
import dev.emi.trinkets.api.TrinketItem;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.sign.SignTypeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.world.poi.PointOfInterestHelper;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.fabricmc.fabric.api.registry.StrippableBlockRegistry;
import net.minecraft.block.*;
import net.minecraft.block.entity.BannerPattern;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.*;
import net.minecraft.item.Item.Settings;
import net.minecraft.loot.entry.LootPoolEntryType;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.property.Properties;
import net.minecraft.structure.StructureSet;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolElement;
import net.minecraft.structure.pool.StructurePools;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.SignType;
import net.minecraft.util.registry.*;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureSpawns;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.StructureTerrainAdaptation;
import net.minecraft.world.gen.YOffset;
import net.minecraft.world.gen.chunk.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.gen.chunk.placement.SpreadType;
import net.minecraft.world.gen.chunk.placement.StructurePlacement;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.foliage.FoliagePlacerType;
import net.minecraft.world.gen.heightprovider.ConstantHeightProvider;
import net.minecraft.world.gen.placementmodifier.BiomePlacementModifier;
import net.minecraft.world.gen.placementmodifier.HeightmapPlacementModifier;
import net.minecraft.world.gen.placementmodifier.RarityFilterPlacementModifier;
import net.minecraft.world.gen.placementmodifier.SquarePlacementModifier;
import net.minecraft.world.gen.structure.JigsawStructure;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.trunk.TrunkPlacerType;
import net.minecraft.world.poi.PointOfInterestType;

import java.util.*;
import java.util.function.ToIntFunction;

import static arcana.Arcana.arcId;
import static arcana.blocks.ArcanaBlockSettings.BlockLayer.CUTOUT;
import static arcana.blocks.ArcanaBlockSettings.BlockLayer.TRANSLUCENT;
import static arcana.blocks.ArcanaBlockSettings.of;
import static arcana.items.CapItem.capProperties;
import static arcana.items.CoreItem.coreProperties;
import static net.minecraft.tag.BlockTags.*;

public final class ArcanaRegistry{
	
	public static class Tab{
		public static final ItemGroup ARCANA = FabricItemGroupBuilder.build(
				arcId("group"),
				() -> new ItemStack(ARCANUM)
		);
		public static final ItemSubGroup MAIN = ItemSubGroup.create(ARCANA, arcId("main"));
		public static final ItemSubGroup RESOURCES = ItemSubGroup.create(ARCANA, arcId("resources"));
		public static final ItemSubGroup EQUIPMENT = ItemSubGroup.create(ARCANA, arcId("equipment"));
		public static final ItemSubGroup WANDS = ItemSubGroup.create(ARCANA, arcId("wands"));
		public static final ItemSubGroup CRYSTALS = ItemSubGroup.create(ARCANA, arcId("crystals"));
		public static final ItemSubGroup PHIALS = ItemSubGroup.create(ARCANA, arcId("phials"));
		public static final ItemSubGroup TAINTED = ItemSubGroup.create(ARCANA, arcId("tainted"));
		public static final ItemSubGroup CREATIVE = ItemSubGroup.create(ARCANA, arcId("creative"));
	}
	
	private static final Settings GROUPED = new Settings().group(Tab.MAIN);
	private static final Settings GROUPED_SINGLE = new Settings().group(Tab.MAIN).maxCount(1);
	
	private static final Settings GROUPED_RES = new Settings().group(Tab.RESOURCES);
	
	private static final Settings GROUPED_WAND = new Settings().group(Tab.WANDS);
	private static final Settings GROUPED_WAND_SINGLE = new Settings().group(Tab.WANDS).maxCount(1);
	
	private static final Settings GROUPED_CREATIVE_SINGLE = new Settings().group(Tab.CREATIVE).maxCount(1).rarity(Rarity.EPIC);
	
	// fluids...
	public static final FlowableFluid STILL_TAINT_GOO = new TaintGooFluid(true);
	public static final FlowableFluid FLOWING_TAINT_GOO = new TaintGooFluid(false);
	
	public static final FlowableFluid STILL_PUTREFACTION = new PutrefactionFluid(true);
	public static final FlowableFluid FLOWING_PUTREFACTION = new PutrefactionFluid(false);
	
	// status effects...
	public static final StatusEffect TAINTED = new TaintedStatusEffect();
	public static final StatusEffect WARP_FRAIL = new FrailWarpStatusEffect();
	
	public static final StatusEffect ARCANE_AURA = new SetBonusStatusEffect();
	
	public static final StatusEffect ARCANE_DISCHARGE = new ArcanaStatusEffect(StatusEffectCategory.BENEFICIAL, 0xF881D6);
	public static final StatusEffect WARP_WARD = new ArcanaStatusEffect(StatusEffectCategory.BENEFICIAL, 0xBFEBF8);
	public static final StatusEffect AIR_POWER = new AspectPowerStatusEffect(Aspects.AIR)
			.addAttributeModifier(EntityAttributes.GENERIC_MOVEMENT_SPEED, "63c5f0ac-285e-42b7-9744-32d527655214", .1f, EntityAttributeModifier.Operation.MULTIPLY_TOTAL);
	public static final StatusEffect FIRE_POWER = new AspectPowerStatusEffect(Aspects.FIRE);
	public static final StatusEffect WATER_POWER = new AspectPowerStatusEffect(Aspects.WATER);
	public static final StatusEffect EARTH_POWER = new AspectPowerStatusEffect(Aspects.EARTH)
			.addAttributeModifier(EntityAttributes.GENERIC_ATTACK_SPEED, "fa52fb6d-66c8-4e3a-9afd-dcc8de1114b5", .1f, EntityAttributeModifier.Operation.MULTIPLY_TOTAL);
	public static final StatusEffect ORDER_POWER = new AspectPowerStatusEffect(Aspects.ORDER)
			.addAttributeModifier(EntityAttributes.GENERIC_ARMOR, "518d94ba-0c3c-4706-89c6-ed2c47437e53", 2, EntityAttributeModifier.Operation.ADDITION)
			.addAttributeModifier(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, "6aef6e54-29b8-4cfc-a219-2fdcf22cc557", .1f, EntityAttributeModifier.Operation.ADDITION);
	public static final StatusEffect ENTROPY_POWER = new AspectPowerStatusEffect(Aspects.ENTROPY)
			.addAttributeModifier(EntityAttributes.GENERIC_ATTACK_DAMAGE, "acf64683-f1b5-4518-bd64-5ffb72918ab6", .1f, EntityAttributeModifier.Operation.MULTIPLY_TOTAL);
	
	public static final StatusEffect PRESSURE = new PressureStatusEffect();
	
	// items...
	public static final Item SCRIBBLED_NOTES = new ScribbledNotesItem(GROUPED_SINGLE);
	public static final Item SCRIBING_TOOLS = new Item(new Settings().group(Tab.MAIN).maxDamage(200));
	public static final Item GOGGLES_OF_REVEALING = new GogglesOfRevealingItem(new Settings().group(Tab.MAIN).maxCount(1));
	public static final Item MONOCLE_OF_REVEALING = new TrinketItem(GROUPED_SINGLE);
	public static final Item INTROSPECTIVE_LENS = new Item(GROUPED_SINGLE);
	public static final Item REVELATORY_LENS = new Item(GROUPED_SINGLE);
	public static final Item FLUX_LENS = new Item(GROUPED_SINGLE);
	
	public static final Item ARCANUM = new ResearchBookItem(GROUPED_SINGLE, arcId("arcanum"));
	public static final Item CRIMSON_RITES = new ResearchBookItem(GROUPED_SINGLE, arcId("crimson_rites"));
	public static final Item TOME_OF_SHARING = new TomeOfSharingItem(GROUPED_SINGLE);
	public static final Item CHEATERS_ARCANUM = new CheatersArcanumItem(GROUPED_SINGLE);
	
	public static final Item RESEARCH_NOTES = new ResearchNotesItem(new Settings().maxCount(1), false);
	public static final Item COMPLETE_RESEARCH_NOTES = new ResearchNotesItem(new Settings().maxCount(1), true);
	
	public static final Item TAINT_GOO_BUCKET = new BucketItem(STILL_TAINT_GOO, new Settings().group(Tab.MAIN).maxCount(1).recipeRemainder(Items.BUCKET));
	public static final Item PUTREFACTION_BUCKET = new BucketItem(STILL_PUTREFACTION, new Settings().group(Tab.MAIN).maxCount(1).recipeRemainder(Items.BUCKET));
	
	public static final Item FLUX_METER = new Item(GROUPED_SINGLE);
	public static final Item TAINT_IN_A_BOTTLE = new TaintInABottleItem(GROUPED);
	public static final Item DRINKABLE_TAINT = new DrinkableTaintItem(new Settings().group(Tab.MAIN).maxCount(1).food(new FoodComponent.Builder()
			.hunger(4)
			.saturationModifier(1.1f)
			.statusEffect(new StatusEffectInstance(TAINTED, 40 * 20, 1), 1)
			.build()));
	
	public static final Item RAREFIED_SHERBERT = new Item(new Settings().group(Tab.MAIN).food(aspectCandyFood(AIR_POWER)));
	public static final Item SOBERING_SYRUP = new Item(new Settings().group(Tab.MAIN).food(aspectCandyFood(FIRE_POWER)));
	public static final Item SEAFOAM_SODA = new Item(new Settings().group(Tab.MAIN).food(aspectCandyFood(WATER_POWER)));
	public static final Item BEDROCK_CANDY = new Item(new Settings().group(Tab.MAIN).food(aspectCandyFood(EARTH_POWER)));
	public static final Item GUMMY_CUBES = new Item(new Settings().group(Tab.MAIN).food(aspectCandyFood(ORDER_POWER)));
	public static final Item TWISTED_LIQUORICE = new Item(new Settings().group(Tab.MAIN).food(aspectCandyFood(ENTROPY_POWER)));
	
	public static final Item SILVERLEAF_BREW = new DrinkItem(new Settings().group(Tab.MAIN).maxCount(1).food(new FoodComponent.Builder()
			.hunger(2)
			.saturationModifier(0.25f)
			.alwaysEdible()
			.statusEffect(new StatusEffectInstance(WARP_WARD, 8 * 60 * 20, 0, true, true), 1)
			.build()));
	
	public static final Item ARCANIUM_INGOT = new Item(GROUPED_RES);
	public static final Item ARCANIUM_SWORD = new SwordItem(ArcanaToolMaterials.ARCANIUM, 3, -2.4f, new Settings().group(Tab.EQUIPMENT));
	public static final Item ARCANIUM_SHOVEL = new ShovelItem(ArcanaToolMaterials.ARCANIUM, 1.5f, -3, new Settings().group(Tab.EQUIPMENT));
	public static final Item ARCANIUM_PICKAXE = new PickaxeItem(ArcanaToolMaterials.ARCANIUM, 1, -2.8f, new Settings().group(Tab.EQUIPMENT));
	public static final Item ARCANIUM_AXE = new AxeItem(ArcanaToolMaterials.ARCANIUM, 5.5f, -3, new Settings().group(Tab.EQUIPMENT));
	public static final Item ARCANIUM_HOE = new HoeItem(ArcanaToolMaterials.ARCANIUM, -2, -1, new Settings().group(Tab.EQUIPMENT));
	public static final Item ARCANIUM_HELMET = new ArmorItem(ArcanaArmourMaterials.ARCANIUM, EquipmentSlot.HEAD, new Settings().group(Tab.EQUIPMENT));
	public static final Item ARCANIUM_CHESTPLATE = new ArmorItem(ArcanaArmourMaterials.ARCANIUM, EquipmentSlot.CHEST, new Settings().group(Tab.EQUIPMENT));
	public static final Item ARCANIUM_LEGGINGS = new ArmorItem(ArcanaArmourMaterials.ARCANIUM, EquipmentSlot.LEGS, new Settings().group(Tab.EQUIPMENT));
	public static final Item ARCANIUM_BOOTS = new ArmorItem(ArcanaArmourMaterials.ARCANIUM, EquipmentSlot.FEET, new Settings().group(Tab.EQUIPMENT));
	
	public static final Item THAUMIUM_INGOT = new Item(GROUPED_RES);
	public static final Item THAUMIUM_NUGGET = new Item(GROUPED_RES);
	
	public static final Item VOID_METAL_INGOT = new Item(GROUPED_RES);
	public static final Item VOID_METAL_NUGGET = new Item(GROUPED_RES);
	public static final Item VOID_SEED = new Item(GROUPED_RES);
	public static final Item VOID_METAL_SWORD = new SwordItem(ArcanaToolMaterials.VOID_METAL, 3, -2.4f, new Settings().group(Tab.EQUIPMENT));
	public static final Item VOID_METAL_SHOVEL = new ShovelItem(ArcanaToolMaterials.VOID_METAL, 1.5f, -3, new Settings().group(Tab.EQUIPMENT));
	public static final Item VOID_METAL_PICKAXE = new PickaxeItem(ArcanaToolMaterials.VOID_METAL, 1, -2.8f, new Settings().group(Tab.EQUIPMENT));
	public static final Item VOID_METAL_AXE = new AxeItem(ArcanaToolMaterials.VOID_METAL, 5.5f, -3, new Settings().group(Tab.EQUIPMENT));
	public static final Item VOID_METAL_HOE = new HoeItem(ArcanaToolMaterials.VOID_METAL, -2, -1, new Settings().group(Tab.EQUIPMENT));
	public static final Item VOID_METAL_HELMET = new ArmorItem(ArcanaArmourMaterials.VOID_METAL, EquipmentSlot.HEAD, new Settings().group(Tab.EQUIPMENT));
	public static final Item VOID_METAL_CHESTPLATE = new ArmorItem(ArcanaArmourMaterials.VOID_METAL, EquipmentSlot.CHEST, new Settings().group(Tab.EQUIPMENT));
	public static final Item VOID_METAL_LEGGINGS = new ArmorItem(ArcanaArmourMaterials.VOID_METAL, EquipmentSlot.LEGS, new Settings().group(Tab.EQUIPMENT));
	public static final Item VOID_METAL_BOOTS = new ArmorItem(ArcanaArmourMaterials.VOID_METAL, EquipmentSlot.FEET, new Settings().group(Tab.EQUIPMENT));
	
	public static final Item SILVERLEAF = new Item(GROUPED_RES);
	public static final Item SILVERLEAF_AMALGAMATE = new Item(GROUPED_RES);
	public static final Item SILVERLEAF_SWORD = new SwordItem(ArcanaToolMaterials.SILVERLEAF, 3, -2.4f, new Settings().group(Tab.EQUIPMENT));
	public static final Item SILVERLEAF_SHOVEL = new ShovelItem(ArcanaToolMaterials.SILVERLEAF, 1.5f, -3, new Settings().group(Tab.EQUIPMENT));
	public static final Item SILVERLEAF_PICKAXE = new PickaxeItem(ArcanaToolMaterials.SILVERLEAF, 1, -2.8f, new Settings().group(Tab.EQUIPMENT));
	public static final Item SILVERLEAF_AXE = new AxeItem(ArcanaToolMaterials.SILVERLEAF, 5.5f, -3, new Settings().group(Tab.EQUIPMENT));
	public static final Item SILVERLEAF_HOE = new HoeItem(ArcanaToolMaterials.SILVERLEAF, -2, -1, new Settings().group(Tab.EQUIPMENT));
	public static final Item SILVERLEAF_HELMET = new ArmorItem(ArcanaArmourMaterials.SILVERLEAF, EquipmentSlot.HEAD, new Settings().group(Tab.EQUIPMENT));
	public static final Item SILVERLEAF_CHESTPLATE = new ArmorItem(ArcanaArmourMaterials.SILVERLEAF, EquipmentSlot.CHEST, new Settings().group(Tab.EQUIPMENT));
	public static final Item SILVERLEAF_LEGGINGS = new ArmorItem(ArcanaArmourMaterials.SILVERLEAF, EquipmentSlot.LEGS, new Settings().group(Tab.EQUIPMENT));
	public static final Item SILVERLEAF_BOOTS = new ArmorItem(ArcanaArmourMaterials.SILVERLEAF, EquipmentSlot.FEET, new Settings().group(Tab.EQUIPMENT));
	
	public static final Item WISPY_ESSENCE = new Item(GROUPED_RES);
	public static final Item TWISTED_ESSENCE = new Item(GROUPED_RES);
	public static final Item BEJEWELED_BEET = new Item(new Settings().group(Tab.RESOURCES).food(new FoodComponent.Builder().hunger(5).saturationModifier(1).build()));
	public static final Item SPIRAL_SUGAR = new Item(GROUPED_RES);
	public static final Item ABERRANT_FLORA = new Item(GROUPED_RES);
	public static final Item BLOODLET_RUBY = new Item(new ArcanaItemSettings().fragile(0xBC0826, StatusEffects.INSTANT_HEALTH).group(Tab.RESOURCES));
	public static final Item MOTILE = new MotileItem(new Settings().group(Tab.RESOURCES).rarity(Rarity.UNCOMMON));
	public static final Item MOTILE_PIECE = new MotileItem(new Settings().group(Tab.RESOURCES).rarity(Rarity.UNCOMMON));
	
	public static final Item SWORD_OF_THE_ZEPHYR = new SwordItem(ArcanaToolMaterials.PRIMAL, 3, -2.4f, new Settings().group(Tab.EQUIPMENT).rarity(Rarity.UNCOMMON));
	public static final Item SHOVEL_OF_THE_EARTHMOVER = new EarthmoverShovelItem(ArcanaToolMaterials.PRIMAL, 1.5f, -3, new Settings().group(Tab.EQUIPMENT).rarity(Rarity.UNCOMMON));
	public static final Item PICKAXE_OF_THE_CORE = new PickaxeItem(ArcanaToolMaterials.PRIMAL, 1, -2.8f, new Settings().group(Tab.EQUIPMENT).rarity(Rarity.UNCOMMON));
	public static final Item AXE_OF_THE_STREAM = new AxeItem(ArcanaToolMaterials.PRIMAL, 5.5f, -3, new Settings().group(Tab.EQUIPMENT).rarity(Rarity.UNCOMMON));
	public static final Item HOE_OF_THE_CYCLE = new HoeItem(ArcanaToolMaterials.PRIMAL, -2, -1, new Settings().group(Tab.EQUIPMENT).rarity(Rarity.UNCOMMON));
	
	public static final Item ARCANIUM_SCALPEL = new ScalpelItem(new Settings().group(Tab.EQUIPMENT).maxDamage(100), ScalpelItem.ScalpelType.ROSE);
	public static final Item SILVERLEAF_SCALPEL = new ScalpelItem(new Settings().group(Tab.EQUIPMENT).maxDamage(100), ScalpelItem.ScalpelType.SILVER);
	public static final Item VOID_METAL_SCALPEL = new ScalpelItem(new Settings().group(Tab.EQUIPMENT).maxDamage(100), ScalpelItem.ScalpelType.BLACK);
	
	public static final Item EMERALD_NECKLACE = new TrinketItem(new Settings().group(Tab.EQUIPMENT).maxCount(1));
	public static final Item GOLD_RING = new RingItem(new Settings().group(Tab.EQUIPMENT).maxCount(1), 2, 0);
	public static final Item ARCANIUM_RING = new RingItem(new Settings().group(Tab.EQUIPMENT).maxCount(1), 3, 0);
	public static final Item ADORNED_RING = new RingItem(new Settings().group(Tab.EQUIPMENT).maxCount(1), 1, 5);
	public static final Item PLANE_PROJECTION_RING = new RingItem(new Settings().group(Tab.EQUIPMENT).maxCount(1), 3, 0);
	public static final Item RING_OF_THE_SURGING_BARRIER = new RingItem(new Settings().group(Tab.EQUIPMENT).maxCount(1).rarity(Rarity.UNCOMMON), 1, 0);
	public static final Item RING_OF_TWIN_HEARTBEATS = new TwinHeartbeatRingItem(new Settings().group(Tab.EQUIPMENT).maxCount(1).rarity(Rarity.UNCOMMON));
	public static final Item AMULET_OF_RUNIC_SHIELDING = new ShieldingTrinketItem(new Settings().group(Tab.EQUIPMENT).maxCount(1), 2);
	public static final Item AMULET_OF_UNBURDENED_TRAVEL = new ShieldingTrinketItem(new Settings().group(Tab.EQUIPMENT).maxCount(1), 4);
	public static final Item AMULET_OF_DEAFENING_SHIELDING = new ShieldingTrinketItem(new Settings().group(Tab.EQUIPMENT).maxCount(1), 1);
	
	public static final Item CRIMSON_BLADE = new SwordItem(ArcanaToolMaterials.CRIMSON, 3, -2.4f, new Settings().group(Tab.EQUIPMENT));
	public static final Item CRIMSON_LONGBOW = new CrimsonLongbowItem(new Settings().group(Tab.EQUIPMENT).maxDamage(564));
	public static final Item CRIMSON_LEECH = new CrimsonLeechItem(new Settings().group(Tab.EQUIPMENT).rarity(Rarity.UNCOMMON).maxDamage(874));
	
	public static final Item BOOTS_OF_THE_TRAVELLER = new BootsOfTheTravellerItem(ArcanaArmourMaterials.BOOTS_OF_THE_TRAVELLER, new Settings().group(Tab.EQUIPMENT));
	public static final Item BOOTS_OF_THE_SAILOR = new BootsOfTheTravellerItem(ArcanaArmourMaterials.BOOTS_OF_THE_SAILOR, new Settings().group(Tab.EQUIPMENT));
	public static final Item BOOTS_OF_THE_REAPER = new BootsOfTheTravellerItem(ArcanaArmourMaterials.BOOTS_OF_THE_REAPER, new Settings().group(Tab.EQUIPMENT));
	
	public static final Item ALCHEMICAL_IRON = new Item(GROUPED_RES);
	public static final Item ALCHEMICAL_GOLD = new Item(GROUPED_RES);
	public static final Item ALCHEMICAL_COPPER = new Item(GROUPED_RES);
	public static final Item ALCHEMICAL_ARCANIUM = new Item(GROUPED_RES);
	public static final Item ALUMENTUM = new AlumentumItem(GROUPED_RES);
	
	public static final Item SHATTERED_HUSK = new Item(GROUPED);
	public static final Item SYNTHETIC_SCAFFOLDING = new Item(GROUPED);
	public static final Item FORMLESS_FOAM = new Item(GROUPED);
	
	public static final Item VOID_PUTTY = new Item(GROUPED);
	
	public static final Item WAND = new WandItem(GROUPED_WAND_SINGLE);
	
	public static final Item FOCUS_POUCH = new FocusPouchItem(GROUPED_WAND_SINGLE);
	
	// foci...
	public static final Item FIRE_FOCUS = new FireFocusItem(GROUPED_WAND_SINGLE);
	public static final Item SOLAR_FLARE_FOCUS = new SolarFlareFocusItem(GROUPED_WAND_SINGLE);
	public static final Item FETCH_FOCUS = new FetchFocusItem(GROUPED_WAND_SINGLE);
	public static final Item PORTABLE_HOLE_FOCUS = new PortableHoleFocusItem(GROUPED_WAND_SINGLE);
	public static final Item LIGHT_FOCUS = new LightFocusItem(GROUPED_WAND_SINGLE);
	public static final Item PRISMATIC_LIGHT_FOCUS = new PrismaticLightFocusItem(GROUPED_WAND_SINGLE);
	public static final Item LIGHTNING_FOCUS = new LightningFocusItem(GROUPED_WAND_SINGLE);
	public static final Item EQUIVALENT_EXCHANGE_FOCUS = new EquivalentExchangeFocusItem(GROUPED_WAND_SINGLE);
	public static final Item COAGULATION_FOCUS = new CoagulationFocusItem(GROUPED_WAND_SINGLE);
	public static final Item CRYSTAL_CAPACITOR_FOCUS = new CrystalCapacitorFocusItem(new Settings().group(Tab.WANDS).maxCount(1).maxDamage(6));
	public static final Item WARD_FOCUS = new WardFocusItem(new Settings().group(Tab.WANDS).maxCount(1).rarity(Rarity.UNCOMMON));
	public static final Item CONSUME_REBUKE_FOCUS = new ConsumeRebukeFocus(GROUPED_WAND_SINGLE);
	
	// caps...
	public static final CapItem IRON_WAND_CAP = new CapItem(GROUPED_WAND, capProperties().capacity(5).complexity(3));
	
	public static final CapItem GOLD_WAND_CAP = new CapItem(GROUPED_WAND, capProperties().capacity(14).complexity(15));
	public static final CapItem COPPER_WAND_CAP = new CapItem(GROUPED_WAND, capProperties().capacity(10).complexity(12).strBonus(5));
	public static final CapItem LEATHER_WAND_CAP = new CapItem(GROUPED_WAND, capProperties().capacity(10).complexity(12).discountAll(8));
	
	public static final CapItem THAUMIUM_WAND_CAP = new CapItem(GROUPED_WAND, capProperties().capacity(25).complexity(35));
	public static final CapItem BAMBOO_WAND_CAP = new CapItem(GROUPED_WAND, capProperties().capacity(20).complexity(30).discountFor(Aspects.AIR, 12));
	public static final CapItem QUARTZ_WAND_CAP = new CapItem(GROUPED_WAND, capProperties().capacity(20).complexity(30).discountFor(Aspects.FIRE, 12));
	public static final CapItem PRISMARINE_WAND_CAP = new CapItem(GROUPED_WAND, capProperties().capacity(20).complexity(30).discountFor(Aspects.WATER, 12));
	public static final CapItem AMBER_WAND_CAP = new CapItem(GROUPED_WAND, capProperties().capacity(20).complexity(30).discountFor(Aspects.EARTH, 12));
	public static final CapItem HONEYCOMB_WAND_CAP = new CapItem(GROUPED_WAND, capProperties().capacity(20).complexity(30).discountFor(Aspects.ORDER, 12));
	// TODO: chaos elemental cap
	
	public static final CapItem NETHERITE_WAND_CAP = new CapItem(GROUPED_WAND, capProperties().capacity(55).complexity(75));
	public static final CapItem MECHANICAL_WAND_CAP = new CapItem(GROUPED_WAND, capProperties().capacity(40).complexity(55)/*.mechanical()?*/);
	public static final CapItem VOID_WAND_CAP = new CapItem(GROUPED_WAND, capProperties().capacity(50).complexity(80).warping(1));
	public static final CapItem CRIMSON_WAND_CAP = new CapItem(GROUPED_WAND, capProperties().capacity(75).complexity(70).warping(2));
	
	public static final Cap MISSING_CAP = new Cap.Impl(arcId("missing"), 0, 0);
	
	// cores...
	public static final Core STICK_CORE = new Core.Impl(arcId("stick_wand_core"), 20, 3);
	
	public static final CoreItem GREATWOOD_WAND_CORE = new CoreItem(GROUPED_WAND, coreProperties().capacity(45).strength(10));
	public static final CoreItem NETHER_STEM_WAND_CORE = new CoreItem(GROUPED_WAND, coreProperties().capacity(37).strength(8).cmplxBonus(6));
	public static final CoreItem VARNISHED_WOOD_WAND_CORE = new CoreItem(GROUPED_WAND, coreProperties().capacity(37).strength(8).discountAll(12));
	
	public static final CoreItem SILVERWOOD_WAND_CORE = new CoreItem(GROUPED_WAND, coreProperties().capacity(78).strength(20));
	public static final CoreItem SUGAR_CANE_WAND_CORE = new CoreItem(GROUPED_WAND, coreProperties().capacity(63).strength(15).discountFor(Aspects.AIR, 16));
	public static final CoreItem BLAZE_WAND_CORE = new CoreItem(GROUPED_WAND, coreProperties().capacity(63).strength(15).discountFor(Aspects.FIRE, 16));
	public static final CoreItem ICE_WAND_CORE = new CoreItem(GROUPED_WAND, coreProperties().capacity(63).strength(15).discountFor(Aspects.WATER, 16));
	public static final CoreItem OBSIDIAN_WAND_CORE = new CoreItem(GROUPED_WAND, coreProperties().capacity(63).strength(15).discountFor(Aspects.EARTH, 16));
	public static final CoreItem ARCANE_STONE_WAND_CORE = new CoreItem(GROUPED_WAND, coreProperties().capacity(63).strength(15).discountFor(Aspects.ORDER, 16));
	public static final CoreItem BONE_WAND_CORE = new CoreItem(GROUPED_WAND, coreProperties().capacity(63).strength(15).discountFor(Aspects.ENTROPY, 16));
	
	public static final CoreItem ARCANIUM_WAND_CORE = new CoreItem(GROUPED_WAND, coreProperties().capacity(130).strength(40));
	public static final CoreItem MECHANICAL_WAND_CORE = new CoreItem(GROUPED_WAND, coreProperties().capacity(100).strength(30));
	public static final CoreItem TAINTED_WAND_CORE = new CoreItem(GROUPED_WAND, coreProperties().capacity(110).strength(58).warping(1));
	public static final CoreItem ELDRITCH_WAND_CORE = new CoreItem(GROUPED_WAND, coreProperties().capacity(165).strength(35).warping(2));
	
	public static final Core MISSING_CORE = new Core.Impl(arcId("missing"), 0, 0);
	
	// banner patterns...
	public static final BannerPattern ELDRITCH_BANNER_PATTERN_SHAPE = new BannerPattern("arcana_eldritch");
	public static final Item ELDRITCH_BANNER_PATTERN = new BannerPatternItem(ArcanaTags.ELDRITCH_BANNER_PATTERNS, new Item.Settings().maxCount(1).group(Tab.MAIN).rarity(Rarity.UNCOMMON));
	
	// other...?
	public static final Item EMPTY_PHIAL = new PhialItem(new Settings().group(Tab.PHIALS), null);
	public static final Item PRIMORDIAL_PEARL = new PrimordialPearlItem(new Settings().group(Tab.RESOURCES).maxCount(1).rarity(Rarity.EPIC));
	public static final Item BROKEN_AMULET = new TrinketItem(new Settings().group(Tab.RESOURCES).maxCount(1));
	public static final Item CHALLENGERS_AMULET = new TrinketItem(GROUPED_SINGLE);
	public static final Item VICTORS_MEDALLION = new TrinketItem(GROUPED_SINGLE);
	
	// creative-only
	public static final Item NODE_PLACER = new NodePlacerItem(GROUPED_CREATIVE_SINGLE);
	public static final Item NODE_REMOVER = new NodeRemoverItem(GROUPED_CREATIVE_SINGLE);
	public static final Item FLUX_SPONGE = new FluxSpongeItem(GROUPED_CREATIVE_SINGLE);
	public static final Item TAINT_INJECTOR = new TaintConverterItem(GROUPED_CREATIVE_SINGLE, true);
	public static final Item TAINT_ERASER = new TaintConverterItem(GROUPED_CREATIVE_SINGLE, false);
	
	// blocks...
	public static final Block ARCANE_CRAFTING_TABLE = new ArcaneCraftingTableBlock(of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).sounds(BlockSoundGroup.WOOD).strength(3).nonOpaque());
	public static final Block CRUCIBLE = new CrucibleBlock(of(Material.METAL).dropsSelf().requiresTool(PICKAXE_MINEABLE).sounds(BlockSoundGroup.METAL).strength(2).nonOpaque());
	public static final Block RESEARCH_TABLE = new ResearchTableBlock(of(Material.WOOD).dropsSelf().renderLayer(CUTOUT).usesTool(AXE_MINEABLE).nonOpaque().strength(3));
	public static final Block KNOWLEDGEABLE_DROPPER = new KnowledgeableDropperBlock(of(Material.STONE).dropsSelf().requiresTool(PICKAXE_MINEABLE).strength(3));
	
	public static final Block ARCANE_FURNACE = new ArcaneFurnaceBlock(of(Material.STONE).dropsSelf().requiresTool(PICKAXE_MINEABLE).strength(3).luminance(whenLit(14)));
	public static final Block ALEMBIC = new AlembicBlock(of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).strength(3).sounds(BlockSoundGroup.WOOD));
	public static final Block ESSENTIA_TUBE = new EssentiaTubeBlock(of(Material.METAL).dropsSelf().usesTool(PICKAXE_MINEABLE).strength(1).sounds(BlockSoundGroup.METAL));
	public static final Block ESSENTIA_VALVE = new EssentiaValveBlock(of(Material.METAL).dropsSelf().usesTool(PICKAXE_MINEABLE).strength(1).sounds(BlockSoundGroup.METAL));
	public static final Block ESSENTIA_WINDOW = new EssentiaTubeBlock(of(Material.GLASS).dropsSelf().usesTool(PICKAXE_MINEABLE).strength(.7f).sounds(BlockSoundGroup.GLASS));
	public static final Block ESSENTIA_PUMP = new EssentiaPumpBlock(of(Material.METAL).dropsSelf().usesTool(PICKAXE_MINEABLE).strength(1.1f).sounds(BlockSoundGroup.METAL));
	public static final Block ESSENTIA_REDIRECT = new EssentiaRedirectBlock(of(Material.METAL).dropsSelf().usesTool(PICKAXE_MINEABLE).strength(1).sounds(BlockSoundGroup.METAL));
	public static final Block ESSENTIA_ROUTER = new EssentiaRouterBlock(of(Material.METAL).dropsSelf().usesTool(PICKAXE_MINEABLE).strength(1.2f).sounds(BlockSoundGroup.METAL));
	public static final Block WARDED_JAR = new WardedJarBlock(of(Material.GLASS).renderLayer(TRANSLUCENT).strength(.9f).sounds(BlockSoundGroup.GLASS), false);
	public static final Block VOID_JAR = new WardedJarBlock(of(Material.GLASS).renderLayer(TRANSLUCENT).strength(.9f).sounds(BlockSoundGroup.GLASS), true);
	public static final Block DISTILLERY_PATHFINDER = new DistilleryPathfinderBlock(of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).strength(1.5f));
	
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
	public static final BigBlock THAUMIC_HALO = new ThaumicHaloBlock(of(Material.METAL).dropsSelf().requiresTool(PICKAXE_MINEABLE).renderLayer(CUTOUT).strength(3).nonOpaque());
	public static final Block CRYSTALLIZATION_PRESS = new CrystallizationPressBlock(of(Material.METAL).dropsSelf().requiresTool(PICKAXE_MINEABLE).sounds(BlockSoundGroup.ANCIENT_DEBRIS).strength(4).nonOpaque());
	public static final Block MYSTIC_MIST = new MysticMistBlock(of(Material.STONE).dropsSelf().requiresTool(PICKAXE_MINEABLE).sounds(BlockSoundGroup.METAL).strength(2.5f).nonOpaque());
	public static final Block WARDED_CAMPFIRE = new WardedCampfireBlock(of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).renderLayer(CUTOUT).strength(2).sounds(BlockSoundGroup.WOOD).luminance(whenLit(15)).nonOpaque());
	public static final Block CRIMSON_CAMPFIRE = new CrimsonCampfireBlock(of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).renderLayer(CUTOUT).strength(2).sounds(BlockSoundGroup.WOOD).luminance(whenLit(15)).nonOpaque());
	
	public static final Block ARCANIUM_BLOCK = new Block(of(Material.METAL, MapColor.PINK).group(Tab.RESOURCES).dropsSelf().requiresTool(PICKAXE_MINEABLE).strength(5, 6).sounds(BlockSoundGroup.METAL));
	public static final Block THAUMIUM_BLOCK = new Block(of(Material.METAL, MapColor.DARK_DULL_PINK).group(Tab.RESOURCES).dropsSelf().requiresTool(PICKAXE_MINEABLE).strength(5, 6).sounds(BlockSoundGroup.METAL));
	public static final Block VOID_METAL_BLOCK = new Block(of(Material.METAL, MapColor.PURPLE).group(Tab.RESOURCES).dropsSelf().requiresTool(PICKAXE_MINEABLE).strength(5, 6).sounds(BlockSoundGroup.METAL));
	public static final Block SILVERLEAF_AMALGAMATE_BLOCK = new Block(of(Material.METAL, MapColor.WHITE).group(Tab.RESOURCES).dropsSelf().requiresTool(PICKAXE_MINEABLE).strength(5, 6).sounds(BlockSoundGroup.METAL));
	
	public static final Block ARCANE_STONE = new Block(of(Material.STONE).dropsSelf().requiresTool(PICKAXE_MINEABLE).strength(3, 7));
	public static final Block ARCANE_STONE_BRICKS = new Block(of(Material.STONE).dropsSelf().requiresTool(PICKAXE_MINEABLE).strength(3.5f, 7));
	public static final Block ARCANE_STONE_TILES = new Block(of(Material.STONE).dropsSelf().requiresTool(PICKAXE_MINEABLE).strength(3.5f, 7));
	public static final Block ARCANE_STONE_GLEAMING_TILES = new Block(of(Material.STONE).dropsSelf().requiresTool(PICKAXE_MINEABLE).strength(3.5f, 7).luminance(8));
	public static final Block ARCANE_STONE_INSCRIBED_TILES = new Block(of(Material.STONE).dropsSelf().requiresTool(PICKAXE_MINEABLE).strength(3.5f, 7));
	
	public static final Block ARCANE_STONE_SLAB = new SlabBlock(of(Material.STONE).requiresTool(PICKAXE_MINEABLE).strength(3, 7).sounds(BlockSoundGroup.WOOD));
	public static final Block ARCANE_STONE_STAIRS = new StairsBlock(ARCANE_STONE.getDefaultState(), of(Material.STONE).dropsSelf().requiresTool(PICKAXE_MINEABLE).strength(2));
	public static final Block ARCANE_STONE_PRESSURE_PLATE = new PressurePlateBlock(PressurePlateBlock.ActivationRule.MOBS, of(Material.STONE).dropsSelf().requiresTool(PICKAXE_MINEABLE).strength(.5f));
	public static final Block ARCANE_STONE_BUTTON = new StoneButtonBlock(of(Material.STONE).dropsSelf().noCollision().strength(.5f));
	public static final Block ARCANE_STONE_WALL = new WallBlock(of(Material.STONE).dropsSelf().requiresTool(PICKAXE_MINEABLE).strength(2));
	
	public static final Block ARCANE_STONE_BRICKS_SLAB = new SlabBlock(of(Material.STONE).requiresTool(PICKAXE_MINEABLE).strength(3, 7).sounds(BlockSoundGroup.WOOD));
	public static final Block ARCANE_STONE_BRICKS_STAIRS = new StairsBlock(ARCANE_STONE_BRICKS.getDefaultState(), of(Material.STONE).dropsSelf().requiresTool(PICKAXE_MINEABLE).strength(2));
	public static final Block ARCANE_STONE_BRICKS_PRESSURE_PLATE = new PressurePlateBlock(PressurePlateBlock.ActivationRule.MOBS, of(Material.STONE).dropsSelf().requiresTool(PICKAXE_MINEABLE).strength(.5f));
	public static final Block ARCANE_STONE_BRICKS_BUTTON = new StoneButtonBlock(of(Material.STONE).dropsSelf().noCollision().strength(.5f));
	public static final Block ARCANE_STONE_BRICKS_WALL = new WallBlock(of(Material.STONE).dropsSelf().requiresTool(PICKAXE_MINEABLE).strength(2));
	
	public static final Block ARCANE_STONE_TILES_SLAB = new SlabBlock(of(Material.STONE).requiresTool(PICKAXE_MINEABLE).strength(3, 7).sounds(BlockSoundGroup.WOOD));
	public static final Block ARCANE_STONE_TILES_STAIRS = new StairsBlock(ARCANE_STONE_TILES.getDefaultState(), of(Material.STONE).dropsSelf().requiresTool(PICKAXE_MINEABLE).strength(2));
	public static final Block ARCANE_STONE_TILES_PRESSURE_PLATE = new PressurePlateBlock(PressurePlateBlock.ActivationRule.MOBS, of(Material.STONE).dropsSelf().requiresTool(PICKAXE_MINEABLE).strength(.5f));
	public static final Block ARCANE_STONE_TILES_BUTTON = new StoneButtonBlock(of(Material.STONE).dropsSelf().noCollision().strength(.5f));
	public static final Block ARCANE_STONE_TILES_WALL = new WallBlock(of(Material.STONE).dropsSelf().requiresTool(PICKAXE_MINEABLE).strength(2));
	
	public static final Block SILVERWOOD_SAPLING = new SaplingBlock(new SilverwoodSaplingGenerator(), of(Material.PLANT).dropsSelf().renderLayer(CUTOUT).noCollision().ticksRandomly().breakInstantly().sounds(BlockSoundGroup.GRASS));
	public static final Block SILVERWOOD_LOG = new PillarBlock(of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).strength(2).sounds(BlockSoundGroup.WOOD));
	public static final Block SILVERWOOD_LEAVES = new LeavesBlock(of(Material.LEAVES).renderLayer(CUTOUT).strength(.2f).ticksRandomly().sounds(BlockSoundGroup.GRASS).nonOpaque().allowsSpawning(Blocks::canSpawnOnLeaves).suffocates(Blocks::never).blockVision(Blocks::never));
	public static final Block SILVERWOOD_PLANKS = new Block(of(Material.WOOD).dropsSelf().strength(2, 3).sounds(BlockSoundGroup.WOOD));
	
	public static final Block SILVERWOOD_WOOD = new PillarBlock(of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).strength(2).sounds(BlockSoundGroup.WOOD));
	public static final Block STRIPPED_SILVERWOOD_LOG = new PillarBlock(of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).strength(2).sounds(BlockSoundGroup.WOOD));
	public static final Block STRIPPED_SILVERWOOD_WOOD = new PillarBlock(of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).strength(2).sounds(BlockSoundGroup.WOOD));
	
	public static final Block SILVERWOOD_SLAB = new SlabBlock(of(Material.WOOD).usesTool(AXE_MINEABLE).strength(2).sounds(BlockSoundGroup.WOOD));
	public static final Block SILVERWOOD_STAIRS = new StairsBlock(SILVERWOOD_PLANKS.getDefaultState(), of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).strength(2).sounds(BlockSoundGroup.WOOD));
	public static final Block SILVERWOOD_FENCE = new FenceBlock(of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).strength(2).sounds(BlockSoundGroup.WOOD));
	public static final Block SILVERWOOD_FENCE_GATE = new FenceGateBlock(of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).strength(2).sounds(BlockSoundGroup.WOOD));
	public static final Block SILVERWOOD_PRESSURE_PLATE = new PressurePlateBlock(PressurePlateBlock.ActivationRule.EVERYTHING, of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).strength(.5f).sounds(BlockSoundGroup.WOOD));
	public static final Block SILVERWOOD_BUTTON = new WoodenButtonBlock(of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).strength(.5f).noCollision().sounds(BlockSoundGroup.WOOD));
	
	public static final SignType SILVERWOOD_SIGN_TY = SignTypeRegistry.registerSignType(arcId("silverwood"));
	public static final Block SILVERWOOD_DOOR = new DoorBlock(of(Material.WOOD).usesTool(AXE_MINEABLE).renderLayer(CUTOUT).strength(3).sounds(BlockSoundGroup.WOOD).nonOpaque());
	public static final Block SILVERWOOD_TRAPDOOR = new TrapdoorBlock(of(Material.WOOD).usesTool(AXE_MINEABLE).renderLayer(CUTOUT).dropsSelf().strength(3).sounds(BlockSoundGroup.WOOD).nonOpaque().allowsSpawning(Blocks::never));
	public static final Block SILVERWOOD_SIGN = new SignBlock(of(Material.WOOD).usesTool(AXE_MINEABLE).renderLayer(CUTOUT).strength(3).sounds(BlockSoundGroup.WOOD).nonOpaque(), SILVERWOOD_SIGN_TY);
	public static final Block SILVERWOOD_WALL_SIGN = new WallSignBlock(of(Material.WOOD).usesTool(AXE_MINEABLE).renderLayer(CUTOUT).dropsLike(SILVERWOOD_SIGN).strength(3).sounds(BlockSoundGroup.WOOD).nonOpaque(), SILVERWOOD_SIGN_TY);
	
	public static final Block GLEAMING_SILVERWOOD_PLANKS = new Block(of(Material.WOOD).dropsSelf().strength(2, 3).sounds(BlockSoundGroup.WOOD));
	public static final Block SOLAR_GLEAMING_SILVERWOOD_PLANKS = new Block(of(Material.WOOD).dropsSelf().strength(2, 3).sounds(BlockSoundGroup.WOOD));
	
	public static final Block GREATWOOD_SAPLING = new SaplingBlock(new GreatwoodSaplingGenerator(), of(Material.PLANT).dropsSelf().renderLayer(CUTOUT).noCollision().ticksRandomly().breakInstantly().sounds(BlockSoundGroup.GRASS));
	public static final Block GREATWOOD_LOG = new PillarBlock(of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).strength(2).sounds(BlockSoundGroup.WOOD));
	public static final Block GREATWOOD_LEAVES = new LeavesBlock(of(Material.LEAVES).renderLayer(CUTOUT).strength(.2f).ticksRandomly().sounds(BlockSoundGroup.GRASS).nonOpaque().allowsSpawning(Blocks::canSpawnOnLeaves).suffocates(Blocks::never).blockVision(Blocks::never));
	public static final Block GREATWOOD_PLANKS = new Block(of(Material.WOOD).dropsSelf().strength(2, 3).sounds(BlockSoundGroup.WOOD));
	
	public static final Block GREATWOOD_WOOD = new PillarBlock(of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).strength(2).sounds(BlockSoundGroup.WOOD));
	public static final Block STRIPPED_GREATWOOD_LOG = new PillarBlock(of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).strength(2).sounds(BlockSoundGroup.WOOD));
	public static final Block STRIPPED_GREATWOOD_WOOD = new PillarBlock(of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).strength(2).sounds(BlockSoundGroup.WOOD));
	
	public static final Block GREATWOOD_SLAB = new SlabBlock(of(Material.WOOD).usesTool(AXE_MINEABLE).strength(2).sounds(BlockSoundGroup.WOOD));
	public static final Block GREATWOOD_STAIRS = new StairsBlock(GREATWOOD_PLANKS.getDefaultState(), of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).strength(2).sounds(BlockSoundGroup.WOOD));
	public static final Block GREATWOOD_FENCE = new FenceBlock(of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).strength(2).sounds(BlockSoundGroup.WOOD));
	public static final Block GREATWOOD_FENCE_GATE = new FenceGateBlock(of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).strength(2).sounds(BlockSoundGroup.WOOD));
	public static final Block GREATWOOD_PRESSURE_PLATE = new PressurePlateBlock(PressurePlateBlock.ActivationRule.EVERYTHING, of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).strength(.5f).sounds(BlockSoundGroup.WOOD));
	public static final Block GREATWOOD_BUTTON = new WoodenButtonBlock(of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).strength(.5f).noCollision().sounds(BlockSoundGroup.WOOD));
	
	public static final SignType GREATWOOD_SIGN_TY = SignTypeRegistry.registerSignType(arcId("greatwood"));
	public static final Block GREATWOOD_DOOR = new DoorBlock(of(Material.WOOD).usesTool(AXE_MINEABLE).renderLayer(CUTOUT).strength(3).sounds(BlockSoundGroup.WOOD).nonOpaque());
	public static final Block GREATWOOD_TRAPDOOR = new TrapdoorBlock(of(Material.WOOD).usesTool(AXE_MINEABLE).renderLayer(CUTOUT).dropsSelf().strength(3).sounds(BlockSoundGroup.WOOD).nonOpaque().allowsSpawning(Blocks::never));
	public static final Block GREATWOOD_SIGN = new SignBlock(of(Material.WOOD).usesTool(AXE_MINEABLE).renderLayer(CUTOUT).strength(3).sounds(BlockSoundGroup.WOOD).nonOpaque(), GREATWOOD_SIGN_TY);
	public static final Block GREATWOOD_WALL_SIGN = new WallSignBlock(of(Material.WOOD).usesTool(AXE_MINEABLE).renderLayer(CUTOUT).dropsLike(GREATWOOD_SIGN).strength(3).sounds(BlockSoundGroup.WOOD).nonOpaque(), GREATWOOD_SIGN_TY);
	
	public static final Block GLEAMING_GREATWOOD_PLANKS = new Block(of(Material.WOOD).dropsSelf().strength(2, 3).sounds(BlockSoundGroup.WOOD));
	public static final Block SOLAR_GLEAMING_GREATWOOD_PLANKS = new Block(of(Material.WOOD).dropsSelf().strength(2, 3).sounds(BlockSoundGroup.WOOD));
	
	public static final Block BALANCED_CRYSTAL = new Block(of(Material.AMETHYST, MapColor.WHITE).dropsSelf().usesTool(PICKAXE_MINEABLE).sounds(BlockSoundGroup.AMETHYST_CLUSTER).strength(0.9f).luminance(3));
	public static final Block BALANCED_CRYSTAL_PILLAR = new CrystalPillarBlock(of(Material.AMETHYST, MapColor.WHITE).dropsSelf().usesTool(PICKAXE_MINEABLE).sounds(BlockSoundGroup.AMETHYST_CLUSTER).strength(0.9f).luminance(3));
	
	public static final Block TAINTWOOD_LOG = new PillarBlock(of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).group(Tab.TAINTED).strength(1.2f).sounds(BlockSoundGroup.FUNGUS));
	public static final Block TAINTWOOD_PLANKS = new Block(of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).group(Tab.TAINTED).strength(1.2f).sounds(BlockSoundGroup.FUNGUS));
	public static final Block TAINTWOOD_WOOD = new PillarBlock(of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).group(Tab.TAINTED).strength(1.2f).sounds(BlockSoundGroup.FUNGUS));
	
	public static final Block TAINTWOOD_DOOR = new DoorBlock(of(Material.WOOD).usesTool(AXE_MINEABLE).group(Tab.TAINTED).renderLayer(CUTOUT).strength(1.6f).sounds(BlockSoundGroup.FUNGUS).nonOpaque());
	public static final Block TAINTWOOD_TRAPDOOR = new TrapdoorBlock(of(Material.WOOD).usesTool(AXE_MINEABLE).group(Tab.TAINTED).renderLayer(CUTOUT).dropsSelf().strength(1.6f).sounds(BlockSoundGroup.FUNGUS).nonOpaque().allowsSpawning(Blocks::never));
	public static final Block TAINTWOOD_SLAB = new SlabBlock(of(Material.WOOD).usesTool(AXE_MINEABLE).group(Tab.TAINTED).strength(1.2f).sounds(BlockSoundGroup.FUNGUS));
	public static final Block TAINTWOOD_STAIRS = new StairsBlock(TAINTWOOD_PLANKS.getDefaultState(), of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).group(Tab.TAINTED).strength(2).sounds(BlockSoundGroup.FUNGUS));
	public static final Block TAINTWOOD_FENCE = new FenceBlock(of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).group(Tab.TAINTED).strength(1.2f).sounds(BlockSoundGroup.FUNGUS));
	public static final Block TAINTWOOD_FENCE_GATE = new FenceGateBlock(of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).group(Tab.TAINTED).strength(1.2f).sounds(BlockSoundGroup.FUNGUS));
	public static final Block TAINTWOOD_PRESSURE_PLATE = new PressurePlateBlock(PressurePlateBlock.ActivationRule.EVERYTHING, of(Material.WOOD).dropsSelf().group(Tab.TAINTED).usesTool(AXE_MINEABLE).strength(.5f).sounds(BlockSoundGroup.FUNGUS));
	public static final Block TAINTWOOD_BUTTON = new WoodenButtonBlock(of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).group(Tab.TAINTED).strength(.5f).noCollision().sounds(BlockSoundGroup.FUNGUS));
	
	public static final Block HOLLOWED_LOG = new PillarBlock(of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).group(Tab.TAINTED).strength(1.2f).sounds(BlockSoundGroup.CORAL));
	public static final Block HOLLOWED_PLANKS = new Block(of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).group(Tab.TAINTED).strength(1.2f, 3).sounds(BlockSoundGroup.CORAL));
	public static final Block HOLLOWED_WOOD = new PillarBlock(of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).group(Tab.TAINTED).strength(1.2f).sounds(BlockSoundGroup.CORAL));
	
	public static final Block HOLLOWED_DOOR = new DoorBlock(of(Material.WOOD).usesTool(AXE_MINEABLE).group(Tab.TAINTED).renderLayer(CUTOUT).strength(1.6f).sounds(BlockSoundGroup.CORAL).nonOpaque());
	public static final Block HOLLOWED_TRAPDOOR = new TrapdoorBlock(of(Material.WOOD).usesTool(AXE_MINEABLE).group(Tab.TAINTED).renderLayer(CUTOUT).dropsSelf().strength(1.6f).sounds(BlockSoundGroup.CORAL).nonOpaque().allowsSpawning(Blocks::never));
	public static final Block HOLLOWED_SLAB = new SlabBlock(of(Material.WOOD).usesTool(AXE_MINEABLE).group(Tab.TAINTED).strength(1.2f).sounds(BlockSoundGroup.CORAL));
	public static final Block HOLLOWED_STAIRS = new StairsBlock(HOLLOWED_PLANKS.getDefaultState(), of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).group(Tab.TAINTED).strength(2).sounds(BlockSoundGroup.CORAL));
	public static final Block HOLLOWED_FENCE = new FenceBlock(of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).group(Tab.TAINTED).strength(1.2f).sounds(BlockSoundGroup.CORAL));
	public static final Block HOLLOWED_FENCE_GATE = new FenceGateBlock(of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).group(Tab.TAINTED).strength(1.2f).sounds(BlockSoundGroup.CORAL));
	public static final Block HOLLOWED_PRESSURE_PLATE = new PressurePlateBlock(PressurePlateBlock.ActivationRule.EVERYTHING, of(Material.WOOD).dropsSelf().group(Tab.TAINTED).usesTool(AXE_MINEABLE).strength(.5f).sounds(BlockSoundGroup.CORAL));
	public static final Block HOLLOWED_BUTTON = new WoodenButtonBlock(of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).group(Tab.TAINTED).strength(.5f).noCollision().sounds(BlockSoundGroup.CORAL));
	
	public static final Block VISHROOM = new SizedPlantBlock(of(Material.PLANT).dropsSelf().renderLayer(CUTOUT).sounds(BlockSoundGroup.GRASS).noCollision().breakInstantly().offsetType(AbstractBlock.OffsetType.XZ), 14, 14);
	public static final Block CORDISPORA = new SizedPlantBlock(of(Material.PLANT).dropsSelf().renderLayer(CUTOUT).sounds(BlockSoundGroup.GRASS).noCollision().breakInstantly().offsetType(AbstractBlock.OffsetType.XZ), 6, 6);
	public static final Block SNOWDROP = new SizedPlantBlock(of(Material.PLANT).dropsSelf().renderLayer(CUTOUT).sounds(BlockSoundGroup.GRASS).noCollision().breakInstantly().offsetType(AbstractBlock.OffsetType.XZ), 13, 14);
	public static final Block FIREWHEEL = new SizedPlantBlock(of(Material.PLANT).dropsSelf().renderLayer(CUTOUT).sounds(BlockSoundGroup.GRASS).noCollision().breakInstantly().offsetType(AbstractBlock.OffsetType.XZ), 10, 15);
	public static final Block LILIUM = new SizedPlantBlock(of(Material.PLANT).dropsSelf().renderLayer(CUTOUT).sounds(BlockSoundGroup.GRASS).noCollision().breakInstantly().offsetType(AbstractBlock.OffsetType.XZ), 6, 15);
	
	public static final Block BEJEWELED_BEETS_BLOCK = new BejeweledBeetsBlock(of(Material.PLANT).renderLayer(CUTOUT).nonOpaque().noCollision().ticksRandomly().breakInstantly().sounds(BlockSoundGroup.CROP));
	public static final Item BEJEWELED_BEET_SEEDS = new AliasedBlockItem(BEJEWELED_BEETS_BLOCK, GROUPED_RES);
	
	public static final Block VOID_GROWTH = new Block(of(Material.PLANT).renderLayer(CUTOUT).nonOpaque().noCollision().breakInstantly().sounds(BlockSoundGroup.FROGSPAWN));
	
	public static final WoodenStatueBlock SPEAK_NO_EVIL_STATUE = new WoodenStatueBlock(of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).strength(2).sounds(BlockSoundGroup.WOOD).nonOpaque(), WoodenStatueBlock.Type.speak);
	public static final WoodenStatueBlock SEE_NO_EVIL_STATUE = new WoodenStatueBlock(of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).strength(2).sounds(BlockSoundGroup.WOOD).nonOpaque(), WoodenStatueBlock.Type.see);
	public static final WoodenStatueBlock HEAR_NO_EVIL_STATUE = new WoodenStatueBlock(of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).strength(2).sounds(BlockSoundGroup.WOOD).nonOpaque(), WoodenStatueBlock.Type.hear);
	
	public static final Block CRIMSON_LANTERN = new CrimsonLanternBlock(of(Material.METAL).renderLayer(CUTOUT).dropsSelf().requiresTool(PICKAXE_MINEABLE).strength(3.5f).sounds(BlockSoundGroup.LANTERN).luminance(__ -> 12).nonOpaque());
	public static final Block CHAIN_WALL = new PaneBlock(of(Material.METAL, MapColor.CLEAR).renderLayer(CUTOUT).dropsSelf().requiresTool(PICKAXE_MINEABLE).strength(2.5f).sounds(BlockSoundGroup.METAL).nonOpaque());
	public static final Block METAL_LADDER = new LadderBlock(of(Material.WOOD).renderLayer(CUTOUT).dropsSelf().requiresTool(PICKAXE_MINEABLE).strength(2.5f).sounds(BlockSoundGroup.LADDER).nonOpaque());
	
	public static final Block GLEAMING_LAMPLIGHT = new Block(of(Material.GLASS).dropsSelf().usesTool(PICKAXE_MINEABLE).strength(2, 7).luminance(15));
	public static final Block CHISELED_GLEAMING_LAMPLIGHT = new Block(of(Material.GLASS).dropsSelf().usesTool(PICKAXE_MINEABLE).strength(2, 7).luminance(15));
	
	public static final Block LIGHT_BLOCK = new LightFocusBlock(of(Material.DECORATION).dropsNothing().breakInstantly().ticksRandomly().luminance(state -> 7 + state.get(LightFocusBlock.life)));
	public static final Block TAINT_GOO = new FluidBlock(STILL_TAINT_GOO, FabricBlockSettings.copy(Blocks.WATER));
	public static final Block PUTREFACTION = new FluidBlock(STILL_PUTREFACTION, FabricBlockSettings.copy(Blocks.WATER));
	
	public static final Block POTTED_SILVERWOOD_SAPLING = new FlowerPotBlock(SILVERWOOD_SAPLING, of(Material.DECORATION).renderLayer(CUTOUT).breakInstantly().nonOpaque());
	public static final Block POTTED_GREATWOOD_SAPLING = new FlowerPotBlock(GREATWOOD_SAPLING, of(Material.DECORATION).renderLayer(CUTOUT).breakInstantly().nonOpaque());
	public static final Block POTTED_VISHROOM = new FlowerPotBlock(VISHROOM, of(Material.DECORATION).renderLayer(CUTOUT).breakInstantly().nonOpaque());
	public static final Block POTTED_CORDISPORA = new FlowerPotBlock(CORDISPORA, of(Material.DECORATION).renderLayer(CUTOUT).breakInstantly().nonOpaque());
	public static final Block POTTED_SNOWDROP = new FlowerPotBlock(SNOWDROP, of(Material.DECORATION).renderLayer(CUTOUT).breakInstantly().nonOpaque());
	public static final Block POTTED_FIREWHEEL = new FlowerPotBlock(FIREWHEEL, of(Material.DECORATION).renderLayer(CUTOUT).breakInstantly().nonOpaque());
	public static final Block POTTED_LILIUM = new FlowerPotBlock(LILIUM, of(Material.DECORATION).renderLayer(CUTOUT).breakInstantly().nonOpaque());
	
	// natural tainted blocks
	public static final Block TAINTED_ROCK = new TaintedBlock(of(Material.STONE, MapColor.PURPLE).group(Tab.TAINTED).dropsSelf().requiresTool(PICKAXE_MINEABLE).strength(1.6f, 6));
	public static final Block TAINTED_ANDESITE = new TaintedBlock(of(Material.STONE, MapColor.PURPLE).group(Tab.TAINTED).dropsSelf().requiresTool(PICKAXE_MINEABLE).strength(1.6f, 6));
	public static final Block TAINTED_DIORITE = new TaintedBlock(of(Material.STONE, MapColor.PURPLE).group(Tab.TAINTED).dropsSelf().requiresTool(PICKAXE_MINEABLE).strength(1.6f, 6));
	public static final Block TAINTED_GRANITE = new TaintedBlock(of(Material.STONE, MapColor.PURPLE).group(Tab.TAINTED).dropsSelf().requiresTool(PICKAXE_MINEABLE).strength(1.6f, 6));
	
	public static final Block TAINTED_SOIL = new TaintedBlock(of(Material.SOIL, MapColor.PURPLE).group(Tab.TAINTED).dropsSelf().usesTool(SHOVEL_MINEABLE).strength(0.5f).sounds(BlockSoundGroup.GRAVEL));
	public static final Block TAINTED_GRASS_BLOCK = new TaintedSnowyBlock(of(Material.SOLID_ORGANIC, MapColor.PURPLE).group(Tab.TAINTED).usesTool(SHOVEL_MINEABLE).strength(0.6f).sounds(BlockSoundGroup.GRASS));
	public static final Block TAINTED_SAND = new TaintedFallingBlock(of(Material.AGGREGATE, MapColor.PURPLE).group(Tab.TAINTED).usesTool(SHOVEL_MINEABLE).strength(0.5f).sounds(BlockSoundGroup.SAND));
	public static final Block TAINTED_SANDSTONE = new TaintedBlock(of(Material.STONE, MapColor.PURPLE).group(Tab.TAINTED).dropsSelf().requiresTool(PICKAXE_MINEABLE).strength(0.8f));
	public static final Block TAINTED_GRAVEL = new TaintedFallingBlock(of(Material.AGGREGATE, MapColor.PURPLE).group(Tab.TAINTED).usesTool(SHOVEL_MINEABLE).strength(0.7f).sounds(BlockSoundGroup.GRAVEL));
	public static final Block TAINTED_SNOW_BLOCK = new TaintedFallingBlock(of(Material.SNOW_BLOCK, MapColor.PURPLE).group(Tab.TAINTED).requiresTool(SHOVEL_MINEABLE).strength(0.2f).sounds(BlockSoundGroup.SNOW));
	
	public static final Block TAINTED_HOLLOWED_ORE = new TaintedBlock(of(Material.STONE, MapColor.PURPLE).group(Tab.TAINTED).requiresTool(PICKAXE_MINEABLE).strength(1.8f, 6));
	
	// unique tainted blocks
	public static final Block TAINT_CRUST = new TaintedBlock(of(Material.SOLID_ORGANIC, MapColor.PURPLE).group(Tab.TAINTED).requiresTool(HOE_MINEABLE).strength(0.7f).sounds(BlockSoundGroup.SLIME));
	
	// dead/damaged/untainted blocks
	public static final Block HOLLOWED_ORE = new Block(of(Material.STONE).group(Tab.TAINTED).dropsSelf().requiresTool(PICKAXE_MINEABLE).strength(1.8f));
	
	// points of interest...
	// created in register
	public static PointOfInterestType WARDED_CAMPFIRE_POI;
	
	// screen handlers...
	public static final ScreenHandlerType<ArcaneCraftingScreen.Handler> ARCANE_CRAFTING_SCREEN_HANDLER = new ScreenHandlerType<>(ArcaneCraftingScreen.Handler::new);
	public static final ScreenHandlerType<ResearchTableScreen.Handler> RESEARCH_TABLE_SCREEN_HANDLER = new ScreenHandlerType<>(ResearchTableScreen.Handler::new);
	public static final ScreenHandlerType<KnowledgeableDropperScreen.Handler> KNOWLEDGEABLE_DROPPER_SCREEN_HANDLER = new ScreenHandlerType<>(KnowledgeableDropperScreen.Handler::new);
	public static final ScreenHandlerType<ArcaneFurnaceScreen.Handler> ARCANE_FURNACE_SCREEN_HANDLER = new ScreenHandlerType<>(ArcaneFurnaceScreen.Handler::new);
	public static final ScreenHandlerType<DistilleryPathfinderScreen.Handler> DISTILLERY_PATHFINDER_SCREEN_HANDLER = new ScreenHandlerType<>(DistilleryPathfinderScreen.Handler::new);
	public static final ScreenHandlerType<CrystallizationPressScreen.Handler> CRYSTALLIZATION_PRESS_SCREEN_HANDLER = new ScreenHandlerType<>(CrystallizationPressScreen.Handler::new);
	public static final ScreenHandlerType<FocusPouchScreen.Handler> FOCUS_POUCH_SCREEN_HANDLER = new ScreenHandlerType<>(FocusPouchScreen.Handler::new);
	
	// block entities...
	public static BlockEntityType<CrucibleBlockEntity> CRUCIBLE_BE = FabricBlockEntityTypeBuilder.create(CrucibleBlockEntity::new, CRUCIBLE).build();
	public static BlockEntityType<ResearchTableBlockEntity> RESEARCH_TABLE_BE = FabricBlockEntityTypeBuilder.create(ResearchTableBlockEntity::new, RESEARCH_TABLE).build();
	public static BlockEntityType<KnowledgeableDropperBlockEntity> KNOWLEDGEABLE_DROPPER_BE = FabricBlockEntityTypeBuilder.create(KnowledgeableDropperBlockEntity::new, KNOWLEDGEABLE_DROPPER).build();
	public static BlockEntityType<PedestalBlockEntity> PEDESTAL_BE = FabricBlockEntityTypeBuilder.create(PedestalBlockEntity::new, PEDESTAL).build();
	public static BlockEntityType<ArcaneLevitatorBlockEntity> ARCANE_LEVITATOR_BE = FabricBlockEntityTypeBuilder.create(ArcaneLevitatorBlockEntity::new, ARCANE_LEVITATOR).build();
	public static BlockEntityType<InfusionPillarBlockEntity> INFUSION_PILLAR_BE = FabricBlockEntityTypeBuilder.create(InfusionPillarBlockEntity::new, INFUSION_PILLAR).build();
	public static BlockEntityType<InfusionMatrixBlockEntity> INFUSION_MATRIX_BE = FabricBlockEntityTypeBuilder.create(InfusionMatrixBlockEntity::new, INFUSION_MATRIX).build();
	public static BlockEntityType<WardedJarBlockEntity> WARDED_JAR_BE = FabricBlockEntityTypeBuilder.create((pos, state) -> new WardedJarBlockEntity(pos, state, false), WARDED_JAR).build();
	public static BlockEntityType<WardedJarBlockEntity> VOID_JAR_BE = FabricBlockEntityTypeBuilder.create((pos, state) -> new WardedJarBlockEntity(pos, state, true), VOID_JAR).build();
	public static BlockEntityType<CrystallizationPressBlockEntity> CRYSTALLIZATION_PRESS_BE = FabricBlockEntityTypeBuilder.create(CrystallizationPressBlockEntity::new, CRYSTALLIZATION_PRESS).build();
	public static BlockEntityType<MysticMistBlockEntity> MYSTIC_MIST_BE = FabricBlockEntityTypeBuilder.create(MysticMistBlockEntity::new, MYSTIC_MIST).build();
	public static BlockEntityType<WardedCampfireBlockEntity> WARDED_CAMPFIRE_BE = FabricBlockEntityTypeBuilder.create(WardedCampfireBlockEntity::new, WARDED_CAMPFIRE).build();
	public static BlockEntityType<CrimsonCampfireBlockEntity> CRIMSON_CAMPFIRE_BE = FabricBlockEntityTypeBuilder.create(CrimsonCampfireBlockEntity::new, CRIMSON_CAMPFIRE).build();
	public static BlockEntityType<EssentiaTubeBlockEntity> ESSENTIA_TUBE_BE = FabricBlockEntityTypeBuilder.create(EssentiaTubeBlockEntity::new, ESSENTIA_TUBE, ESSENTIA_WINDOW).build();
	public static BlockEntityType<EssentiaPumpBlockEntity> ESSENTIA_PUMP_BE = FabricBlockEntityTypeBuilder.create(EssentiaPumpBlockEntity::new, ESSENTIA_PUMP).build();
	public static BlockEntityType<EssentiaValveBlockEntity> ESSENTIA_VALVE_BE = FabricBlockEntityTypeBuilder.create(EssentiaValveBlockEntity::new, ESSENTIA_VALVE).build();
	public static BlockEntityType<EssentiaRedirectBlockEntity> ESSENTIA_REDIRECT_BE = FabricBlockEntityTypeBuilder.create(EssentiaRedirectBlockEntity::new, ESSENTIA_REDIRECT).build();
	public static BlockEntityType<EssentiaRouterBlockEntity> ESSENTIA_ROUTER_BE = FabricBlockEntityTypeBuilder.create(EssentiaRouterBlockEntity::new, ESSENTIA_ROUTER).build();
	public static BlockEntityType<ArcaneFurnaceBlockEntity> ARCANE_FURNACE_BE = FabricBlockEntityTypeBuilder.create(ArcaneFurnaceBlockEntity::new, ARCANE_FURNACE).build();
	public static BlockEntityType<AlembicBlockEntity> ALEMBIC_BE = FabricBlockEntityTypeBuilder.create(AlembicBlockEntity::new, ALEMBIC).build();
	public static BlockEntityType<CrimsonLanternBlockEntity> CRIMSON_LANTERN_BE = FabricBlockEntityTypeBuilder.create(CrimsonLanternBlockEntity::new, CRIMSON_LANTERN).build();
	public static BlockEntityType<DistilleryPathfinderBlockEntity> DISTILLERY_PATHFINDER_BE = FabricBlockEntityTypeBuilder.create(DistilleryPathfinderBlockEntity::new, DISTILLERY_PATHFINDER).build();
	public static BlockEntityType<ThaumicHaloBlockEntity> THAUMIC_HALO_BE = FabricBlockEntityTypeBuilder.create(ThaumicHaloBlockEntity::new, THAUMIC_HALO).build();
	
	// enchantments...
	public static final Map<Item, Item> TRANSMUTATIVE_SWAPS = ImmutableMap.<Item, Item>builder()
			.put(Items.GUNPOWDER, Items.BLAZE_POWDER)
			.put(Items.STRING, Items.COBWEB)
			.put(Items.QUARTZ, Items.FLINT)
			.put(Items.STICK, Items.DEAD_BUSH)
			.put(Items.REDSTONE, Items.GLOWSTONE_DUST)
			.put(Items.GLOWSTONE_DUST, Items.REDSTONE)
			.put(Items.SNOWBALL, Items.ICE)
			.put(Items.ROTTEN_FLESH, Items.LEATHER)
			.put(Items.SPIDER_EYE, Items.FERMENTED_SPIDER_EYE)
			.put(Items.CHICKEN, Items.RABBIT)
			.put(Items.RABBIT, Items.CHICKEN)
			.put(Items.MUTTON, Items.BEEF)
			.put(Items.BEEF, Items.MUTTON)
			.put(Items.GOLD_NUGGET, Items.IRON_NUGGET)
			.put(Items.MAGMA_CREAM, Items.NETHER_WART)
			.put(Items.BONE_MEAL, Items.KELP)
			.put(Items.EMERALD, Items.DIAMOND)
			.put(WISPY_ESSENCE, TWISTED_ESSENCE)
			.build();
	public static final Map<Item, Item> PURIFYING_SWAPS = Map.of(
			Items.RAW_IRON, ALCHEMICAL_IRON,
			Items.RAW_COPPER, ALCHEMICAL_COPPER,
			Items.RAW_GOLD, ALCHEMICAL_GOLD
	);
	
	public static Enchantment WARPING = new WarpingCurseEnchantment(Enchantment.Rarity.VERY_RARE, EquipmentSlot.values());
	public static Enchantment PROJECTING = new ProjectingEnchantment();
	public static LootSwapEnchantment TRANSMUTATIVE = new LootSwapEnchantment(EnchantmentTarget.WEAPON, TRANSMUTATIVE_SWAPS, 1, 1.0f);
	public static LootSwapEnchantment PURIFYING = new LootSwapEnchantment(EnchantmentTarget.DIGGER, PURIFYING_SWAPS, 3, 0.2f);
	public static Enchantment RUNIC_SHIELDING = new RunicShieldingEnchantment();
	
	// features...
	// TODO: move elsewhere? e.g. to each feature's class
	public static Feature<DefaultFeatureConfig> SURFACE_NODE_FEATURE = new SurfaceNodeFeature();
	public static ConfiguredFeature<?, ?> SURFACE_NODE_CONF_FEATURE = new ConfiguredFeature<>(SURFACE_NODE_FEATURE, DefaultFeatureConfig.INSTANCE);
	public static PlacedFeature SURFACE_NODE_PLACED_FEATURE = new PlacedFeature(
			RegistryEntry.of(SURFACE_NODE_CONF_FEATURE),
			List.of(HeightmapPlacementModifier.of(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES))
	);
	
	public static Feature<DefaultFeatureConfig> HANGING_NODE_FEATURE = new HangingNodeFeature();
	public static ConfiguredFeature<?, ?> HANGING_NODE_CONF_FEATURE = new ConfiguredFeature<>(HANGING_NODE_FEATURE, DefaultFeatureConfig.INSTANCE);
	public static PlacedFeature HANGING_NODE_PLACED_FEATURE = new PlacedFeature(
			RegistryEntry.of(HANGING_NODE_CONF_FEATURE),
			List.of(
					PlacedFeatures.BOTTOM_TO_TOP_RANGE,
					RarityFilterPlacementModifier.of(3),
					SquarePlacementModifier.of(),
					BiomePlacementModifier.of()
			)
	);
	
	// structures
	public static final RegistryEntry<StructurePool> CRIMSON_OUTPOST_STRUCTURE_POOL = StructurePools.register(
			new StructurePool(
					new Identifier("arcana:crimson_outpost"),
					new Identifier("empty"),
					ImmutableList.of(Pair.of(StructurePoolElement.ofLegacySingle("arcana:crimson_outpost"), 1)),
					StructurePool.Projection.RIGID
			)
	);
	
	public static final Structure CRIMSON_OUTPOST = new JigsawStructure(
			createStructureConfig(
					ArcanaTags.GREATWOOD_SPAWNABLE,
					Map.of(),
					GenerationStep.Feature.SURFACE_STRUCTURES,
					StructureTerrainAdaptation.BEARD_THIN
			),
			CRIMSON_OUTPOST_STRUCTURE_POOL,
			1,
			ConstantHeightProvider.create(YOffset.fixed(0)),
			false,
			Heightmap.Type.WORLD_SURFACE_WG
	);
	
	public static final StructurePlacement CRIMSON_OUTPOST_PLACEMENT = new RandomSpreadStructurePlacement(48, 12, SpreadType.LINEAR, 1256);
	
	public static final RegistryEntry<StructurePool> CRIMSON_CAMP_STRUCTURE_POOL = StructurePools.register(
			new StructurePool(
					new Identifier("arcana:crimson_camp"),
					new Identifier("empty"),
					ImmutableList.of(Pair.of(StructurePoolElement.ofLegacySingle("arcana:crimson_camp"), 1)),
					StructurePool.Projection.RIGID
			)
	);
	
	public static final Structure CRIMSON_CAMP = new JigsawStructure(
			createStructureConfig(
					ArcanaTags.GREATWOOD_SPAWNABLE,
					Map.of(),
					GenerationStep.Feature.SURFACE_STRUCTURES,
					StructureTerrainAdaptation.BEARD_THIN
			),
			CRIMSON_CAMP_STRUCTURE_POOL,
			1,
			ConstantHeightProvider.create(YOffset.fixed(0)),
			false,
			Heightmap.Type.WORLD_SURFACE_WG
	);
	
	public static final StructurePlacement CRIMSON_CAMP_PLACEMENT = new RandomSpreadStructurePlacement(38, 12, SpreadType.LINEAR, 1356);
	
	// particle types...
	public static DefaultParticleType TAINT_BUBBLE = FabricParticleTypes.simple();
	public static DefaultParticleType FLAME = FabricParticleTypes.simple();
	public static DefaultParticleType LIGHTNING = FabricParticleTypes.simple();
	
	public static ParticleType<CubeParticleEffect> WARDING_EFFECT = FabricParticleTypes.complex(CubeParticleEffect.PARAMETERS_FACTORY);
	
	public static ParticleType<BlockStateParticleEffect> HUNGRY_NODE_DISC = FabricParticleTypes.complex(BlockStateParticleEffect.PARAMETERS_FACTORY);
	public static ParticleType<BlockStateParticleEffect> HUNGRY_NODE_BLOCK = FabricParticleTypes.complex(BlockStateParticleEffect.PARAMETERS_FACTORY);
	public static ParticleType<ItemStackParticleEffect> INFUSION_ITEM = FabricParticleTypes.complex(ItemStackParticleEffect.PARAMETERS_FACTORY);
	public static ParticleType<AspectParticleEffect> ESSENTIA_STREAM = FabricParticleTypes.complex(AspectParticleEffect.PARAMETERS_FACTORY);
	
	// entities...
	public static final EntityType<ThrownAlumentumEntity> THROWN_ALUMENTUM = FabricEntityTypeBuilder
			.create(SpawnGroup.MISC, ThrownAlumentumEntity::new)
			.build();
	public static final EntityType<ThrownTaintBottleEntity> THROWN_TAINT_BOTTLE = FabricEntityTypeBuilder
			.<ThrownTaintBottleEntity>create(SpawnGroup.MISC, ThrownTaintBottleEntity::new)
			.trackRangeChunks(4)
			.trackedUpdateRate(10)
			.dimensions(EntityDimensions.fixed(0.25f, 0.25f))
			.build();
	public static final EntityType<PrismaticOrbEntity> PRISMATIC_ORB = FabricEntityTypeBuilder
			.create(SpawnGroup.MISC, PrismaticOrbEntity::new)
			.trackRangeChunks(4)
			.trackedUpdateRate(10)
			.dimensions(EntityDimensions.fixed(0.1f, 0.1f))
			.build();
	
	public static final EntityType<WispEntity> WISP = FabricEntityTypeBuilder
			.<WispEntity>createMob()
			.entityFactory(WispEntity::new)
			.spawnGroup(SpawnGroup.MISC)
			.defaultAttributes(WispEntity::createDefaultAttributes)
			.dimensions(EntityDimensions.fixed(1.5f, 1.5f))
			.fireImmune()
			.build();
	public static final EntityType<TaintedWispEntity> TAINTED_WISP = FabricEntityTypeBuilder
			.<TaintedWispEntity>createMob()
			.entityFactory(TaintedWispEntity::new)
			.spawnGroup(SpawnGroup.MISC)
			.defaultAttributes(TaintedWispEntity::createDefaultAttributes)
			.dimensions(EntityDimensions.fixed(1.5f, 1.5f))
			.fireImmune()
			.build();
	public static final EntityType<PureWispEntity> PURE_WISP = FabricEntityTypeBuilder
			.<PureWispEntity>createMob()
			.entityFactory(PureWispEntity::new)
			.spawnGroup(SpawnGroup.MISC)
			.defaultAttributes(PureWispEntity::createDefaultAttributes)
			.dimensions(EntityDimensions.fixed(1.5f, 1.5f))
			.fireImmune()
			.build();
	public static final EntityType<CoagulationEntity> COAGULATION = FabricEntityTypeBuilder
			.<CoagulationEntity>createMob()
			.entityFactory(CoagulationEntity::new)
			.spawnGroup(SpawnGroup.MISC)
			.defaultAttributes(CoagulationEntity::createDefaultAttributes)
			.dimensions(EntityDimensions.fixed(1.2f, 1.2f))
			.fireImmune()
			.build();
	
	public static final EntityType<CrimsonKnightEntity> CRIMSON_KNIGHT = FabricEntityTypeBuilder
			.createLiving()
			.entityFactory(CrimsonKnightEntity::new)
			.spawnGroup(SpawnGroup.MONSTER)
			.defaultAttributes(CrimsonKnightEntity::createKnightAttributes)
			.dimensions(EntityDimensions.fixed(1, 1.8f))
			.build();
	public static final EntityType<CrimsonArcherEntity> CRIMSON_ARCHER = FabricEntityTypeBuilder
			.createLiving()
			.entityFactory(CrimsonArcherEntity::new)
			.spawnGroup(SpawnGroup.MONSTER)
			.defaultAttributes(CrimsonArcherEntity::createArcherAttributes)
			.dimensions(EntityDimensions.fixed(1, 1.8f))
			.build();
	public static final EntityType<CrimsonProtectorEntity> CRIMSON_PROTECTOR = FabricEntityTypeBuilder
			.createLiving()
			.entityFactory(CrimsonProtectorEntity::new)
			.spawnGroup(SpawnGroup.MONSTER)
			.defaultAttributes(CrimsonProtectorEntity::createProtectorAttributes)
			.dimensions(EntityDimensions.fixed(1, 1.8f))
			.build();
	public static final EntityType<CrimsonMissionaryEntity> CRIMSON_MISSIONARY = FabricEntityTypeBuilder
			.createLiving()
			.entityFactory(CrimsonMissionaryEntity::new)
			.spawnGroup(SpawnGroup.MONSTER)
			.defaultAttributes(CrimsonMissionaryEntity::createMissionaryAttributes)
			.dimensions(EntityDimensions.fixed(1, 1.8f))
			.build();
	public static final EntityType<CrimsonHeavyKnightEntity> CRIMSON_HEAVY_KNIGHT = FabricEntityTypeBuilder
			.createLiving()
			.entityFactory(CrimsonHeavyKnightEntity::new)
			.spawnGroup(SpawnGroup.MONSTER)
			.defaultAttributes(CrimsonHeavyKnightEntity::createHeavyKnightAttributes)
			.dimensions(EntityDimensions.fixed(1, 1.8f))
			.build();
	public static final EntityType<CrimsonJesterEntity> CRIMSON_JESTER = FabricEntityTypeBuilder
			.createLiving()
			.entityFactory(CrimsonJesterEntity::new)
			.spawnGroup(SpawnGroup.MONSTER)
			.defaultAttributes(CrimsonJesterEntity::createJesterAttributes)
			.dimensions(EntityDimensions.fixed(1, 1.8f))
			.build();
	
	// entity groups... don't need registering
	@SuppressWarnings("InstantiationOfUtilityClass") // no, it's just an identity token
	public static final EntityGroup CRIMSON_GROUP = new EntityGroup();
	
	// damage sources... also don't need it
	public static final DamageSource HUNGRY_NODE_DAMAGE = new DamageSource("arcana.hungry_node");
	
	public static final List<Item> items = new ArrayList<>();
	public static final List<Block> blocks = new ArrayList<>();
	public static final List<ArcanaFluid> stillFluids = new ArrayList<>();
	
	public static void setup(){
		// fluids
		register("taint_goo", STILL_TAINT_GOO);
		register("flowing_taint_goo", FLOWING_TAINT_GOO);
		
		register("putrefaction", STILL_PUTREFACTION);
		register("flowing_putrefaction", FLOWING_PUTREFACTION);
		
		// items + wand components
		register("scribbled_notes", SCRIBBLED_NOTES);
		register("scribing_tools", SCRIBING_TOOLS);
		register("goggles_of_revealing", GOGGLES_OF_REVEALING);
		register("monocle_of_revealing", MONOCLE_OF_REVEALING);
		register("introspective_lens", INTROSPECTIVE_LENS);
		register("revelatory_lens", REVELATORY_LENS);
		register("flux_lens", FLUX_LENS);
		
		register("arcanum", ARCANUM);
		register("crimson_rites", CRIMSON_RITES);
		register("tome_of_sharing", TOME_OF_SHARING);
		register("cheaters_arcanum", CHEATERS_ARCANUM);
		
		register("research_notes", RESEARCH_NOTES);
		register("complete_research_notes", COMPLETE_RESEARCH_NOTES);
		
		register("taint_goo_bucket", TAINT_GOO_BUCKET);
		register("putrefaction_bucket", PUTREFACTION_BUCKET);
		
		register("flux_meter", FLUX_METER);
		register("taint_in_a_bottle", TAINT_IN_A_BOTTLE);
		register("drinkable_taint", DRINKABLE_TAINT);
		
		register("rarefied_sherbert", RAREFIED_SHERBERT);
		register("sobering_syrup", SOBERING_SYRUP);
		register("seafoam_soda", SEAFOAM_SODA);
		register("bedrock_candy", BEDROCK_CANDY);
		register("gummy_cubes", GUMMY_CUBES);
		register("twisted_liquorice", TWISTED_LIQUORICE);
		
		register("silverleaf_brew", SILVERLEAF_BREW);
		
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
		
		register("thaumium_ingot", THAUMIUM_INGOT);
		register("thaumium_nugget", THAUMIUM_NUGGET);
		
		register("void_metal_ingot", VOID_METAL_INGOT);
		register("void_metal_nugget", VOID_METAL_NUGGET);
		register("void_seed", VOID_SEED);
		register("void_metal_sword", VOID_METAL_SWORD);
		register("void_metal_shovel", VOID_METAL_SHOVEL);
		register("void_metal_pickaxe", VOID_METAL_PICKAXE);
		register("void_metal_axe", VOID_METAL_AXE);
		register("void_metal_hoe", VOID_METAL_HOE);
		register("void_metal_helmet", VOID_METAL_HELMET);
		register("void_metal_chestplate", VOID_METAL_CHESTPLATE);
		register("void_metal_leggings", VOID_METAL_LEGGINGS);
		register("void_metal_boots", VOID_METAL_BOOTS);
		
		register("silverleaf", SILVERLEAF);
		register("silverleaf_amalgamate", SILVERLEAF_AMALGAMATE);
		register("silverleaf_sword", SILVERLEAF_SWORD);
		register("silverleaf_shovel", SILVERLEAF_SHOVEL);
		register("silverleaf_pickaxe", SILVERLEAF_PICKAXE);
		register("silverleaf_axe", SILVERLEAF_AXE);
		register("silverleaf_hoe", SILVERLEAF_HOE);
		register("silverleaf_helmet", SILVERLEAF_HELMET);
		register("silverleaf_chestplate", SILVERLEAF_CHESTPLATE);
		register("silverleaf_leggings", SILVERLEAF_LEGGINGS);
		register("silverleaf_boots", SILVERLEAF_BOOTS);
		
		register("wispy_essence", WISPY_ESSENCE);
		register("twisted_essence", TWISTED_ESSENCE);
		register("bejeweled_beet_seeds", BEJEWELED_BEET_SEEDS);
		register("bejeweled_beet", BEJEWELED_BEET);
		register("spiral_sugar", SPIRAL_SUGAR);
		register("aberrant_flora", ABERRANT_FLORA);
		register("bloodlet_ruby", BLOODLET_RUBY);
		register("motile", MOTILE);
		register("motile_piece", MOTILE_PIECE);
		
		register("sword_of_the_zephyr", SWORD_OF_THE_ZEPHYR);
		register("shovel_of_the_earthmover", SHOVEL_OF_THE_EARTHMOVER);
		register("pickaxe_of_the_core", PICKAXE_OF_THE_CORE);
		register("axe_of_the_stream", AXE_OF_THE_STREAM);
		register("hoe_of_the_cycle", HOE_OF_THE_CYCLE);
		
		register("arcanium_scalpel", ARCANIUM_SCALPEL);
		register("silverleaf_scalpel", SILVERLEAF_SCALPEL);
		register("void_metal_scalpel", VOID_METAL_SCALPEL);
		
		register("emerald_necklace", EMERALD_NECKLACE);
		register("gold_ring", GOLD_RING);
		register("arcanium_ring", ARCANIUM_RING);
		register("adorned_ring", ADORNED_RING);
		register("plane_projection_ring", PLANE_PROJECTION_RING);
		register("ring_of_the_surging_barrier", RING_OF_THE_SURGING_BARRIER);
		register("ring_of_twin_heartbeats", RING_OF_TWIN_HEARTBEATS);
		register("amulet_of_runic_shielding", AMULET_OF_RUNIC_SHIELDING);
		register("amulet_of_unburdened_travel", AMULET_OF_UNBURDENED_TRAVEL);
		register("amulet_of_deafening_shielding", AMULET_OF_DEAFENING_SHIELDING);
		
		register("crimson_blade", CRIMSON_BLADE);
		register("crimson_longbow", CRIMSON_LONGBOW);
		register("crimson_leech", CRIMSON_LEECH);
		
		register("boots_of_the_traveller", BOOTS_OF_THE_TRAVELLER);
		register("boots_of_the_sailor", BOOTS_OF_THE_SAILOR);
		register("boots_of_the_reaper", BOOTS_OF_THE_REAPER);
		
		register("alchemical_iron", ALCHEMICAL_IRON);
		register("alchemical_gold", ALCHEMICAL_GOLD);
		register("alchemical_copper", ALCHEMICAL_COPPER);
		register("alchemical_arcanium", ALCHEMICAL_ARCANIUM);
		register("alumentum", ALUMENTUM);
		FuelRegistry.INSTANCE.add(ALUMENTUM, 1600 * 4); // 4x coal = half stack
		
		register("shattered_husk", SHATTERED_HUSK);
		register("synthetic_scaffolding", SYNTHETIC_SCAFFOLDING);
		register("formless_foam", FORMLESS_FOAM);
		ArcaneFurnaceBlock.substrateTimes.put(SYNTHETIC_SCAFFOLDING, new ArcaneFurnaceBlock.SubstrateData(15, 0x43FC48));
		ArcaneFurnaceBlock.substrateTimes.put(FORMLESS_FOAM, new ArcaneFurnaceBlock.SubstrateData(40, 0x2FD8C2));
		
		register("void_putty", VOID_PUTTY);
		ArcaneFurnaceBlock.substrateTimes.put(VOID_PUTTY, new ArcaneFurnaceBlock.SubstrateData(200, 0x852797));
		
		register("wand", WAND);
		
		register("focus_pouch", FOCUS_POUCH);
		
		register("fire_focus", FIRE_FOCUS);
		register("solar_flare_focus", SOLAR_FLARE_FOCUS);
		register("fetch_focus", FETCH_FOCUS);
		register("portable_hole_focus", PORTABLE_HOLE_FOCUS);
		register("light_focus", LIGHT_FOCUS);
		register("prismatic_light_focus", PRISMATIC_LIGHT_FOCUS);
		register("lightning_focus", LIGHTNING_FOCUS);
		register("equivalent_exchange_focus", EQUIVALENT_EXCHANGE_FOCUS);
		register("coagulation_focus", COAGULATION_FOCUS);
		register("crystal_capacitor_focus", CRYSTAL_CAPACITOR_FOCUS);
		register("ward_focus", WARD_FOCUS);
		register("consume_rebuke_focus", CONSUME_REBUKE_FOCUS);
		
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
		
		register("eldritch", ELDRITCH_BANNER_PATTERN_SHAPE);
		register("eldritch_banner_pattern", ELDRITCH_BANNER_PATTERN);
		
		register("empty_phial", EMPTY_PHIAL);
		register("primordial_pearl", PRIMORDIAL_PEARL);
		register("broken_amulet", BROKEN_AMULET);
		register("challengers_amulet", CHALLENGERS_AMULET);
		register("victors_medallion", VICTORS_MEDALLION);
		
		register("node_placer", NODE_PLACER);
		register("node_remover", NODE_REMOVER);
		register("flux_sponge", FLUX_SPONGE);
		register("taint_injector", TAINT_INJECTOR);
		register("taint_eraser", TAINT_ERASER);
		
		for(Aspect aspect : Aspects.getOrderedAspects()){
			var shortName = aspect.id().getPath();
			CrystalItem crystalItem = new CrystalItem(new Settings().group(Tab.CRYSTALS), aspect);
			register("crystals/" + shortName, crystalItem);
			Aspects.crystals.put(aspect, crystalItem);
			
			PhialItem phialItem = new PhialItem(new ArcanaItemSettings().fragile(aspect.colour()).group(Tab.PHIALS), aspect);
			register("phials/" + shortName, phialItem);
			Aspects.phials.put(aspect, phialItem);
			
			if(Aspects.primals.contains(aspect))
				ArcaneFurnaceBlock.substrateTimes.put(crystalItem, new ArcaneFurnaceBlock.SubstrateData(5, aspect.colour()));
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
		register("essentia_redirect", ESSENTIA_REDIRECT);
		register("essentia_router", ESSENTIA_ROUTER);
		register("warded_jar", WARDED_JAR);
		register("void_jar", VOID_JAR);
		register("distillery_pathfinder", DISTILLERY_PATHFINDER);
		
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
		register("thaumic_halo", THAUMIC_HALO, false);
		register("thaumic_halo", new BigBlockItem(THAUMIC_HALO, GROUPED));
		register("crystallization_press", CRYSTALLIZATION_PRESS);
		register("mystic_mist", MYSTIC_MIST);
		register("warded_campfire", WARDED_CAMPFIRE);
		register("crimson_campfire", CRIMSON_CAMPFIRE);
		
		register("arcanium_block", ARCANIUM_BLOCK);
		register("thaumium_block", THAUMIUM_BLOCK);
		register("void_metal_block", VOID_METAL_BLOCK);
		register("silverleaf_amalgamate_block", SILVERLEAF_AMALGAMATE_BLOCK);
		
		register("arcane_stone", ARCANE_STONE);
		register("arcane_stone_bricks", ARCANE_STONE_BRICKS);
		register("arcane_stone_tiles", ARCANE_STONE_TILES);
		register("arcane_stone_gleaming_tiles", ARCANE_STONE_GLEAMING_TILES);
		register("arcane_stone_inscribed_tiles", ARCANE_STONE_INSCRIBED_TILES);
		
		register("arcane_stone_slab", ARCANE_STONE_SLAB);
		register("arcane_stone_stairs", ARCANE_STONE_STAIRS);
		register("arcane_stone_pressure_plate", ARCANE_STONE_PRESSURE_PLATE);
		register("arcane_stone_button", ARCANE_STONE_BUTTON);
		register("arcane_stone_wall", ARCANE_STONE_WALL);
		
		register("arcane_stone_bricks_slab", ARCANE_STONE_BRICKS_SLAB);
		register("arcane_stone_bricks_stairs", ARCANE_STONE_BRICKS_STAIRS);
		register("arcane_stone_bricks_pressure_plate", ARCANE_STONE_BRICKS_PRESSURE_PLATE);
		register("arcane_stone_bricks_button", ARCANE_STONE_BRICKS_BUTTON);
		register("arcane_stone_bricks_wall", ARCANE_STONE_BRICKS_WALL);
		
		register("arcane_stone_tiles_slab", ARCANE_STONE_TILES_SLAB);
		register("arcane_stone_tiles_stairs", ARCANE_STONE_TILES_STAIRS);
		register("arcane_stone_tiles_pressure_plate", ARCANE_STONE_TILES_PRESSURE_PLATE);
		register("arcane_stone_tiles_button", ARCANE_STONE_TILES_BUTTON);
		register("arcane_stone_tiles_wall", ARCANE_STONE_TILES_WALL);
		
		register("silverwood_sapling", SILVERWOOD_SAPLING);
		register("silverwood_log", SILVERWOOD_LOG);
		register("silverwood_leaves", SILVERWOOD_LEAVES);
		register("silverwood_planks", SILVERWOOD_PLANKS);
		
		register("silverwood_wood", SILVERWOOD_WOOD);
		register("stripped_silverwood_log", STRIPPED_SILVERWOOD_LOG);
		register("stripped_silverwood_wood", STRIPPED_SILVERWOOD_WOOD);
		StrippableBlockRegistry.register(SILVERWOOD_LOG, STRIPPED_SILVERWOOD_LOG);
		StrippableBlockRegistry.register(SILVERWOOD_WOOD, STRIPPED_SILVERWOOD_WOOD);
		
		register("silverwood_slab", SILVERWOOD_SLAB);
		register("silverwood_stairs", SILVERWOOD_STAIRS);
		register("silverwood_fence", SILVERWOOD_FENCE);
		register("silverwood_fence_gate", SILVERWOOD_FENCE_GATE);
		register("silverwood_pressure_plate", SILVERWOOD_PRESSURE_PLATE);
		register("silverwood_button", SILVERWOOD_BUTTON);
		
		register("silverwood_door", SILVERWOOD_DOOR);
		register("silverwood_trapdoor", SILVERWOOD_TRAPDOOR);
		register("silverwood_sign", SILVERWOOD_SIGN, false);
		register("silverwood_wall_sign", SILVERWOOD_WALL_SIGN, false);
		register("silverwood_sign", new SignItem(new Settings().group(Tab.MAIN).maxCount(16), SILVERWOOD_SIGN, SILVERWOOD_WALL_SIGN));
		
		register("gleaming_silverwood_planks", GLEAMING_SILVERWOOD_PLANKS);
		register("solar_gleaming_silverwood_planks", SOLAR_GLEAMING_SILVERWOOD_PLANKS);
		
		register("greatwood_sapling", GREATWOOD_SAPLING);
		register("greatwood_log", GREATWOOD_LOG);
		register("greatwood_leaves", GREATWOOD_LEAVES);
		register("greatwood_planks", GREATWOOD_PLANKS);
		
		register("greatwood_wood", GREATWOOD_WOOD);
		register("stripped_greatwood_log", STRIPPED_GREATWOOD_LOG);
		register("stripped_greatwood_wood", STRIPPED_GREATWOOD_WOOD);
		StrippableBlockRegistry.register(GREATWOOD_LOG, STRIPPED_GREATWOOD_LOG);
		StrippableBlockRegistry.register(GREATWOOD_WOOD, STRIPPED_GREATWOOD_WOOD);
		
		register("greatwood_slab", GREATWOOD_SLAB);
		register("greatwood_stairs", GREATWOOD_STAIRS);
		register("greatwood_fence", GREATWOOD_FENCE);
		register("greatwood_fence_gate", GREATWOOD_FENCE_GATE);
		register("greatwood_pressure_plate", GREATWOOD_PRESSURE_PLATE);
		register("greatwood_button", GREATWOOD_BUTTON);
		
		register("greatwood_door", GREATWOOD_DOOR);
		register("greatwood_trapdoor", GREATWOOD_TRAPDOOR);
		register("greatwood_sign", GREATWOOD_SIGN, false);
		register("greatwood_wall_sign", GREATWOOD_WALL_SIGN, false);
		register("greatwood_sign", new SignItem(new Settings().group(Tab.MAIN).maxCount(16), GREATWOOD_SIGN, GREATWOOD_WALL_SIGN));
		
		register("gleaming_greatwood_planks", GLEAMING_GREATWOOD_PLANKS);
		register("solar_gleaming_greatwood_planks", SOLAR_GLEAMING_GREATWOOD_PLANKS);
		
		register("taintwood_log", TAINTWOOD_LOG);
		register("taintwood_planks", TAINTWOOD_PLANKS);
		register("taintwood_wood", TAINTWOOD_WOOD);
		
		register("taintwood_slab", TAINTWOOD_SLAB);
		register("taintwood_stairs", TAINTWOOD_STAIRS);
		register("taintwood_fence", TAINTWOOD_FENCE);
		register("taintwood_fence_gate", TAINTWOOD_FENCE_GATE);
		register("taintwood_pressure_plate", TAINTWOOD_PRESSURE_PLATE);
		register("taintwood_button", TAINTWOOD_BUTTON);
		register("taintwood_door", TAINTWOOD_DOOR);
		register("taintwood_trapdoor", TAINTWOOD_TRAPDOOR);
		
		register("hollowed_log", HOLLOWED_LOG);
		register("hollowed_planks", HOLLOWED_PLANKS);
		register("hollowed_wood", HOLLOWED_WOOD);
		
		register("hollowed_slab", HOLLOWED_SLAB);
		register("hollowed_stairs", HOLLOWED_STAIRS);
		register("hollowed_fence", HOLLOWED_FENCE);
		register("hollowed_fence_gate", HOLLOWED_FENCE_GATE);
		register("hollowed_pressure_plate", HOLLOWED_PRESSURE_PLATE);
		register("hollowed_button", HOLLOWED_BUTTON);
		register("hollowed_door", HOLLOWED_DOOR);
		register("hollowed_trapdoor", HOLLOWED_TRAPDOOR);
		
		// HACKFIX, since fabric halfassed this API
		BlockEntityType.SIGN.blocks = new HashSet<>(BlockEntityType.SIGN.blocks);
		BlockEntityType.SIGN.blocks.addAll(Set.of(SILVERWOOD_SIGN, SILVERWOOD_WALL_SIGN, GREATWOOD_SIGN, GREATWOOD_WALL_SIGN));
		
		register("vishroom", VISHROOM);
		register("cordispora", CORDISPORA);
		register("snowdrop", SNOWDROP);
		register("firewheel", FIREWHEEL);
		register("lilium", LILIUM);
		
		register("bejeweled_beets", BEJEWELED_BEETS_BLOCK, false);
		register("void_growth", VOID_GROWTH, false);
		
		register("speak_no_evil_statue", SPEAK_NO_EVIL_STATUE, false);
		register("speak_no_evil_statue", new BigBlockItem(SPEAK_NO_EVIL_STATUE, GROUPED));
		register("see_no_evil_statue", SEE_NO_EVIL_STATUE, false);
		register("see_no_evil_statue", new BigBlockItem(SEE_NO_EVIL_STATUE, GROUPED));
		register("hear_no_evil_statue", HEAR_NO_EVIL_STATUE, false);
		register("hear_no_evil_statue", new BigBlockItem(HEAR_NO_EVIL_STATUE, GROUPED));
		
		register("crimson_lantern", CRIMSON_LANTERN);
		register("chain_wall", CHAIN_WALL);
		register("metal_ladder", METAL_LADDER);
		
		register("gleaming_lamplight", GLEAMING_LAMPLIGHT);
		register("chiseled_gleaming_lamplight", CHISELED_GLEAMING_LAMPLIGHT);
		
		register("balanced_crystal", BALANCED_CRYSTAL);
		register("balanced_crystal_pillar", BALANCED_CRYSTAL_PILLAR);
		
		for(Aspect aspect : Aspects.hasCluster){
			var shortName = aspect.id().getPath();
			if(Aspects.primals.contains(aspect)){
				FabricBlockSettings settings = of(Material.AMETHYST, MapColor.WHITE)
						.dropsSelf()
						.usesTool(PICKAXE_MINEABLE)
						.sounds(BlockSoundGroup.AMETHYST_CLUSTER)
						.strength(0.9f)
						.luminance(3);
				Block crystalBlock = new Block(settings);
				register("crystal_blocks/" + shortName, crystalBlock);
				Aspects.crystalBlocks.put(aspect, crystalBlock);
				
				Block pillarBlock = new CrystalPillarBlock(settings);
				register("crystal_pillars/" + shortName, pillarBlock);
				Aspects.crystalPillars.put(aspect, pillarBlock);
			}
			
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
					aspect);
			register("clusters/" + shortName, clusterBlock);
			Aspects.clusters.put(aspect, clusterBlock);
			
			ClusterSeedItem seed = new ClusterSeedItem(clusterBlock, GROUPED, aspect);
			register("cluster_seeds/" + shortName, seed);
			Aspects.clusterSeeds.put(aspect, seed);
		}
		
		register("light_block", LIGHT_BLOCK, false);
		register("taint_goo", TAINT_GOO, false);
		register("putrefaction", PUTREFACTION, false);
		
		register("potted_greatwood_sapling", POTTED_GREATWOOD_SAPLING, false);
		register("potted_silverwood_sapling", POTTED_SILVERWOOD_SAPLING, false);
		register("potted_vishroom", POTTED_VISHROOM, false);
		register("potted_cordispora", POTTED_CORDISPORA, false);
		register("potted_snowdrop", POTTED_SNOWDROP, false);
		register("potted_firewheel", POTTED_FIREWHEEL, false);
		register("potted_lilium", POTTED_LILIUM, false);
		
		register("tainted_rock", TAINTED_ROCK);
		register("tainted_andesite", TAINTED_ANDESITE);
		register("tainted_granite", TAINTED_GRANITE);
		register("tainted_diorite", TAINTED_DIORITE);
		
		register("tainted_soil", TAINTED_SOIL);
		register("tainted_grass_block", TAINTED_GRASS_BLOCK);
		register("tainted_sand", TAINTED_SAND);
		register("tainted_sandstone", TAINTED_SANDSTONE);
		register("tainted_gravel", TAINTED_GRAVEL);
		register("tainted_snow_block", TAINTED_SNOW_BLOCK);
		
		register("tainted_hollowed_ore", TAINTED_HOLLOWED_ORE);
		
		register("taint_crust", TAINT_CRUST);
		
		register("hollowed_ore", HOLLOWED_ORE);
		
		// points of interest
		WARDED_CAMPFIRE_POI = PointOfInterestHelper.register(arcId("warded_campfire"), 0, 2, WARDED_CAMPFIRE.getStateManager().getStates().stream().filter(x -> x.get(Properties.LIT)).toList());
		
		// screen handlers
		register("arcane_crafting", ARCANE_CRAFTING_SCREEN_HANDLER);
		register("research_table", RESEARCH_TABLE_SCREEN_HANDLER);
		register("knowledgeable_dropper", KNOWLEDGEABLE_DROPPER_SCREEN_HANDLER);
		register("arcane_furnace", ARCANE_FURNACE_SCREEN_HANDLER);
		register("distillery_pathfinder", DISTILLERY_PATHFINDER_SCREEN_HANDLER);
		register("crystallization_press", CRYSTALLIZATION_PRESS_SCREEN_HANDLER);
		register("focus_pouch", FOCUS_POUCH_SCREEN_HANDLER);
		
		// block entities
		register("crucible", CRUCIBLE_BE);
		register("research_table", RESEARCH_TABLE_BE);
		register("knowledgeable_dropper", KNOWLEDGEABLE_DROPPER_BE);
		register("pedestal", PEDESTAL_BE);
		register("arcane_levitator", ARCANE_LEVITATOR_BE);
		register("infusion_pillar", INFUSION_PILLAR_BE);
		register("infusion_matrix", INFUSION_MATRIX_BE);
		register("warded_jar", WARDED_JAR_BE);
		register("void_jar", VOID_JAR_BE);
		register("crystallization_press", CRYSTALLIZATION_PRESS_BE);
		register("mystic_mist", MYSTIC_MIST_BE);
		register("warded_campfire", WARDED_CAMPFIRE_BE);
		register("crimson_campfire", CRIMSON_CAMPFIRE_BE);
		register("essentia_tube", ESSENTIA_TUBE_BE);
		register("essentia_pump", ESSENTIA_PUMP_BE);
		register("essentia_valve", ESSENTIA_VALVE_BE);
		register("essentia_redirect", ESSENTIA_REDIRECT_BE);
		register("essentia_router", ESSENTIA_ROUTER_BE);
		register("arcane_furnace", ARCANE_FURNACE_BE);
		register("alembic", ALEMBIC_BE);
		register("crimson_lantern", CRIMSON_LANTERN_BE);
		register("distillery_pathfinder", DISTILLERY_PATHFINDER_BE);
		register("thaumic_halo", THAUMIC_HALO_BE);
		
		// enchantments
		register("warping", WARPING);
		register("projecting", PROJECTING);
		register("transmutative", TRANSMUTATIVE);
		register("purifying", PURIFYING);
		register("runic_shielding", RUNIC_SHIELDING);
		
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
		
		register("hanging_node", HANGING_NODE_FEATURE);
		register("hanging_node", HANGING_NODE_CONF_FEATURE);
		register("hanging_node", HANGING_NODE_PLACED_FEATURE);
		
		register("silverwood_foliage", SilverwoodFoliagePlacer.TYPE);
		register("silverwood_trunk", SilverwoodTrunkPlacer.TYPE);
		register("silverwood_tree", SilverwoodTree.SILVERWOOD_TREE);
		register("silverwood_tree", SilverwoodTree.SCATTERED_SILVERWOOD_TREE);
		
		register("greatwood_foliage", GreatwoodFoliagePlacer.TYPE);
		register("greatwood_trunk", GreatwoodTrunkPlacer.TYPE);
		register("greatwood_tree", GreatwoodTree.GREATWOOD_TREE);
		register("greatwood_tree", GreatwoodTree.SCATTERED_GREATWOOD_TREE);
		
		// structures
		register("crimson_outpost", CRIMSON_OUTPOST, CRIMSON_OUTPOST_PLACEMENT);
		register("crimson_camp", CRIMSON_CAMP, CRIMSON_CAMP_PLACEMENT);
		
		// particle types
		register("taint_bubble", TAINT_BUBBLE);
		register("flame", FLAME);
		register("lightning", LIGHTNING);
		
		register("warding_effect", WARDING_EFFECT);
		
		register("hungry_node_disc", HUNGRY_NODE_DISC);
		register("hungry_node_block", HUNGRY_NODE_BLOCK);
		register("infusion_item", INFUSION_ITEM);
		register("essentia_stream", ESSENTIA_STREAM);
		
		// entity types
		register("thrown_alumentum", THROWN_ALUMENTUM);
		register("thrown_taint_bottle", THROWN_TAINT_BOTTLE);
		register("prismatic_orb", PRISMATIC_ORB);
		
		register("wisp", WISP);
		register("tainted_wisp", TAINTED_WISP);
		register("pure_wisp", PURE_WISP);
		register("coagulation", COAGULATION);
		
		register("crimson_knight", CRIMSON_KNIGHT);
		register("crimson_archer", CRIMSON_ARCHER);
		register("crimson_protector", CRIMSON_PROTECTOR);
		register("crimson_missionary", CRIMSON_MISSIONARY);
		register("crimson_jester", CRIMSON_JESTER);
		register("crimson_heavy_knight", CRIMSON_HEAVY_KNIGHT);
		
		// status effects
		register("tainted", TAINTED);
		register("warp_frail", WARP_FRAIL);
		register("arcane_aura", ARCANE_AURA);
		register("arcane_discharge", ARCANE_DISCHARGE);
		register("warp_ward", WARP_WARD);
		register("air_power", AIR_POWER);
		register("fire_power", FIRE_POWER);
		register("water_power", WATER_POWER);
		register("earth_power", EARTH_POWER);
		register("order_power", ORDER_POWER);
		register("entropy_power", ENTROPY_POWER);
		register("pressure", PRESSURE);
		
		// loot pool entry types
		register("tag_gift", TagGiftEntry.TYPE);
		
		// entity attributes
		// TODO: move elsewhere?
		register("max_shielding", RunicShielding.MAX_SHIELDING);
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
		if(andItem){
			Settings settings = new Settings().group(Tab.MAIN);
			if(block.settings instanceof ArcanaBlockSettings abs && abs.getGroup() != null)
				settings.group(abs.getGroup());
			register(name, new BlockItem(block, settings));
		}
	}
	
	private static void register(String name, Fluid fluid){
		Registry.register(Registry.FLUID, arcId(name), fluid);
		if(fluid instanceof ArcanaFluid af && af.isStill())
			stillFluids.add(af);
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
	
	private static void register(String name, StatusEffect effect){
		Registry.register(Registry.STATUS_EFFECT, arcId(name), effect);
	}
	
	private static void register(String name, BannerPattern effect){
		Registry.register(Registry.BANNER_PATTERN, arcId(name), effect);
	}
	
	private static void register(String name, LootPoolEntryType type){
		Registry.register(Registry.LOOT_POOL_ENTRY_TYPE, arcId(name), type);
	}
	
	private static void register(String name, Structure structure, StructurePlacement placement){
		RegistryKey<Structure> structureKey = RegistryKey.of(Registry.STRUCTURE_KEY, arcId(name));
		RegistryEntry<Structure> structureEntry = BuiltinRegistries.add(BuiltinRegistries.STRUCTURE, structureKey, structure);
		RegistryKey<StructureSet> setKey = RegistryKey.of(Registry.STRUCTURE_SET_KEY, arcId(name));
		BuiltinRegistries.add(BuiltinRegistries.STRUCTURE_SET, setKey, new StructureSet(structureEntry, placement));
	}
	
	private static void register(String name, EntityAttribute attribute){
		Registry.register(Registry.ATTRIBUTE, arcId(name), attribute);
	}
	
	private static void registerCapOnly(Cap cap){
		Cap.caps.put(cap.id(), cap);
	}
	
	private static void registerCoreOnly(Core core){
		Core.cores.put(core.id(), core);
	}
	
	private static Structure.Config createStructureConfig(TagKey<Biome> biomeTag, Map<SpawnGroup, StructureSpawns> spawns, GenerationStep.Feature featureStep, StructureTerrainAdaptation terrainAdaptation){
		return new Structure.Config(getOrCreateBiomeTag(biomeTag), spawns, featureStep, terrainAdaptation);
	}
	
	private static RegistryEntryList<Biome> getOrCreateBiomeTag(TagKey<Biome> key){
		return BuiltinRegistries.BIOME.getOrCreateEntryList(key);
	}
	
	private static ToIntFunction<BlockState> whenLit(int litLevel){
		return state -> state.get(Properties.LIT) ? litLevel : 0;
	}
	
	private static FoodComponent aspectCandyFood(StatusEffect effect){
		return new FoodComponent.Builder()
				.hunger(3)
				.saturationModifier(0.5f)
				.alwaysEdible()
				.snack()
				.statusEffect(new StatusEffectInstance(effect, 135 * 20, 0, true, true), 1)
				.build();
	}
}