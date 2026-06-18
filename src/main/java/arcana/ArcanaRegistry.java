package arcana;

import arcana.api.Cap;
import arcana.api.Core;
import arcana.api.CustomCreativePresentationItem;
import arcana.aspects.Aspect;
import arcana.aspects.Aspects;
import arcana.blocks.*;
import arcana.blocks.be.*;
import arcana.blocks.deco.CrystalPillarBlock;
import arcana.blocks.deco.NitorBlock;
import arcana.blocks.deco.StoneVaseBlock;
import arcana.blocks.deco.WoodenStatueBlock;
import arcana.blocks.tubes.*;
import arcana.cca_components.RunicShielding;
import arcana.client.particles.AspectParticleEffect;
import arcana.client.particles.CubeParticleEffect;
import arcana.effects.*;
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
import arcana.items.components.ArcanaItemComponentTypes;
import arcana.items.creative.FluxSpongeItem;
import arcana.items.creative.NodePlacerItem;
import arcana.items.creative.NodeRemoverItem;
import arcana.items.creative.TaintConverterItem;
import arcana.items.foci.*;
import arcana.screens.*;
import arcana.util.RandomChanceOnceLootCondition;
import arcana.util.TagGiftLootEntry;
import arcana.worldgen.HangingNodeFeature;
import arcana.worldgen.SurfaceNodeFeature;
import arcana.worldgen.geodes.NodalGeodeFeature;
import arcana.worldgen.greatwood.GreatwoodFoliagePlacer;
import arcana.worldgen.greatwood.GreatwoodTrunkPlacer;
import arcana.worldgen.mushroom.StructureMushroomFeature;
import arcana.worldgen.silverwood.SilverwoodFoliagePlacer;
import arcana.worldgen.silverwood.SilverwoodTrunkPlacer;
import com.unascribed.lib39.weld.api.BigBlock;
import com.unascribed.lib39.weld.api.BigBlockItem;
import de.dafuqs.fractal.api.ItemSubGroup;
import dev.emi.trinkets.api.TrinketItem;
import dev.emi.trinkets.api.TrinketsAttributeModifiersComponent;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.world.poi.PointOfInterestHelper;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.fabricmc.fabric.api.registry.StrippableBlockRegistry;
import net.minecraft.block.*;
import net.minecraft.block.entity.BannerPattern;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.*;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ColorCode;
import net.minecraft.util.Rarity;
import net.minecraft.world.gen.chunk.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.gen.chunk.placement.SpreadType;
import net.minecraft.world.gen.chunk.placement.StructurePlacement;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.foliage.FoliagePlacerType;
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
import static net.minecraft.registry.tag.BlockTags.*;

public final class ArcanaRegistry{
	
	public enum Tab{
		MAIN,
		RESOURCES,
		EQUIPMENT,
		WANDS,
		CRYSTALS,
		PHIALS,
		TAINTED,
		CREATIVE
	}
	
	private static final Map<Tab, List<Item>> ITEMS_BY_TAB = new EnumMap<>(Tab.class);
	
	private static final ArcanaItemSettings GROUPED = new ArcanaItemSettings().group(Tab.MAIN);
	private static final ArcanaItemSettings GROUPED_SINGLE = new ArcanaItemSettings().group(Tab.MAIN).maxCount(1);
	
	private static final ArcanaItemSettings GROUPED_RES = new ArcanaItemSettings().group(Tab.RESOURCES);
	
	private static final ArcanaItemSettings GROUPED_WAND = new ArcanaItemSettings().group(Tab.WANDS);
	private static final ArcanaItemSettings GROUPED_WAND_SINGLE = new ArcanaItemSettings().group(Tab.WANDS).maxCount(1);
	
	private static final ArcanaItemSettings GROUPED_CREATIVE_SINGLE = new ArcanaItemSettings().group(Tab.CREATIVE).maxCount(1).rarity(Rarity.EPIC);
	
	// fluids...
	public static final FlowableFluid STILL_TAINT_GOO = new TaintGooFluid(true);
	public static final FlowableFluid FLOWING_TAINT_GOO = new TaintGooFluid(false);
	
	public static final FlowableFluid STILL_PUTREFACTION = new PutrefactionFluid(true);
	public static final FlowableFluid FLOWING_PUTREFACTION = new PutrefactionFluid(false);
	
	// status effects...
	public static final Registerable<StatusEffect> TAINTED = new Registerable<StatusEffect>(new TaintedStatusEffect()).register(Registries.STATUS_EFFECT, "tainted");
	public static final Registerable<StatusEffect> WARP_FRAIL = new Registerable<StatusEffect>(new FrailWarpStatusEffect()).register(Registries.STATUS_EFFECT, "warp_frail");
	
	public static final Registerable<StatusEffect> ARCANE_AURA = new Registerable<StatusEffect>(new SetBonusStatusEffect()).register(Registries.STATUS_EFFECT, "arcane_aura");
	
	public static final Registerable<StatusEffect> ARCANE_DISCHARGE = new Registerable<StatusEffect>(new ArcanaStatusEffect(StatusEffectCategory.BENEFICIAL, 0xF881D6)).register(Registries.STATUS_EFFECT, "arcane_discharge");
	public static final Registerable<StatusEffect> WARP_WARD = new Registerable<StatusEffect>(new ArcanaStatusEffect(StatusEffectCategory.BENEFICIAL, 0xBFEBF8)).register(Registries.STATUS_EFFECT, "warp_ward");
	public static final Registerable<StatusEffect> AIR_POWER = new Registerable<StatusEffect>(new AspectPowerStatusEffect(Aspects.AIR)
			.addAttributeModifier(EntityAttributes.GENERIC_MOVEMENT_SPEED, arcId("air_power/movement_speed"), .1f, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)).register(Registries.STATUS_EFFECT, "air_power");
	public static final Registerable<StatusEffect> FIRE_POWER = new Registerable<StatusEffect>(new AspectPowerStatusEffect(Aspects.FIRE)).register(Registries.STATUS_EFFECT, "fire_power");
	public static final Registerable<StatusEffect> WATER_POWER = new Registerable<StatusEffect>(new AspectPowerStatusEffect(Aspects.WATER)).register(Registries.STATUS_EFFECT, "water_power");
	public static final Registerable<StatusEffect> EARTH_POWER = new Registerable<StatusEffect>(new AspectPowerStatusEffect(Aspects.EARTH)
			.addAttributeModifier(EntityAttributes.GENERIC_ATTACK_SPEED, arcId("earth_power/attack_speed"), .1f, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)).register(Registries.STATUS_EFFECT, "earth_power");
	public static final Registerable<StatusEffect> ORDER_POWER = new Registerable<StatusEffect>(new AspectPowerStatusEffect(Aspects.ORDER)
			.addAttributeModifier(EntityAttributes.GENERIC_ARMOR, arcId("order_power/armor"), 2, EntityAttributeModifier.Operation.ADD_VALUE)
			.addAttributeModifier(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, arcId("ordo_power/knockback_resistance"), .1f, EntityAttributeModifier.Operation.ADD_VALUE)).register(Registries.STATUS_EFFECT, "order_power");
	public static final Registerable<StatusEffect> ENTROPY_POWER = new Registerable<StatusEffect>(new AspectPowerStatusEffect(Aspects.ENTROPY)
			.addAttributeModifier(EntityAttributes.GENERIC_ATTACK_DAMAGE, arcId("entropy_power/attack_damage"), .1f, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)).register(Registries.STATUS_EFFECT, "entropy_power");
	
	public static final Registerable<StatusEffect> PRESSURE = new Registerable<StatusEffect>(new PressureStatusEffect()).register(Registries.STATUS_EFFECT, "pressure");
	
	// items...
	public static final Item SCRIBBLED_NOTES = new ScribbledNotesItem(GROUPED_SINGLE);
	public static final Item SCRIBING_TOOLS = new Item(new ArcanaItemSettings().group(Tab.MAIN).maxDamage(200));
	public static final Item GOGGLES_OF_REVEALING = new GogglesOfRevealingItem(new ArcanaItemSettings().group(Tab.MAIN).maxCount(1).maxDamage(100));
	public static final Item MONOCLE_OF_REVEALING = new TrinketItem(GROUPED_SINGLE);
	public static final Item INTROSPECTIVE_LENS = new Item(GROUPED_SINGLE);
	public static final Item REVELATORY_LENS = new Item(GROUPED_SINGLE);
	public static final Item FLUX_LENS = new Item(GROUPED_SINGLE);
	
	public static final Item ARCANUM = new ResearchBookItem(GROUPED_SINGLE, arcId("arcanum"));
	public static final Item CRIMSON_RITES = new ResearchBookItem(GROUPED_SINGLE, arcId("crimson_rites"));
	public static final Item TOME_OF_SHARING = new TomeOfSharingItem(GROUPED_SINGLE);
	public static final Item CHEATERS_ARCANUM = new CheatersArcanumItem(GROUPED_CREATIVE_SINGLE);
	
	public static final Item RESEARCH_NOTES = new ResearchNotesItem(new ArcanaItemSettings().maxCount(1), false);
	public static final Item COMPLETE_RESEARCH_NOTES = new ResearchNotesItem(new ArcanaItemSettings().maxCount(1), true);
	
	public static final Item TAINT_GOO_BUCKET = new BucketItem(STILL_TAINT_GOO, new ArcanaItemSettings().group(Tab.MAIN).maxCount(1).recipeRemainder(Items.BUCKET));
	public static final Item PUTREFACTION_BUCKET = new BucketItem(STILL_PUTREFACTION, new ArcanaItemSettings().group(Tab.MAIN).maxCount(1).recipeRemainder(Items.BUCKET));
	
	public static final Item FLUX_METER = new Item(GROUPED_SINGLE);
	public static final Item TAINT_IN_A_BOTTLE = new TaintInABottleItem(GROUPED);
	public static final Item DRINKABLE_TAINT = new DrinkableTaintItem(new ArcanaItemSettings().group(Tab.MAIN).maxCount(1).food(new FoodComponent.Builder()
			.nutrition(4)
			.saturationModifier(1.1f)
			.statusEffect(new StatusEffectInstance(TAINTED.entry(), 40 * 20, 1), 1)
			.build()));
	
	public static final Item PERSONAL_MAGIC_MIRROR = new PersonalMagicMirrorItem(new ArcanaItemSettings().group(Tab.MAIN).maxCount(1));
	
	public static final Item RAREFIED_SHERBERT = new Item(new ArcanaItemSettings().group(Tab.MAIN).food(aspectCandyFood(AIR_POWER.entry())));
	public static final Item SOBERING_SYRUP = new Item(new ArcanaItemSettings().group(Tab.MAIN).food(aspectCandyFood(FIRE_POWER.entry())));
	public static final Item SEAFOAM_SODA = new Item(new ArcanaItemSettings().group(Tab.MAIN).food(aspectCandyFood(WATER_POWER.entry())));
	public static final Item BEDROCK_CANDY = new Item(new ArcanaItemSettings().group(Tab.MAIN).food(aspectCandyFood(EARTH_POWER.entry())));
	public static final Item GUMMY_CUBES = new Item(new ArcanaItemSettings().group(Tab.MAIN).food(aspectCandyFood(ORDER_POWER.entry())));
	public static final Item TWISTED_LIQUORICE = new Item(new ArcanaItemSettings().group(Tab.MAIN).food(aspectCandyFood(ENTROPY_POWER.entry())));
	
	public static final Item SILVERLEAF_BREW = new DrinkItem(new ArcanaItemSettings().group(Tab.MAIN).maxCount(1).food(new FoodComponent.Builder()
			.nutrition(2)
			.saturationModifier(0.25f)
			.alwaysEdible()
			.statusEffect(new StatusEffectInstance(WARP_WARD.entry(), 8 * 60 * 20, 0, true, true), 1)
			.build()));
	
	public static final Item ARCANIUM_INGOT = new Item(GROUPED_RES);
	public static final Item ARCANIUM_SWORD = new SwordItem(ArcanaToolMaterials.ARCANIUM, new ArcanaItemSettings().group(Tab.EQUIPMENT).attributeModifiers(SwordItem.createAttributeModifiers(ArcanaToolMaterials.ARCANIUM, 3, -2.4f)));
	public static final Item ARCANIUM_SHOVEL = new ShovelItem(ArcanaToolMaterials.ARCANIUM, new ArcanaItemSettings().group(Tab.EQUIPMENT).attributeModifiers(ShovelItem.createAttributeModifiers(ArcanaToolMaterials.ARCANIUM, 1.5f, -3)));
	public static final Item ARCANIUM_PICKAXE = new PickaxeItem(ArcanaToolMaterials.ARCANIUM, new ArcanaItemSettings().group(Tab.EQUIPMENT).attributeModifiers(PickaxeItem.createAttributeModifiers(ArcanaToolMaterials.ARCANIUM, 1, -2.8f)));
	public static final Item ARCANIUM_AXE = new AxeItem(ArcanaToolMaterials.ARCANIUM, new ArcanaItemSettings().group(Tab.EQUIPMENT).attributeModifiers(AxeItem.createAttributeModifiers(ArcanaToolMaterials.ARCANIUM, 5.5f, -3)));
	public static final Item ARCANIUM_HOE = new HoeItem(ArcanaToolMaterials.ARCANIUM, new ArcanaItemSettings().group(Tab.EQUIPMENT).attributeModifiers(HoeItem.createAttributeModifiers(ArcanaToolMaterials.ARCANIUM, -2, -1)));
	public static final Item ARCANIUM_HELMET = new ArcanaArmorItem(ArcanaArmorMaterials.ARCANIUM, ArmorItem.Type.HELMET, new ArcanaItemSettings().group(Tab.EQUIPMENT));
	public static final Item ARCANIUM_CHESTPLATE = new ArcanaArmorItem(ArcanaArmorMaterials.ARCANIUM, ArmorItem.Type.CHESTPLATE, new ArcanaItemSettings().group(Tab.EQUIPMENT));
	public static final Item ARCANIUM_LEGGINGS = new ArcanaArmorItem(ArcanaArmorMaterials.ARCANIUM, ArmorItem.Type.LEGGINGS, new ArcanaItemSettings().group(Tab.EQUIPMENT));
	public static final Item ARCANIUM_BOOTS = new ArcanaArmorItem(ArcanaArmorMaterials.ARCANIUM, ArmorItem.Type.BOOTS, new ArcanaItemSettings().group(Tab.EQUIPMENT));
	
	public static final Item THAUMIUM_INGOT = new Item(GROUPED_RES);
	public static final Item THAUMIUM_NUGGET = new Item(GROUPED_RES);
	
	public static final Item VOID_METAL_INGOT = new Item(GROUPED_RES);
	public static final Item VOID_METAL_NUGGET = new Item(GROUPED_RES);
	public static final Item VOID_SEED = new Item(GROUPED_RES);
	public static final Item VOID_METAL_SWORD = new SwordItem(ArcanaToolMaterials.VOID_METAL, new ArcanaItemSettings().group(Tab.EQUIPMENT).attributeModifiers(SwordItem.createAttributeModifiers(ArcanaToolMaterials.VOID_METAL, 3, -2.4f)));
	public static final Item VOID_METAL_SHOVEL = new ShovelItem(ArcanaToolMaterials.VOID_METAL, new ArcanaItemSettings().group(Tab.EQUIPMENT).attributeModifiers(ShovelItem.createAttributeModifiers(ArcanaToolMaterials.VOID_METAL, 1.5f, -3)));
	public static final Item VOID_METAL_PICKAXE = new PickaxeItem(ArcanaToolMaterials.VOID_METAL, new ArcanaItemSettings().group(Tab.EQUIPMENT).attributeModifiers(PickaxeItem.createAttributeModifiers(ArcanaToolMaterials.VOID_METAL, 1, -2.8f)));
	public static final Item VOID_METAL_AXE = new AxeItem(ArcanaToolMaterials.VOID_METAL, new ArcanaItemSettings().group(Tab.EQUIPMENT).attributeModifiers(AxeItem.createAttributeModifiers(ArcanaToolMaterials.VOID_METAL, 5.5f, -3)));
	public static final Item VOID_METAL_HOE = new HoeItem(ArcanaToolMaterials.VOID_METAL, new ArcanaItemSettings().group(Tab.EQUIPMENT).attributeModifiers(HoeItem.createAttributeModifiers(ArcanaToolMaterials.VOID_METAL, -2, -1)));
	public static final Item VOID_METAL_HELMET = new ArcanaArmorItem(ArcanaArmorMaterials.VOID_METAL, ArmorItem.Type.HELMET, new ArcanaItemSettings().group(Tab.EQUIPMENT));
	public static final Item VOID_METAL_CHESTPLATE = new ArcanaArmorItem(ArcanaArmorMaterials.VOID_METAL, ArmorItem.Type.CHESTPLATE, new ArcanaItemSettings().group(Tab.EQUIPMENT));
	public static final Item VOID_METAL_LEGGINGS = new ArcanaArmorItem(ArcanaArmorMaterials.VOID_METAL, ArmorItem.Type.LEGGINGS, new ArcanaItemSettings().group(Tab.EQUIPMENT));
	public static final Item VOID_METAL_BOOTS = new ArcanaArmorItem(ArcanaArmorMaterials.VOID_METAL, ArmorItem.Type.HELMET, new ArcanaItemSettings().group(Tab.EQUIPMENT));
	
	public static final Item SILVERLEAF = new Item(GROUPED_RES);
	public static final Item SILVERLEAF_AMALGAMATE = new Item(GROUPED_RES);
	public static final Item SILVERLEAF_SWORD = new SwordItem(ArcanaToolMaterials.SILVERLEAF, new ArcanaItemSettings().group(Tab.EQUIPMENT).attributeModifiers(SwordItem.createAttributeModifiers(ArcanaToolMaterials.SILVERLEAF, 3, -2.4F)));
	public static final Item SILVERLEAF_SHOVEL = new ShovelItem(ArcanaToolMaterials.SILVERLEAF, new ArcanaItemSettings().group(Tab.EQUIPMENT).attributeModifiers(ShovelItem.createAttributeModifiers(ArcanaToolMaterials.SILVERLEAF, 1.5f, -3)));
	public static final Item SILVERLEAF_PICKAXE = new PickaxeItem(ArcanaToolMaterials.SILVERLEAF, new ArcanaItemSettings().group(Tab.EQUIPMENT).attributeModifiers(PickaxeItem.createAttributeModifiers(ArcanaToolMaterials.SILVERLEAF, 1, -2.8f)));
	public static final Item SILVERLEAF_AXE = new AxeItem(ArcanaToolMaterials.SILVERLEAF, new ArcanaItemSettings().group(Tab.EQUIPMENT).attributeModifiers(AxeItem.createAttributeModifiers(ArcanaToolMaterials.SILVERLEAF, 5.5f, -3)));
	public static final Item SILVERLEAF_HOE = new HoeItem(ArcanaToolMaterials.SILVERLEAF, new ArcanaItemSettings().group(Tab.EQUIPMENT).attributeModifiers(HoeItem.createAttributeModifiers(ArcanaToolMaterials.SILVERLEAF, -2, -1)));
	public static final Item SILVERLEAF_HELMET = new ArcanaArmorItem(ArcanaArmorMaterials.SILVERLEAF, ArmorItem.Type.HELMET, new ArcanaItemSettings().group(Tab.EQUIPMENT));
	public static final Item SILVERLEAF_CHESTPLATE = new ArcanaArmorItem(ArcanaArmorMaterials.SILVERLEAF, ArmorItem.Type.CHESTPLATE, new ArcanaItemSettings().group(Tab.EQUIPMENT));
	public static final Item SILVERLEAF_LEGGINGS = new ArcanaArmorItem(ArcanaArmorMaterials.SILVERLEAF, ArmorItem.Type.LEGGINGS, new ArcanaItemSettings().group(Tab.EQUIPMENT));
	public static final Item SILVERLEAF_BOOTS = new ArcanaArmorItem(ArcanaArmorMaterials.SILVERLEAF, ArmorItem.Type.BOOTS, new ArcanaItemSettings().group(Tab.EQUIPMENT));
	
	public static final Item WISPY_ESSENCE = new Item(GROUPED_RES);
	public static final Item TWISTED_ESSENCE = new Item(GROUPED_RES);
	public static final Item BEJEWELED_BEET = new Item(new ArcanaItemSettings().group(Tab.RESOURCES).food(new FoodComponent.Builder().nutrition(5).saturationModifier(1).build()));
	public static final Item SPIRAL_SUGAR = new Item(GROUPED_RES);
	public static final Item ABERRANT_FLORA = new Item(GROUPED_RES);
	public static final Item BLOODLET_RUBY = new Item(new ArcanaItemSettings().fragile(0xBC0826, StatusEffects.INSTANT_HEALTH).group(Tab.RESOURCES));
	public static final Item MOTILE = new MotileItem(new ArcanaItemSettings().group(Tab.RESOURCES).rarity(Rarity.UNCOMMON));
	public static final Item MOTILE_PIECE = new MotileItem(new ArcanaItemSettings().group(Tab.RESOURCES).rarity(Rarity.UNCOMMON));
	
	public static final Item SWORD_OF_THE_ZEPHYR = new SwordItem(ArcanaToolMaterials.PRIMAL, new ArcanaItemSettings().group(Tab.EQUIPMENT).rarity(Rarity.UNCOMMON).attributeModifiers(SwordItem.createAttributeModifiers(ArcanaToolMaterials.PRIMAL, 3, -2.4F)));
	public static final Item SHOVEL_OF_THE_EARTHMOVER = new ShovelItem(ArcanaToolMaterials.PRIMAL, new ArcanaItemSettings().group(Tab.EQUIPMENT).rarity(Rarity.UNCOMMON).attributeModifiers(ShovelItem.createAttributeModifiers(ArcanaToolMaterials.PRIMAL, 1.5f, -3)).component(DataComponentTypes.TOOL, ArcanaToolMaterials.PRIMAL.createComponent(ArcanaTags.EARTHMOVER_MINEABLE)));
	public static final Item PICKAXE_OF_THE_CORE = new PickaxeItem(ArcanaToolMaterials.PRIMAL, new ArcanaItemSettings().group(Tab.EQUIPMENT).rarity(Rarity.UNCOMMON).attributeModifiers(PickaxeItem.createAttributeModifiers(ArcanaToolMaterials.PRIMAL, 1, -2.8f)));
	public static final Item AXE_OF_THE_STREAM = new AxeItem(ArcanaToolMaterials.PRIMAL, new ArcanaItemSettings().group(Tab.EQUIPMENT).rarity(Rarity.UNCOMMON).attributeModifiers(AxeItem.createAttributeModifiers(ArcanaToolMaterials.PRIMAL, 5.5f, -3)));
	public static final Item HOE_OF_THE_CYCLE = new HoeItem(ArcanaToolMaterials.PRIMAL, new ArcanaItemSettings().group(Tab.EQUIPMENT).rarity(Rarity.UNCOMMON).attributeModifiers(HoeItem.createAttributeModifiers(ArcanaToolMaterials.PRIMAL, -2, -1)));
	
	public static final Item ARCANIUM_SCALPEL = new ScalpelItem(new ArcanaItemSettings().group(Tab.EQUIPMENT).maxDamage(129).attributeModifiers(ScalpelItem.createAttributeModifiers(ScalpelItem.ScalpelType.ROSE)), ScalpelItem.ScalpelType.ROSE);
	public static final Item SILVERLEAF_SCALPEL = new ScalpelItem(new ArcanaItemSettings().group(Tab.EQUIPMENT).maxDamage(315).attributeModifiers(ScalpelItem.createAttributeModifiers(ScalpelItem.ScalpelType.SILVER)), ScalpelItem.ScalpelType.SILVER);
	public static final Item VOID_METAL_SCALPEL = new ScalpelItem(new ArcanaItemSettings().group(Tab.EQUIPMENT).maxDamage(101).attributeModifiers(ScalpelItem.createAttributeModifiers(ScalpelItem.ScalpelType.BLACK)), ScalpelItem.ScalpelType.BLACK);
	
	public static final Item GOLD_RING = new TrinketItem(new ArcanaItemSettings().group(Tab.EQUIPMENT).maxCount(1));
	public static final Item ARCANIUM_RING = new TrinketItem(new ArcanaItemSettings().group(Tab.EQUIPMENT).maxCount(1));
	public static final Item ADORNED_RING = new VisDiscountTrinketItem(new ArcanaItemSettings().group(Tab.EQUIPMENT).maxCount(1), 5);
	public static final Item LAMPLIGHT_RING = new LamplightTrinketItem(new ArcanaItemSettings().group(Tab.EQUIPMENT).maxCount(1));
	public static final Item PLANE_PROJECTION_RING = new TrinketItem(new ArcanaItemSettings().group(Tab.EQUIPMENT).maxCount(1));
	public static final Item RING_OF_THE_SURGING_BARRIER = new TrinketItem(new ArcanaItemSettings().group(Tab.EQUIPMENT).maxCount(1).rarity(Rarity.UNCOMMON));
	public static final Item RING_OF_TWIN_HEARTBEATS = new WarpingTrinketItem(new ArcanaItemSettings().group(Tab.EQUIPMENT).maxCount(1).rarity(Rarity.UNCOMMON));
	public static final Item RING_OF_THE_VOIDGAZER = new WarpBasedDiscountTrinketItem(new ArcanaItemSettings().group(Tab.EQUIPMENT).maxCount(1).rarity(Rarity.UNCOMMON));
	public static final Item EMERALD_NECKLACE = new TrinketItem(new ArcanaItemSettings().group(Tab.EQUIPMENT).maxCount(1));
	public static final Item AMULET_OF_RUNIC_SHIELDING = new TrinketItem(new ArcanaItemSettings().group(Tab.EQUIPMENT).maxCount(1).component(TrinketsAttributeModifiersComponent.TYPE, RunicShielding.createTrinketModifiers(2)));
	public static final Item AMULET_OF_UNBURDENED_TRAVEL = new TrinketItem(new ArcanaItemSettings().group(Tab.EQUIPMENT).maxCount(1).component(TrinketsAttributeModifiersComponent.TYPE, RunicShielding.createTrinketModifiers(6)));
	public static final Item AMULET_OF_DEAFENING_SHIELDING = new TrinketItem(new ArcanaItemSettings().group(Tab.EQUIPMENT).maxCount(1).component(TrinketsAttributeModifiersComponent.TYPE, RunicShielding.createTrinketModifiers(1)));
	
	public static final Item CRIMSON_BLADE = new SwordItem(ArcanaToolMaterials.CRIMSON, new ArcanaItemSettings().group(Tab.EQUIPMENT).attributeModifiers(SwordItem.createAttributeModifiers(ArcanaToolMaterials.CRIMSON, 3, -2.4f)));
	public static final Item CRIMSON_LONGBOW = new CrimsonLongbowItem(new ArcanaItemSettings().group(Tab.EQUIPMENT).maxDamage(564));
	public static final Item CRIMSON_LEECH = new CrimsonLeechItem(new ArcanaItemSettings().group(Tab.EQUIPMENT).rarity(Rarity.UNCOMMON).maxDamage(874).attributeModifiers(CrimsonLeechItem.createAttributeModifiers()));
	
	public static final Item BOOTS_OF_THE_TRAVELLER = new BootsOfTheTravellerItem(ArcanaArmorMaterials.BOOTS_OF_THE_TRAVELLER.entry(), new ArcanaItemSettings().group(Tab.EQUIPMENT).attributeModifiers(BootsOfTheTravellerItem.createAttributeModifiers()));
	public static final Item BOOTS_OF_THE_SAILOR = new BootsOfTheTravellerItem(ArcanaArmorMaterials.BOOTS_OF_THE_SAILOR.entry(), new ArcanaItemSettings().group(Tab.EQUIPMENT).attributeModifiers(BootsOfTheTravellerItem.createAttributeModifiers()));
	public static final Item BOOTS_OF_THE_REAPER = new BootsOfTheTravellerItem(ArcanaArmorMaterials.BOOTS_OF_THE_REAPER.entry(), new ArcanaItemSettings().group(Tab.EQUIPMENT).attributeModifiers(BootsOfTheTravellerItem.createAttributeModifiers()));
	
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
	public static final Item CRYSTAL_CAPACITOR_FOCUS = new CrystalCapacitorFocusItem(new ArcanaItemSettings().group(Tab.WANDS).maxCount(1).maxDamage(6));
	public static final Item WARD_FOCUS = new WardFocusItem(new ArcanaItemSettings().group(Tab.WANDS).maxCount(1).rarity(Rarity.UNCOMMON));
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
	public static final BannerPattern ELDRITCH_BANNER_PATTERN_SHAPE = new BannerPattern(arcId("eldritch"), "block.arcana.banner.eldritch");
	public static final Item ELDRITCH_BANNER_PATTERN = new BannerPatternItem(ArcanaTags.ELDRITCH_BANNER_PATTERNS, new ArcanaItemSettings().group(Tab.MAIN).maxCount(1).rarity(Rarity.UNCOMMON));
	
	// other...?
	public static final Item EMPTY_PHIAL = new PhialItem(new ArcanaItemSettings().group(Tab.PHIALS), null);
	public static final Item PRIMORDIAL_PEARL = new PrimordialPearlItem(new ArcanaItemSettings().group(Tab.RESOURCES).maxCount(1).rarity(Rarity.EPIC));
	public static final Item BROKEN_AMULET = new TrinketItem(new ArcanaItemSettings().group(Tab.RESOURCES).maxCount(1));
	public static final Item CHALLENGERS_AMULET = new TrinketItem(GROUPED_SINGLE);
	public static final Item VICTORS_MEDALLION = new TrinketItem(GROUPED_SINGLE);
	
	// creative-only
	public static final Item NODE_PLACER = new NodePlacerItem(GROUPED_CREATIVE_SINGLE);
	public static final Item NODE_REMOVER = new NodeRemoverItem(GROUPED_CREATIVE_SINGLE);
	public static final Item FLUX_SPONGE = new FluxSpongeItem(GROUPED_CREATIVE_SINGLE);
	public static final Item TAINT_INJECTOR = new TaintConverterItem(GROUPED_CREATIVE_SINGLE, true, true);
	public static final Item TAINT_ERASER = new TaintConverterItem(GROUPED_CREATIVE_SINGLE, false, true);
	public static final Item INFESTATION_INJECTOR = new TaintConverterItem(GROUPED_CREATIVE_SINGLE, true, false);
	public static final Item INFESTATION_ERASER = new TaintConverterItem(GROUPED_CREATIVE_SINGLE, false, false);
	
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
	public static final Block HARDENED_GLASS = new TranslucentBlock(of(Material.GLASS).dropsSelf().usesTool(PICKAXE_MINEABLE).renderLayer(CUTOUT).strength(3, 10).sounds(BlockSoundGroup.GLASS).nonOpaque().allowsSpawning(Blocks::never).solidBlock(Blocks::never).suffocates(Blocks::never).blockVision(Blocks::never));
	public static final Block LUMINIFEROUS_GLASS = new TranslucentBlock(of(Material.GLASS).dropsSelf().usesTool(PICKAXE_MINEABLE).renderLayer(TRANSLUCENT).luminance(15).strength(.6f).sounds(BlockSoundGroup.GLASS).nonOpaque().allowsSpawning(Blocks::never).solidBlock(Blocks::never).suffocates(Blocks::never).blockVision(Blocks::never));
	public static final Block STATIC_GLASS = new StaticGlassBlock(of(Material.GLASS).dropsSelf().usesTool(PICKAXE_MINEABLE).renderLayer(TRANSLUCENT).strength(.6f).sounds(BlockSoundGroup.GLASS).nonOpaque().allowsSpawning(Blocks::never).solidBlock(Blocks::never).suffocates(Blocks::never).blockVision(Blocks::never));
	public static final Block PAVING_STONE_OF_TRAVEL = new PavingStoneOfTravelBlock(of(Material.STONE).dropsSelf().requiresTool(PICKAXE_MINEABLE).strength(3, 7));
	public static final Block PAVING_STONE_OF_WARDING = new PavingStoneOfWardingBlock(of(Material.STONE).dropsSelf().requiresTool(PICKAXE_MINEABLE).strength(3.5f, 7));
	public static final Block PEDESTAL = new PedestalBlock(of(Material.STONE).dropsSelf().requiresTool(PICKAXE_MINEABLE).strength(3).nonOpaque());
	public static final Block GREATWOOD_SCRIBING_DESK = OrientableBigBlock.create(IntProperty.of("x", 0, 1), null, null, of(Material.WOOD).dropsSelf().renderLayer(CUTOUT).usesTool(AXE_MINEABLE).nonOpaque().strength(3));
	public static final Block SILVERWOOD_SCRIBING_DESK = OrientableBigBlock.create(IntProperty.of("x", 0, 1), null, null, of(Material.WOOD).dropsSelf().renderLayer(CUTOUT).usesTool(AXE_MINEABLE).nonOpaque().strength(3));
	public static final Block ARCANE_LEVITATOR = new ArcaneLevitatorBlock(of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).sounds(BlockSoundGroup.WOOD).strength(2));
	public static final BigBlock THAUMIC_HALO = new ThaumicHaloBlock(of(Material.METAL).dropsSelf().requiresTool(PICKAXE_MINEABLE).renderLayer(CUTOUT).strength(3).nonOpaque());
	public static final Block CRYSTALLIZATION_PRESS = new CrystallizationPressBlock(of(Material.METAL).dropsSelf().requiresTool(PICKAXE_MINEABLE).sounds(BlockSoundGroup.ANCIENT_DEBRIS).strength(4).nonOpaque());
	public static final Block MYSTIC_MIST = new MysticMistBlock(of(Material.STONE).dropsSelf().requiresTool(PICKAXE_MINEABLE).sounds(BlockSoundGroup.METAL).strength(2.5f).nonOpaque());
	public static final Block MAGIC_MIRROR = new MagicMirrorBlock(of(Material.METAL).usesTool(PICKAXE_MINEABLE).strength(0.7f).sounds(BlockSoundGroup.GLASS).nonOpaque().noCollision().luminance(2));
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
	public static final Block ARCANE_STONE_PRESSURE_PLATE = new PressurePlateBlock(ArcanaBlockSetTypes.GENERIC_STONE, of(Material.STONE).dropsSelf().requiresTool(PICKAXE_MINEABLE).strength(.5f));
	public static final Block ARCANE_STONE_BUTTON = new ButtonBlock(ArcanaBlockSetTypes.GENERIC_STONE, 20, of(Material.STONE).dropsSelf().noCollision().strength(.5f));
	public static final Block ARCANE_STONE_WALL = new WallBlock(of(Material.STONE).dropsSelf().requiresTool(PICKAXE_MINEABLE).strength(2));
	
	public static final Block ARCANE_STONE_BRICKS_SLAB = new SlabBlock(of(Material.STONE).requiresTool(PICKAXE_MINEABLE).strength(3, 7).sounds(BlockSoundGroup.WOOD));
	public static final Block ARCANE_STONE_BRICKS_STAIRS = new StairsBlock(ARCANE_STONE_BRICKS.getDefaultState(), of(Material.STONE).dropsSelf().requiresTool(PICKAXE_MINEABLE).strength(2));
	public static final Block ARCANE_STONE_BRICKS_PRESSURE_PLATE = new PressurePlateBlock(ArcanaBlockSetTypes.GENERIC_STONE, of(Material.STONE).dropsSelf().requiresTool(PICKAXE_MINEABLE).strength(.5f));
	public static final Block ARCANE_STONE_BRICKS_BUTTON = new ButtonBlock(ArcanaBlockSetTypes.GENERIC_STONE, 20, of(Material.STONE).dropsSelf().noCollision().strength(.5f));
	public static final Block ARCANE_STONE_BRICKS_WALL = new WallBlock(of(Material.STONE).dropsSelf().requiresTool(PICKAXE_MINEABLE).strength(2));
	
	public static final Block ARCANE_STONE_TILES_SLAB = new SlabBlock(of(Material.STONE).requiresTool(PICKAXE_MINEABLE).strength(3, 7).sounds(BlockSoundGroup.WOOD));
	public static final Block ARCANE_STONE_TILES_STAIRS = new StairsBlock(ARCANE_STONE_TILES.getDefaultState(), of(Material.STONE).dropsSelf().requiresTool(PICKAXE_MINEABLE).strength(2));
	public static final Block ARCANE_STONE_TILES_PRESSURE_PLATE = new PressurePlateBlock(ArcanaBlockSetTypes.GENERIC_STONE, of(Material.STONE).dropsSelf().requiresTool(PICKAXE_MINEABLE).strength(.5f));
	public static final Block ARCANE_STONE_TILES_BUTTON = new ButtonBlock(ArcanaBlockSetTypes.GENERIC_STONE, 20, of(Material.STONE).dropsSelf().noCollision().strength(.5f));
	public static final Block ARCANE_STONE_TILES_WALL = new WallBlock(of(Material.STONE).dropsSelf().requiresTool(PICKAXE_MINEABLE).strength(2));
	
	public static final SaplingGenerator SILVERWOOD_SAPLING_GEN = new SaplingGenerator(
			"arcana:silverwood_sapling", Optional.of(RegistryKey.of(RegistryKeys.CONFIGURED_FEATURE, arcId("silverwood_tree"))), Optional.empty(), Optional.empty()
	);
	public static final Block SILVERWOOD_SAPLING = new SaplingBlock(SILVERWOOD_SAPLING_GEN, of(Material.PLANT).dropsSelf().renderLayer(CUTOUT).noCollision().ticksRandomly().breakInstantly().sounds(BlockSoundGroup.GRASS));
	public static final Block SILVERWOOD_LOG = new PillarBlock(of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).strength(2).sounds(BlockSoundGroup.WOOD));
	public static final Block SILVERWOOD_LEAVES = new LeavesBlock(of(Material.LEAVES).renderLayer(CUTOUT).strength(.2f).ticksRandomly().sounds(BlockSoundGroup.GRASS).nonOpaque().allowsSpawning(Blocks::canSpawnOnLeaves).suffocates(Blocks::never).blockVision(Blocks::never));
	public static final Block SILVERWOOD_PLANKS = new Block(of(Material.WOOD).dropsSelf().strength(2, 3).sounds(BlockSoundGroup.WOOD));
	
	public static final Block SILVERWOOD_WOOD = new PillarBlock(of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).strength(2).sounds(BlockSoundGroup.WOOD));
	public static final Block STRIPPED_SILVERWOOD_LOG = new PillarBlock(of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).strength(2).sounds(BlockSoundGroup.WOOD));
	public static final Block STRIPPED_SILVERWOOD_WOOD = new PillarBlock(of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).strength(2).sounds(BlockSoundGroup.WOOD));
	
	public static final Block SILVERWOOD_SLAB = new SlabBlock(of(Material.WOOD).usesTool(AXE_MINEABLE).strength(2).sounds(BlockSoundGroup.WOOD));
	public static final Block SILVERWOOD_STAIRS = new StairsBlock(SILVERWOOD_PLANKS.getDefaultState(), of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).strength(2).sounds(BlockSoundGroup.WOOD));
	public static final Block SILVERWOOD_FENCE = new FenceBlock(of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).strength(2).sounds(BlockSoundGroup.WOOD));
	public static final Block SILVERWOOD_FENCE_GATE = new FenceGateBlock(ArcanaBlockSetTypes.SILVERWOOD, of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).strength(2).sounds(BlockSoundGroup.WOOD));
	public static final Block SILVERWOOD_PRESSURE_PLATE = new PressurePlateBlock(ArcanaBlockSetTypes.GENERIC_WOOD, of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).strength(.5f).sounds(BlockSoundGroup.WOOD));
	public static final Block SILVERWOOD_BUTTON = new ButtonBlock(ArcanaBlockSetTypes.GENERIC_WOOD, 30, of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).strength(.5f).noCollision().sounds(BlockSoundGroup.WOOD));
	
	public static final Block SILVERWOOD_DOOR = new DoorBlock(ArcanaBlockSetTypes.GENERIC_WOOD, of(Material.WOOD).usesTool(AXE_MINEABLE).renderLayer(CUTOUT).strength(3).sounds(BlockSoundGroup.WOOD).nonOpaque());
	public static final Block SILVERWOOD_TRAPDOOR = new TrapdoorBlock(ArcanaBlockSetTypes.GENERIC_WOOD, of(Material.WOOD).usesTool(AXE_MINEABLE).renderLayer(CUTOUT).dropsSelf().strength(3).sounds(BlockSoundGroup.WOOD).nonOpaque().allowsSpawning(Blocks::never));
	public static final Block SILVERWOOD_SIGN = new SignBlock(ArcanaBlockSetTypes.SILVERWOOD, of(Material.WOOD).usesTool(AXE_MINEABLE).renderLayer(CUTOUT).strength(3).sounds(BlockSoundGroup.WOOD).nonOpaque());
	public static final Block SILVERWOOD_WALL_SIGN = new WallSignBlock(ArcanaBlockSetTypes.SILVERWOOD, of(Material.WOOD).usesTool(AXE_MINEABLE).renderLayer(CUTOUT).dropsLike(SILVERWOOD_SIGN).strength(3).sounds(BlockSoundGroup.WOOD).nonOpaque());
	
	public static final Block GLEAMING_SILVERWOOD_PLANKS = new Block(of(Material.WOOD).dropsSelf().strength(2, 3).sounds(BlockSoundGroup.WOOD));
	public static final Block SOLAR_GLEAMING_SILVERWOOD_PLANKS = new Block(of(Material.WOOD).dropsSelf().strength(2, 3).sounds(BlockSoundGroup.WOOD));
	
	public static final SaplingGenerator GREATWOOD_SAPLING_GEN = new SaplingGenerator(
			"arcana:greatwood_sapling", Optional.of(RegistryKey.of(RegistryKeys.CONFIGURED_FEATURE, arcId("greatwood_tree"))), Optional.empty(), Optional.empty()
	);
	public static final Block GREATWOOD_SAPLING = new SaplingBlock(GREATWOOD_SAPLING_GEN, of(Material.PLANT).dropsSelf().renderLayer(CUTOUT).noCollision().ticksRandomly().breakInstantly().sounds(BlockSoundGroup.GRASS));
	public static final Block GREATWOOD_LOG = new PillarBlock(of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).strength(2).sounds(BlockSoundGroup.WOOD));
	public static final Block GREATWOOD_LEAVES = new LeavesBlock(of(Material.LEAVES).renderLayer(CUTOUT).strength(.2f).ticksRandomly().sounds(BlockSoundGroup.GRASS).nonOpaque().allowsSpawning(Blocks::canSpawnOnLeaves).suffocates(Blocks::never).blockVision(Blocks::never));
	public static final Block GREATWOOD_PLANKS = new Block(of(Material.WOOD).dropsSelf().strength(2, 3).sounds(BlockSoundGroup.WOOD));
	
	public static final Block GREATWOOD_WOOD = new PillarBlock(of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).strength(2).sounds(BlockSoundGroup.WOOD));
	public static final Block STRIPPED_GREATWOOD_LOG = new PillarBlock(of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).strength(2).sounds(BlockSoundGroup.WOOD));
	public static final Block STRIPPED_GREATWOOD_WOOD = new PillarBlock(of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).strength(2).sounds(BlockSoundGroup.WOOD));
	
	public static final Block GREATWOOD_SLAB = new SlabBlock(of(Material.WOOD).usesTool(AXE_MINEABLE).strength(2).sounds(BlockSoundGroup.WOOD));
	public static final Block GREATWOOD_STAIRS = new StairsBlock(GREATWOOD_PLANKS.getDefaultState(), of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).strength(2).sounds(BlockSoundGroup.WOOD));
	public static final Block GREATWOOD_FENCE = new FenceBlock(of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).strength(2).sounds(BlockSoundGroup.WOOD));
	public static final Block GREATWOOD_FENCE_GATE = new FenceGateBlock(ArcanaBlockSetTypes.GREATWOOD, of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).strength(2).sounds(BlockSoundGroup.WOOD));
	public static final Block GREATWOOD_PRESSURE_PLATE = new PressurePlateBlock(ArcanaBlockSetTypes.GENERIC_WOOD, of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).strength(.5f).sounds(BlockSoundGroup.WOOD));
	public static final Block GREATWOOD_BUTTON = new ButtonBlock(ArcanaBlockSetTypes.GENERIC_WOOD, 30, of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).strength(.5f).noCollision().sounds(BlockSoundGroup.WOOD));
	
	public static final Block GREATWOOD_DOOR = new DoorBlock(ArcanaBlockSetTypes.GENERIC_WOOD, of(Material.WOOD).usesTool(AXE_MINEABLE).renderLayer(CUTOUT).strength(3).sounds(BlockSoundGroup.WOOD).nonOpaque());
	public static final Block GREATWOOD_TRAPDOOR = new TrapdoorBlock(ArcanaBlockSetTypes.GENERIC_WOOD, of(Material.WOOD).usesTool(AXE_MINEABLE).renderLayer(CUTOUT).dropsSelf().strength(3).sounds(BlockSoundGroup.WOOD).nonOpaque().allowsSpawning(Blocks::never));
	public static final Block GREATWOOD_SIGN = new SignBlock(ArcanaBlockSetTypes.GREATWOOD, of(Material.WOOD).usesTool(AXE_MINEABLE).renderLayer(CUTOUT).strength(3).sounds(BlockSoundGroup.WOOD).nonOpaque());
	public static final Block GREATWOOD_WALL_SIGN = new WallSignBlock(ArcanaBlockSetTypes.GREATWOOD, of(Material.WOOD).usesTool(AXE_MINEABLE).renderLayer(CUTOUT).dropsLike(GREATWOOD_SIGN).strength(3).sounds(BlockSoundGroup.WOOD).nonOpaque());
	
	public static final Block GLEAMING_GREATWOOD_PLANKS = new Block(of(Material.WOOD).dropsSelf().strength(2, 3).sounds(BlockSoundGroup.WOOD));
	public static final Block SOLAR_GLEAMING_GREATWOOD_PLANKS = new Block(of(Material.WOOD).dropsSelf().strength(2, 3).sounds(BlockSoundGroup.WOOD));
	
	public static final Block BALANCED_CRYSTAL = new Block(of(Material.AMETHYST, MapColor.WHITE).dropsSelf().usesTool(PICKAXE_MINEABLE).sounds(BlockSoundGroup.AMETHYST_CLUSTER).strength(0.9f).luminance(3));
	public static final Block BALANCED_CRYSTAL_PILLAR = new CrystalPillarBlock(of(Material.AMETHYST, MapColor.WHITE).usesTool(PICKAXE_MINEABLE).sounds(BlockSoundGroup.AMETHYST_CLUSTER).strength(0.9f).luminance(3));
	
	public static final Block TAINTWOOD_LOG = new PillarBlock(of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).group(Tab.TAINTED).strength(1.2f).sounds(BlockSoundGroup.FUNGUS));
	public static final Block TAINTWOOD_PLANKS = new Block(of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).group(Tab.TAINTED).strength(1.2f).sounds(BlockSoundGroup.FUNGUS));
	public static final Block TAINTWOOD_WOOD = new PillarBlock(of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).group(Tab.TAINTED).strength(1.2f).sounds(BlockSoundGroup.FUNGUS));
	
	public static final Block TAINTWOOD_DOOR = new DoorBlock(ArcanaBlockSetTypes.GENERIC_WOOD, of(Material.WOOD).usesTool(AXE_MINEABLE).group(Tab.TAINTED).renderLayer(CUTOUT).strength(1.6f).sounds(BlockSoundGroup.FUNGUS).nonOpaque());
	public static final Block TAINTWOOD_TRAPDOOR = new TrapdoorBlock(ArcanaBlockSetTypes.GENERIC_WOOD, of(Material.WOOD).usesTool(AXE_MINEABLE).group(Tab.TAINTED).renderLayer(CUTOUT).dropsSelf().strength(1.6f).sounds(BlockSoundGroup.FUNGUS).nonOpaque().allowsSpawning(Blocks::never));
	public static final Block TAINTWOOD_SLAB = new SlabBlock(of(Material.WOOD).usesTool(AXE_MINEABLE).group(Tab.TAINTED).strength(1.2f).sounds(BlockSoundGroup.FUNGUS));
	public static final Block TAINTWOOD_STAIRS = new StairsBlock(TAINTWOOD_PLANKS.getDefaultState(), of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).group(Tab.TAINTED).strength(2).sounds(BlockSoundGroup.FUNGUS));
	public static final Block TAINTWOOD_FENCE = new FenceBlock(of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).group(Tab.TAINTED).strength(1.2f).sounds(BlockSoundGroup.FUNGUS));
	public static final Block TAINTWOOD_FENCE_GATE = new FenceGateBlock(ArcanaBlockSetTypes.TAINTWOOD, of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).group(Tab.TAINTED).strength(1.2f).sounds(BlockSoundGroup.FUNGUS));
	public static final Block TAINTWOOD_PRESSURE_PLATE = new PressurePlateBlock(ArcanaBlockSetTypes.GENERIC_WOOD, of(Material.WOOD).dropsSelf().group(Tab.TAINTED).usesTool(AXE_MINEABLE).strength(.5f).sounds(BlockSoundGroup.FUNGUS));
	public static final Block TAINTWOOD_BUTTON = new ButtonBlock(ArcanaBlockSetTypes.GENERIC_WOOD, 30, of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).group(Tab.TAINTED).strength(.5f).noCollision().sounds(BlockSoundGroup.FUNGUS));
	
	public static final Block HOLLOWED_LOG = new PillarBlock(of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).group(Tab.TAINTED).strength(1.2f).sounds(BlockSoundGroup.CORAL));
	public static final Block HOLLOWED_PLANKS = new Block(of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).group(Tab.TAINTED).strength(1.2f, 3).sounds(BlockSoundGroup.CORAL));
	public static final Block HOLLOWED_WOOD = new PillarBlock(of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).group(Tab.TAINTED).strength(1.2f).sounds(BlockSoundGroup.CORAL));
	
	public static final Block HOLLOWED_DOOR = new DoorBlock(ArcanaBlockSetTypes.GENERIC_WOOD, of(Material.WOOD).usesTool(AXE_MINEABLE).group(Tab.TAINTED).renderLayer(CUTOUT).strength(1.6f).sounds(BlockSoundGroup.CORAL).nonOpaque());
	public static final Block HOLLOWED_TRAPDOOR = new TrapdoorBlock(ArcanaBlockSetTypes.GENERIC_WOOD, of(Material.WOOD).usesTool(AXE_MINEABLE).group(Tab.TAINTED).renderLayer(CUTOUT).dropsSelf().strength(1.6f).sounds(BlockSoundGroup.CORAL).nonOpaque().allowsSpawning(Blocks::never));
	public static final Block HOLLOWED_SLAB = new SlabBlock(of(Material.WOOD).usesTool(AXE_MINEABLE).group(Tab.TAINTED).strength(1.2f).sounds(BlockSoundGroup.CORAL));
	public static final Block HOLLOWED_STAIRS = new StairsBlock(HOLLOWED_PLANKS.getDefaultState(), of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).group(Tab.TAINTED).strength(2).sounds(BlockSoundGroup.CORAL));
	public static final Block HOLLOWED_FENCE = new FenceBlock(of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).group(Tab.TAINTED).strength(1.2f).sounds(BlockSoundGroup.CORAL));
	public static final Block HOLLOWED_FENCE_GATE = new FenceGateBlock(ArcanaBlockSetTypes.HOLLOWED, of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).group(Tab.TAINTED).strength(1.2f).sounds(BlockSoundGroup.CORAL));
	public static final Block HOLLOWED_PRESSURE_PLATE = new PressurePlateBlock(ArcanaBlockSetTypes.GENERIC_WOOD, of(Material.WOOD).dropsSelf().group(Tab.TAINTED).usesTool(AXE_MINEABLE).strength(.5f).sounds(BlockSoundGroup.CORAL));
	public static final Block HOLLOWED_BUTTON = new ButtonBlock(ArcanaBlockSetTypes.GENERIC_WOOD, 30, of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).group(Tab.TAINTED).strength(.5f).noCollision().sounds(BlockSoundGroup.CORAL));
	
	public static final Block VISHROOM = new SizedPlantBlock(of(Material.PLANT).dropsSelf().renderLayer(CUTOUT).sounds(BlockSoundGroup.GRASS).noCollision().breakInstantly().offset(AbstractBlock.OffsetType.XZ), 14, 14);
	public static final Block CORDISPORA = new SizedPlantBlock(of(Material.PLANT).dropsSelf().renderLayer(CUTOUT).sounds(BlockSoundGroup.GRASS).noCollision().breakInstantly().offset(AbstractBlock.OffsetType.XZ), 6, 6);
	public static final Block SNOWDROP = new SizedPlantBlock(of(Material.PLANT).dropsSelf().renderLayer(CUTOUT).sounds(BlockSoundGroup.GRASS).noCollision().breakInstantly().offset(AbstractBlock.OffsetType.XZ), 13, 14);
	public static final Block FIREWHEEL = new SizedPlantBlock(of(Material.PLANT).dropsSelf().renderLayer(CUTOUT).sounds(BlockSoundGroup.GRASS).noCollision().breakInstantly().offset(AbstractBlock.OffsetType.XZ), 10, 15);
	public static final Block LILIUM = new SizedPlantBlock(of(Material.PLANT).dropsSelf().renderLayer(CUTOUT).sounds(BlockSoundGroup.GRASS).noCollision().breakInstantly().offset(AbstractBlock.OffsetType.XZ), 6, 15);
	
	public static final Block HUGE_VISHROOM_STEM = new MushroomBlock(of(Material.WOOD, MapColor.WHITE_GRAY).strength(0.2F).sounds(BlockSoundGroup.WOOD));
	public static final Block HUGE_VISHROOM_CAP = new MushroomBlock(of(Material.WOOD, MapColor.GREEN).strength(0.2F).sounds(BlockSoundGroup.WOOD));
	public static final Block HUGE_CORDISPORA_STEM = new MushroomBlock(of(Material.WOOD, MapColor.WHITE_GRAY).strength(0.2F).sounds(BlockSoundGroup.WOOD));
	public static final Block HUGE_CORDISPORA_CAP = new MushroomBlock(of(Material.WOOD, MapColor.PINK).strength(0.2F).sounds(BlockSoundGroup.WOOD));
	
	public static final Block BEJEWELED_BEETS_BLOCK = new BejeweledBeetsBlock(of(Material.PLANT).renderLayer(CUTOUT).nonOpaque().noCollision().ticksRandomly().breakInstantly().sounds(BlockSoundGroup.CROP));
	public static final Item BEJEWELED_BEET_SEEDS = new AliasedBlockItem(BEJEWELED_BEETS_BLOCK, GROUPED_RES);
	
	public static final Block VOID_GROWTH = new Block(of(Material.PLANT).renderLayer(CUTOUT).nonOpaque().noCollision().breakInstantly().sounds(BlockSoundGroup.FROGSPAWN));
	
	public static final WoodenStatueBlock SPEAK_NO_EVIL_STATUE = new WoodenStatueBlock(of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).strength(2).sounds(BlockSoundGroup.WOOD).nonOpaque(), WoodenStatueBlock.Type.SPEAK);
	public static final WoodenStatueBlock SEE_NO_EVIL_STATUE = new WoodenStatueBlock(of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).strength(2).sounds(BlockSoundGroup.WOOD).nonOpaque(), WoodenStatueBlock.Type.SEE);
	public static final WoodenStatueBlock HEAR_NO_EVIL_STATUE = new WoodenStatueBlock(of(Material.WOOD).dropsSelf().usesTool(AXE_MINEABLE).strength(2).sounds(BlockSoundGroup.WOOD).nonOpaque(), WoodenStatueBlock.Type.HEAR);
	public static final StoneVaseBlock STONE_VASE = new StoneVaseBlock(of(Material.STONE).dropsSelf().usesTool(PICKAXE_MINEABLE).strength(2).nonOpaque());
	
	public static final Block CRIMSON_LANTERN = new CrimsonLanternBlock(of(Material.METAL).renderLayer(CUTOUT).dropsSelf().requiresTool(PICKAXE_MINEABLE).strength(3.5f).sounds(BlockSoundGroup.LANTERN).luminance(__ -> 12).nonOpaque());
	public static final Block CHAIN_WALL = new PaneBlock(of(Material.METAL, MapColor.CLEAR).renderLayer(CUTOUT).dropsSelf().requiresTool(PICKAXE_MINEABLE).strength(2.5f).sounds(BlockSoundGroup.METAL).nonOpaque());
	public static final Block METAL_LADDER = new LadderBlock(of(Material.WOOD).renderLayer(CUTOUT).dropsSelf().requiresTool(PICKAXE_MINEABLE).strength(2.5f).sounds(BlockSoundGroup.LADDER).nonOpaque());
	
	public static final Block GLEAMING_LAMPLIGHT = new Block(of(Material.GLASS).dropsSelf().usesTool(PICKAXE_MINEABLE).strength(2, 7).luminance(15));
	public static final Block CHISELED_GLEAMING_LAMPLIGHT = new Block(of(Material.GLASS).dropsSelf().usesTool(PICKAXE_MINEABLE).strength(2, 7).luminance(15));
	
	public static final Block TEMPORARY_LIGHT_BLOCK = new TemporaryLightBlock(of(Material.DECORATION).dropsNothing().breakInstantly().ticksRandomly().luminance(state -> 7 + state.get(TemporaryLightBlock.LIFE)));
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
	public static final Block TAINTED_ROCK = new Block(of(Material.STONE, MapColor.PURPLE).group(Tab.TAINTED).dropsSelf().requiresTool(PICKAXE_MINEABLE).strength(1.6f, 6));
	public static final Block TAINTED_ANDESITE = new Block(of(Material.STONE, MapColor.PURPLE).group(Tab.TAINTED).dropsSelf().requiresTool(PICKAXE_MINEABLE).strength(1.6f, 6));
	public static final Block TAINTED_DIORITE = new Block(of(Material.STONE, MapColor.PURPLE).group(Tab.TAINTED).dropsSelf().requiresTool(PICKAXE_MINEABLE).strength(1.6f, 6));
	public static final Block TAINTED_GRANITE = new Block(of(Material.STONE, MapColor.PURPLE).group(Tab.TAINTED).dropsSelf().requiresTool(PICKAXE_MINEABLE).strength(1.6f, 6));
	
	// TODO: falling block colours
	public static final Block TAINTED_SOIL = new Block(of(Material.SOIL, MapColor.PURPLE).group(Tab.TAINTED).dropsSelf().usesTool(SHOVEL_MINEABLE).strength(0.5f).sounds(BlockSoundGroup.GRAVEL));
	public static final Block TAINTED_GRASS_BLOCK = new SnowyBlock(of(Material.SOLID_ORGANIC, MapColor.PURPLE).group(Tab.TAINTED).usesTool(SHOVEL_MINEABLE).strength(0.6f).sounds(BlockSoundGroup.GRASS));
	public static final Block TAINTED_SAND = new ColoredFallingBlock(new ColorCode(0), of(Material.AGGREGATE, MapColor.PURPLE).group(Tab.TAINTED).usesTool(SHOVEL_MINEABLE).strength(0.5f).sounds(BlockSoundGroup.SAND));
	public static final Block TAINTED_SANDSTONE = new Block(of(Material.STONE, MapColor.PURPLE).group(Tab.TAINTED).dropsSelf().requiresTool(PICKAXE_MINEABLE).strength(0.8f));
	public static final Block TAINTED_GRAVEL = new ColoredFallingBlock(new ColorCode(0), of(Material.AGGREGATE, MapColor.PURPLE).group(Tab.TAINTED).usesTool(SHOVEL_MINEABLE).strength(0.7f).sounds(BlockSoundGroup.GRAVEL));
	public static final Block TAINTED_SNOW_BLOCK = new ColoredFallingBlock(new ColorCode(0), of(Material.SNOW_BLOCK, MapColor.PURPLE).group(Tab.TAINTED).requiresTool(SHOVEL_MINEABLE).strength(0.2f).sounds(BlockSoundGroup.SNOW));
	
	public static final Block TAINTED_HOLLOWED_ORE = new Block(of(Material.STONE, MapColor.PURPLE).group(Tab.TAINTED).requiresTool(PICKAXE_MINEABLE).strength(1.8f, 6));
	
	// unique tainted blocks
	public static final Block TAINT_CRUST = new Block(of(Material.SOLID_ORGANIC, MapColor.PURPLE).group(Tab.TAINTED).requiresTool(HOE_MINEABLE).strength(0.7f).sounds(BlockSoundGroup.SLIME));
	
	// dead/damaged/untainted blocks
	public static final Block HOLLOWED_ORE = new Block(of(Material.STONE).group(Tab.TAINTED).dropsSelf().requiresTool(PICKAXE_MINEABLE).strength(1.8f));
	
	// points of interest...
	// created in register
	public static PointOfInterestType WARDED_CAMPFIRE_POI;
	
	// screen handlers...
	public static final ScreenHandlerType<ArcaneCraftingScreen.Handler> ARCANE_CRAFTING_SCREEN_HANDLER = new ScreenHandlerType<>(ArcaneCraftingScreen.Handler::new, FeatureSet.empty());
	public static final ScreenHandlerType<ResearchTableScreen.Handler> RESEARCH_TABLE_SCREEN_HANDLER = new ScreenHandlerType<>(ResearchTableScreen.Handler::new, FeatureSet.empty());
	public static final ScreenHandlerType<KnowledgeableDropperScreen.Handler> KNOWLEDGEABLE_DROPPER_SCREEN_HANDLER = new ScreenHandlerType<>(KnowledgeableDropperScreen.Handler::new, FeatureSet.empty());
	public static final ScreenHandlerType<ArcaneFurnaceScreen.Handler> ARCANE_FURNACE_SCREEN_HANDLER = new ScreenHandlerType<>(ArcaneFurnaceScreen.Handler::new, FeatureSet.empty());
	public static final ScreenHandlerType<DistilleryPathfinderScreen.Handler> DISTILLERY_PATHFINDER_SCREEN_HANDLER = new ScreenHandlerType<>(DistilleryPathfinderScreen.Handler::new, FeatureSet.empty());
	public static final ScreenHandlerType<CrystallizationPressScreen.Handler> CRYSTALLIZATION_PRESS_SCREEN_HANDLER = new ScreenHandlerType<>(CrystallizationPressScreen.Handler::new, FeatureSet.empty());
	public static final ScreenHandlerType<FocusPouchScreen.Handler> FOCUS_POUCH_SCREEN_HANDLER = new ScreenHandlerType<>(FocusPouchScreen.Handler::new, FeatureSet.empty());
	
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
	public static BlockEntityType<MagicMirrorBlockEntity> MAGIC_MIRROR_BE = FabricBlockEntityTypeBuilder.create(MagicMirrorBlockEntity::new, MAGIC_MIRROR).build();
	
	// structures
	/*public static final RegistryEntry<StructurePool> CRIMSON_OUTPOST_STRUCTURE_POOL = StructurePools.register(
			new StructurePool(
					arcId("crimson_outpost"),
					Identifier.of("empty"),
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
	);*/
	
	public static final StructurePlacement CRIMSON_OUTPOST_PLACEMENT = new RandomSpreadStructurePlacement(48, 12, SpreadType.LINEAR, 1256);
	
	/*public static final RegistryEntry<StructurePool> CRIMSON_CAMP_STRUCTURE_POOL = StructurePools.register(
			new StructurePool(
					arcId("crimson_camp"),
					Identifier.of("empty"),
					ImmutableList.of(Pair.of(StructurePoolElement.ofSingle("arcana:crimson_camp"), 1)),
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
	);*/
	
	public static final StructurePlacement CRIMSON_CAMP_PLACEMENT = new RandomSpreadStructurePlacement(38, 12, SpreadType.LINEAR, 1356);
	
	/*public static final RegistryEntry<StructurePool> FLORAL_ARCHIVE_STRUCTURE_POOL = StructurePools.register(
			new StructurePool(
					arcId("floral_archive"),
					Identifier.of("empty"),
					ImmutableList.of(Pair.of(StructurePoolElement.ofSingle("arcana:floral_archive"), 1)),
					StructurePool.Projection.RIGID
			)
	);
	
	public static final Structure FLORAL_ARCHIVE = new JigsawStructure(
			createStructureConfig(
					ConventionalBiomeTags.CAVES,
					Map.of(),
					GenerationStep.Feature.UNDERGROUND_STRUCTURES,
					StructureTerrainAdaptation.BURY
			),
			FLORAL_ARCHIVE_STRUCTURE_POOL,
			1,
			UniformHeightProvider.create(YOffset.aboveBottom(10), YOffset.aboveBottom(60)),
			false
	);*/
	
	public static final StructurePlacement FLORAL_ARCHIVE_PLACEMENT = new RandomSpreadStructurePlacement(18, 4, SpreadType.TRIANGULAR, 856294);
	
	// particle types...
	public static SimpleParticleType TAINT_BUBBLE = FabricParticleTypes.simple();
	public static SimpleParticleType FLAME = FabricParticleTypes.simple();
	public static SimpleParticleType LIGHTNING = FabricParticleTypes.simple();
	public static SimpleParticleType TAINT_SPORE = FabricParticleTypes.simple();
	
	public static ParticleType<CubeParticleEffect> WARDING_EFFECT = FabricParticleTypes.complex(CubeParticleEffect::createCodec, CubeParticleEffect::createPacketCodec);
	public static ParticleType<CubeParticleEffect> INFESTED_EFFECT = FabricParticleTypes.complex(CubeParticleEffect::createCodec, CubeParticleEffect::createPacketCodec);
	
	public static ParticleType<BlockStateParticleEffect> HUNGRY_NODE_DISC = FabricParticleTypes.complex(BlockStateParticleEffect::createCodec, BlockStateParticleEffect::createPacketCodec);
	public static ParticleType<BlockStateParticleEffect> HUNGRY_NODE_BLOCK = FabricParticleTypes.complex(BlockStateParticleEffect::createCodec, BlockStateParticleEffect::createPacketCodec);
	public static ParticleType<ItemStackParticleEffect> INFUSION_ITEM = FabricParticleTypes.complex(ItemStackParticleEffect::createCodec, ItemStackParticleEffect::createPacketCodec);
	public static ParticleType<AspectParticleEffect> ESSENTIA_STREAM = FabricParticleTypes.complex(AspectParticleEffect::createCodec, AspectParticleEffect::createPacketCodec);
	
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
			.dimensions(EntityDimensions.fixed(1, 1.8f).withEyeHeight(1.74f))
			.build();
	public static final EntityType<CrimsonArcherEntity> CRIMSON_ARCHER = FabricEntityTypeBuilder
			.createLiving()
			.entityFactory(CrimsonArcherEntity::new)
			.spawnGroup(SpawnGroup.MONSTER)
			.defaultAttributes(CrimsonArcherEntity::createArcherAttributes)
			.dimensions(EntityDimensions.fixed(1, 1.8f).withEyeHeight(1.74f))
			.build();
	public static final EntityType<CrimsonProtectorEntity> CRIMSON_PROTECTOR = FabricEntityTypeBuilder
			.createLiving()
			.entityFactory(CrimsonProtectorEntity::new)
			.spawnGroup(SpawnGroup.MONSTER)
			.defaultAttributes(CrimsonProtectorEntity::createProtectorAttributes)
			.dimensions(EntityDimensions.fixed(1, 1.8f).withEyeHeight(1.74f))
			.build();
	public static final EntityType<CrimsonMissionaryEntity> CRIMSON_MISSIONARY = FabricEntityTypeBuilder
			.createLiving()
			.entityFactory(CrimsonMissionaryEntity::new)
			.spawnGroup(SpawnGroup.MONSTER)
			.defaultAttributes(CrimsonMissionaryEntity::createMissionaryAttributes)
			.dimensions(EntityDimensions.fixed(1, 1.8f).withEyeHeight(1.74f))
			.build();
	public static final EntityType<CrimsonHeavyKnightEntity> CRIMSON_HEAVY_KNIGHT = FabricEntityTypeBuilder
			.createLiving()
			.entityFactory(CrimsonHeavyKnightEntity::new)
			.spawnGroup(SpawnGroup.MONSTER)
			.defaultAttributes(CrimsonHeavyKnightEntity::createHeavyKnightAttributes)
			.dimensions(EntityDimensions.fixed(1, 1.8f).withEyeHeight(1.74f))
			.build();
	public static final EntityType<CrimsonJesterEntity> CRIMSON_JESTER = FabricEntityTypeBuilder
			.createLiving()
			.entityFactory(CrimsonJesterEntity::new)
			.spawnGroup(SpawnGroup.MONSTER)
			.defaultAttributes(CrimsonJesterEntity::createJesterAttributes)
			.dimensions(EntityDimensions.fixed(1, 1.8f).withEyeHeight(1.74f))
			.build();
	
	// and finally, the item group
	
	public static final ItemGroup MAIN_GROUP = FabricItemGroup.builder()
			.icon(() -> new ItemStack(ARCANUM))
			.entries((ctx, entries) -> {
				/*for(var items : ITEMS_BY_TAB.values())
					for(Item item : items)
						if(item instanceof CustomCreativePresentationItem presentation)
							presentation.addToTab(entries);
						else
							entries.add(item, ItemGroup.StackVisibility.PARENT_AND_SEARCH_TABS);*/
				for(ItemSubGroup subGroup : smuggleTab().fractal$getChildren())
					entries.addAll(subGroup.getSearchTabStacks(), ItemGroup.StackVisibility.PARENT_AND_SEARCH_TABS);
			})
			.displayName(Text.translatable("item_group.arcana.arcana"))
			.noRenderedName()
			.build();
	
	public static final List<Item> ITEMS = new ArrayList<>();
	public static final List<Block> BLOCKS = new ArrayList<>();
	public static final List<ArcanaFluid> STILL_FLUIDS = new ArrayList<>();
	
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
		
		register("personal_magic_mirror", PERSONAL_MAGIC_MIRROR);
		
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
		
		register("gold_ring", GOLD_RING);
		register("arcanium_ring", ARCANIUM_RING);
		register("adorned_ring", ADORNED_RING);
		register("lamplight_ring", LAMPLIGHT_RING);
		register("plane_projection_ring", PLANE_PROJECTION_RING);
		register("ring_of_the_surging_barrier", RING_OF_THE_SURGING_BARRIER);
		register("ring_of_twin_heartbeats", RING_OF_TWIN_HEARTBEATS);
		register("ring_of_the_voidgazer", RING_OF_THE_VOIDGAZER);
		register("emerald_necklace", EMERALD_NECKLACE);
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
		ArcaneFurnaceBlock.SUBSTRATE_TIMES.put(SYNTHETIC_SCAFFOLDING, new ArcaneFurnaceBlock.SubstrateData(15, 0x43FC48));
		ArcaneFurnaceBlock.SUBSTRATE_TIMES.put(FORMLESS_FOAM, new ArcaneFurnaceBlock.SubstrateData(40, 0x2FD8C2));
		
		register("void_putty", VOID_PUTTY);
		ArcaneFurnaceBlock.SUBSTRATE_TIMES.put(VOID_PUTTY, new ArcaneFurnaceBlock.SubstrateData(200, 0x852797));
		
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
		
		// TODO: banner pattern data
		//Registry.register(Registries.BANNER_PATTERN, arcId("eldritch"), ELDRITCH_BANNER_PATTERN_SHAPE);
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
		register("infestation_injector", INFESTATION_INJECTOR);
		register("infestation_eraser", INFESTATION_ERASER);
		
		for(Aspect aspect : Aspects.getOrderedAspects()){
			var shortName = aspect.id().getPath();
			CrystalItem crystalItem = new CrystalItem(new ArcanaItemSettings().group(Tab.CRYSTALS), aspect);
			register("crystals/" + shortName, crystalItem);
			Aspects.crystals.put(aspect, crystalItem);
			
			PhialItem phialItem = new PhialItem(new ArcanaItemSettings().fragile(aspect.colour()).group(Tab.PHIALS), aspect);
			register("phials/" + shortName, phialItem);
			Aspects.phials.put(aspect, phialItem);
			
			if(Aspects.primals.contains(aspect))
				ArcaneFurnaceBlock.SUBSTRATE_TIMES.put(crystalItem, new ArcaneFurnaceBlock.SubstrateData(5, aspect.colour()));
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
		register("greatwood_scribing_desk", GREATWOOD_SCRIBING_DESK);
		register("silverwood_scribing_desk", SILVERWOOD_SCRIBING_DESK);
		register("arcane_levitator", ARCANE_LEVITATOR);
		register("thaumic_halo", THAUMIC_HALO);
		register("crystallization_press", CRYSTALLIZATION_PRESS);
		register("mystic_mist", MYSTIC_MIST);
		register("magic_mirror", MAGIC_MIRROR, false);
		register("magic_mirror", new MagicMirrorBlockItem(MAGIC_MIRROR, new ArcanaItemSettings().group(Tab.MAIN).maxCount(2)));
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
		register("silverwood_sign", new SignItem(new ArcanaItemSettings().group(Tab.MAIN).maxCount(16), SILVERWOOD_SIGN, SILVERWOOD_WALL_SIGN));
		
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
		register("greatwood_sign", new SignItem(new ArcanaItemSettings().group(Tab.MAIN).maxCount(16), GREATWOOD_SIGN, GREATWOOD_WALL_SIGN));
		
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
		
		for(Block block : Set.of(SILVERWOOD_SIGN, SILVERWOOD_WALL_SIGN, GREATWOOD_SIGN, GREATWOOD_WALL_SIGN))
			BlockEntityType.SIGN.addSupportedBlock(block);
		// these have their own BEs, but *briefly* have the vanilla BE during initialisation
		for(Block block : Set.of(WARDED_CAMPFIRE, CRIMSON_CAMPFIRE))
			BlockEntityType.CAMPFIRE.addSupportedBlock(block);
		
		register("vishroom", VISHROOM);
		register("cordispora", CORDISPORA);
		register("snowdrop", SNOWDROP);
		register("firewheel", FIREWHEEL);
		register("lilium", LILIUM);
		
		register("huge_vishroom_stem", HUGE_VISHROOM_STEM);
		register("huge_vishroom_cap", HUGE_VISHROOM_CAP);
		register("huge_cordispora_stem", HUGE_CORDISPORA_STEM);
		register("huge_cordispora_cap", HUGE_CORDISPORA_CAP);
		
		register("bejeweled_beets", BEJEWELED_BEETS_BLOCK, false);
		register("void_growth", VOID_GROWTH, false);
		
		register("speak_no_evil_statue", SPEAK_NO_EVIL_STATUE);
		register("see_no_evil_statue", SEE_NO_EVIL_STATUE);
		register("hear_no_evil_statue", HEAR_NO_EVIL_STATUE);
		register("stone_vase", STONE_VASE);
		
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
		
		register("light_block", TEMPORARY_LIGHT_BLOCK, false);
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
		register("magic_mirror", MAGIC_MIRROR_BE);
		
		// features
		register("hanging_node", new HangingNodeFeature());
		register("surface_node", new SurfaceNodeFeature());
		register("nodal_geode", new NodalGeodeFeature());
		register("structure_mushroom", new StructureMushroomFeature());
		
		register("silverwood_foliage", SilverwoodFoliagePlacer.TYPE);
		register("silverwood_trunk", SilverwoodTrunkPlacer.TYPE);
		register("greatwood_foliage", GreatwoodFoliagePlacer.TYPE);
		register("greatwood_trunk", GreatwoodTrunkPlacer.TYPE);
		
		// structures
		/*register("crimson_outpost", CRIMSON_OUTPOST, CRIMSON_OUTPOST_PLACEMENT);
		register("crimson_camp", CRIMSON_CAMP, CRIMSON_CAMP_PLACEMENT);
		register("floral_archive", FLORAL_ARCHIVE, FLORAL_ARCHIVE_PLACEMENT);*/
		
		// particle types
		register("taint_bubble", TAINT_BUBBLE);
		register("flame", FLAME);
		register("lightning", LIGHTNING);
		register("taint_spore", TAINT_SPORE);
		
		register("warding_effect", WARDING_EFFECT);
		register("infested_effect", INFESTED_EFFECT);
		
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
		
		// loot pool types
		Registry.register(Registries.LOOT_POOL_ENTRY_TYPE, arcId("tag_gift"), TagGiftLootEntry.TYPE);
		Registry.register(Registries.LOOT_CONDITION_TYPE, arcId("random_chance_once"), RandomChanceOnceLootCondition.TYPE);
		
		// and finally, the creative tab
		Registry.register(Registries.ITEM_GROUP, arcId("main"), MAIN_GROUP);
		for(Tab t : Tab.values())
			createSubTab(t);
	}
	
	private static void register(String name, Item item){
		Registry.register(Registries.ITEM, arcId(name), item);
		ITEMS.add(item);
		if(item instanceof Cap c)
			registerCapOnly(c);
		if(item instanceof Core c)
			registerCoreOnly(c);
		
		Tab tab = item.getComponents().getOrDefault(ArcanaItemComponentTypes.SUBTAB, Tab.MAIN);
		ITEMS_BY_TAB.compute(tab, (__, b) -> {
			if(b == null)
				b = new ArrayList<>();
			b.add(item);
			return b;
		});
	}
	
	private static void register(String name, Block block){
		register(name, block, true);
	}
	
	private static void register(String name, Block block, boolean andItem){
		Registry.register(Registries.BLOCK, arcId(name), block);
		BLOCKS.add(block);
		if(andItem){
			ArcanaItemSettings settings = new ArcanaItemSettings().group(Tab.MAIN);
			if(block.getSettings() instanceof ArcanaBlockSettings abs && abs.getGroup() != null)
				settings.group(abs.getGroup());
			register(name, block instanceof BigBlock bb ? new BigBlockItem(bb, settings) : new BlockItem(block, settings));
		}
	}
	
	private static void register(String name, Fluid fluid){
		Registry.register(Registries.FLUID, arcId(name), fluid);
		if(fluid instanceof ArcanaFluid af && af.isStill())
			STILL_FLUIDS.add(af);
	}
	
	private static void register(String name, ScreenHandlerType<?> type){
		Registry.register(Registries.SCREEN_HANDLER, arcId(name), type);
	}
	
	private static void register(String name, BlockEntityType<?> type){
		Registry.register(Registries.BLOCK_ENTITY_TYPE, arcId(name), type);
	}
	
	private static void register(String name, Feature<?> feature){
		Registry.register(Registries.FEATURE, arcId(name), feature);
	}
	
	private static void register(String name, FoliagePlacerType<?> foliagePlacer){
		Registry.register(Registries.FOLIAGE_PLACER_TYPE, arcId(name), foliagePlacer);
	}
	
	private static void register(String name, TrunkPlacerType<?> trunkPlacer){
		Registry.register(Registries.TRUNK_PLACER_TYPE, arcId(name), trunkPlacer);
	}
	
	private static void register(String name, ParticleType<?> particleType){
		Registry.register(Registries.PARTICLE_TYPE, arcId(name), particleType);
	}
	
	private static void register(String name, EntityType<?> entityType){
		Registry.register(Registries.ENTITY_TYPE, arcId(name), entityType);
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
	
	private static FoodComponent aspectCandyFood(RegistryEntry<StatusEffect> effect){
		return new FoodComponent.Builder()
				.nutrition(3)
				.saturationModifier(0.5f)
				.alwaysEdible()
				.snack()
				.statusEffect(new StatusEffectInstance(effect, 135 * 20, 0, true, true), 1)
				.build();
	}
	
	private static ItemGroup smuggleTab(){
		return MAIN_GROUP;
	}
	
	private static ItemSubGroup createSubTab(Tab tab){
		String lowercase = tab.name().toLowerCase(Locale.ROOT);
		return new ItemSubGroup.Builder(MAIN_GROUP, arcId(lowercase), Text.translatable("item_group.arcana." + lowercase)).entries((ctx, entries) -> {
			for(Item item : ITEMS_BY_TAB.get(tab)){
				if(item instanceof CustomCreativePresentationItem presentation)
					presentation.addToTab(entries);
				else
					entries.add(item.getDefaultStack());
			}
		}).build();
	}
}