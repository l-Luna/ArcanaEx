package arcana.datagen;

import arcana.aspects.Aspects;
import arcana.blocks.CrystalClusterBlock;
import arcana.items.ScalpelItem;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.block.Block;
import net.minecraft.data.client.*;
import net.minecraft.data.family.BlockFamily;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.ToolItem;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

import static arcana.ArcanaRegistry.*;

public final class ArcanaModelProvider extends FabricModelProvider{
	
	private final List<Item> noAutoGen = new ArrayList<>();
	
	public ArcanaModelProvider(FabricDataOutput gen){
		super(gen);
	}
	
	public void generateBlockStateModels(BlockStateModelGenerator blockGen){
		blockGen.registerSimpleState(ARCANE_CRAFTING_TABLE);
		blockGen.registerSimpleState(NITOR);
		blockGen.registerSimpleState(PEDESTAL);
		blockGen.registerSimpleState(ARCANE_LEVITATOR);
		blockGen.registerSimpleState(ALEMBIC);
		blockGen.registerSimpleState(INFUSION_MATRIX);
		
		blockGen.registerSimpleCubeAll(ARCANIUM_BLOCK);
		blockGen.registerSimpleCubeAll(THAUMIUM_BLOCK);
		blockGen.registerSimpleCubeAll(VOID_METAL_BLOCK);
		blockGen.registerSimpleCubeAll(SILVERLEAF_AMALGAMATE_BLOCK);
		blockGen.registerSimpleCubeAll(SILVERWOOD_LEAVES);
		blockGen.registerSingleton(GREATWOOD_LEAVES, TexturedModel.LEAVES);
		blockGen.registerSimpleCubeAll(HARDENED_GLASS);
		blockGen.registerSimpleCubeAll(LUMINIFEROUS_GLASS);
		blockGen.registerSimpleCubeAll(STATIC_GLASS);
		blockGen.registerSimpleCubeAll(PAVING_STONE_OF_TRAVEL);
		blockGen.registerSimpleCubeAll(PAVING_STONE_OF_WARDING);
		blockGen.registerSimpleCubeAll(TAINTED_ROCK);
		blockGen.registerSimpleCubeAll(TAINTED_ANDESITE);
		blockGen.registerSimpleCubeAll(TAINTED_GRANITE);
		blockGen.registerSimpleCubeAll(TAINTED_DIORITE);
		blockGen.registerSimpleCubeAll(TAINTED_SOIL);
		blockGen.registerSimpleCubeAll(TAINTED_SAND);
		blockGen.registerSimpleCubeAll(TAINTED_SANDSTONE);
		blockGen.registerSimpleCubeAll(TAINTED_GRAVEL);
		blockGen.registerSimpleCubeAll(TAINTED_SNOW_BLOCK);
		blockGen.registerSimpleCubeAll(TAINTED_HOLLOWED_ORE);
		blockGen.registerSimpleCubeAll(TAINT_CRUST);
		blockGen.registerSimpleCubeAll(HOLLOWED_ORE);
		blockGen.registerSimpleCubeAll(ARCANE_STONE_INSCRIBED_TILES);
		blockGen.registerSimpleCubeAll(GLEAMING_LAMPLIGHT);
		blockGen.registerSimpleCubeAll(CHISELED_GLEAMING_LAMPLIGHT);
		blockGen.registerSimpleCubeAll(GLEAMING_SILVERWOOD_PLANKS);
		blockGen.registerSimpleCubeAll(SOLAR_GLEAMING_SILVERWOOD_PLANKS);
		blockGen.registerSimpleCubeAll(GLEAMING_GREATWOOD_PLANKS);
		blockGen.registerSimpleCubeAll(SOLAR_GLEAMING_GREATWOOD_PLANKS);
		
		// TODO: cleanup; these are mostly no-ops now
		registerCross(SILVERWOOD_SAPLING, blockGen);
		registerCross(GREATWOOD_SAPLING, blockGen);
		registerCross(VISHROOM, blockGen);
		registerCross(CORDISPORA, blockGen);
		registerCross(SNOWDROP, blockGen);
		registerCross(FIREWHEEL, blockGen);
		registerCross(LILIUM, blockGen);
		
		blockGen.registerFlowerPotPlant(SILVERWOOD_SAPLING, POTTED_SILVERWOOD_SAPLING, BlockStateModelGenerator.TintType.NOT_TINTED);
		blockGen.registerFlowerPotPlant(GREATWOOD_SAPLING, POTTED_GREATWOOD_SAPLING, BlockStateModelGenerator.TintType.NOT_TINTED);
		blockGen.registerFlowerPotPlant(VISHROOM, POTTED_VISHROOM, BlockStateModelGenerator.TintType.NOT_TINTED);
		blockGen.registerFlowerPotPlant(CORDISPORA, POTTED_CORDISPORA, BlockStateModelGenerator.TintType.NOT_TINTED);
		blockGen.registerFlowerPotPlant(SNOWDROP, POTTED_SNOWDROP, BlockStateModelGenerator.TintType.NOT_TINTED);
		blockGen.registerFlowerPotPlant(FIREWHEEL, POTTED_FIREWHEEL, BlockStateModelGenerator.TintType.NOT_TINTED);
		blockGen.registerFlowerPotPlant(LILIUM, POTTED_LILIUM, BlockStateModelGenerator.TintType.NOT_TINTED);
		
		blockGen.registerCrop(BEJEWELED_BEETS_BLOCK, Properties.AGE_3, 0, 1, 2, 3);
		
		blockGen.registerLog(SILVERWOOD_LOG).log(SILVERWOOD_LOG).wood(SILVERWOOD_WOOD);
		blockGen.registerLog(STRIPPED_SILVERWOOD_LOG).log(STRIPPED_SILVERWOOD_LOG).wood(STRIPPED_SILVERWOOD_WOOD);
		blockGen.registerLog(GREATWOOD_LOG).log(GREATWOOD_LOG).wood(GREATWOOD_WOOD);
		blockGen.registerLog(STRIPPED_GREATWOOD_LOG).log(STRIPPED_GREATWOOD_LOG).wood(STRIPPED_GREATWOOD_WOOD);
		blockGen.registerLog(TAINTWOOD_LOG).log(TAINTWOOD_LOG).wood(TAINTWOOD_WOOD);
		blockGen.registerLog(HOLLOWED_LOG).log(HOLLOWED_LOG).wood(HOLLOWED_WOOD);
		
		for(BlockFamily family : ArcanaBlockFamilies.ALL){
			blockGen.registerCubeAllModelTexturePool(family.getBaseBlock()).family(family);
			for(Block value : family.getVariants().values())
				noAutoGen.add(value.asItem());
		}
		
		blockGen.registerCooker(ARCANE_FURNACE, TexturedModel.ORIENTABLE);
		
		blockGen.registerNorthDefaultHorizontalRotation(MAGIC_MIRROR);
		
		blockGen.registerLantern(CRIMSON_LANTERN);
		registerBars(CHAIN_WALL, blockGen);
		
		for(CrystalClusterBlock value : Aspects.clusters.values()){
			blockGen.blockStateCollector.accept(VariantsBlockStateSupplier
					.create(value, BlockStateVariant.create())
					.coordinate(blockGen.createUpDefaultFacingVariantMap())
					.coordinate(BlockStateVariantMap.create(CrystalClusterBlock.SIZE).register(size -> {
						String suffix = size == 3 ? "" : "_" + (size + 1);
						return BlockStateVariant.create()
								.put(VariantSettings.MODEL, blockGen.createSubModel(value, suffix, Models.CROSS, TextureMap::cross));
					}))
			);
		}
		
		Aspects.crystalBlocks.values().forEach(blockGen::registerSimpleCubeAll);
	}
	
	public void generateItemModels(ItemModelGenerator itemGen){
		noAutoGen.add(WAND);
		noAutoGen.add(TOME_OF_SHARING);
		noAutoGen.add(DRINKABLE_TAINT);
		noAutoGen.add(CRIMSON_LONGBOW);
		noAutoGen.add(CRIMSON_LEECH);
		noAutoGen.add(NITOR.asItem());
		noAutoGen.add(INFUSION_PILLAR.asItem());
		noAutoGen.add(ESSENTIA_TUBE.asItem());
		noAutoGen.add(ESSENTIA_VALVE.asItem());
		noAutoGen.add(ESSENTIA_WINDOW.asItem());
		noAutoGen.add(ESSENTIA_PUMP.asItem());
		noAutoGen.add(ESSENTIA_ROUTER.asItem());
		noAutoGen.add(ESSENTIA_REDIRECT.asItem());
		noAutoGen.add(WARDED_CAMPFIRE.asItem());
		noAutoGen.add(CRIMSON_CAMPFIRE.asItem());
		noAutoGen.add(RESEARCH_TABLE.asItem());
		noAutoGen.add(THAUMIC_HALO.asItem());
		noAutoGen.add(CRIMSON_LANTERN.asItem());
		noAutoGen.add(CHAIN_WALL.asItem());
		noAutoGen.add(METAL_LADDER.asItem());
		noAutoGen.add(GREATWOOD_SCRIBING_DESK.asItem());
		noAutoGen.add(SILVERWOOD_SCRIBING_DESK.asItem());
		noAutoGen.add(MAGIC_MIRROR.asItem());
		noAutoGen.add(HOLDING_JUG);
		noAutoGen.add(BEJEWELED_BEET_SEEDS);
		noAutoGen.add(HUGE_CORDISPORA_CAP.asItem());
		noAutoGen.add(HUGE_VISHROOM_CAP.asItem());
		
		itemGen.register(NITOR.asItem(), Models.GENERATED);
		itemGen.register(MAGIC_MIRROR.asItem(), Models.GENERATED);
		itemGen.register(THAUMIC_HALO.asItem(), Models.GENERATED);
		
		for(CrystalClusterBlock value : Aspects.clusters.values()){
			noAutoGen.add(value.asItem());
			itemGen.register(value.asItem(), Models.GENERATED);
		}
		
		for(Item item : ITEMS)
			if(!(noAutoGen.contains(item) || item instanceof BlockItem))
				if(item instanceof ToolItem)
					itemGen.register(item, Models.HANDHELD);
				else if(item instanceof ScalpelItem) // TODO: custom template for 3rd person view
					itemGen.register(item, Models.HANDHELD);
				else
					itemGen.register(item, Models.GENERATED);
		
		for(Block block : BLOCKS)
			if(!noAutoGen.contains(block.asItem()) && block.asItem() != Items.AIR)
				itemGen.writer.accept(ModelIds.getItemModelId(block.asItem()), new SimpleModelSupplier(ModelIds.getBlockModelId(block)));
	}
	
	public String getName(){
		return "Arcana Blockstates and Models";
	}
	
	private void registerCross(Block block, BlockStateModelGenerator blockGen){
		//blockGen.registerTintableCross(block, BlockStateModelGenerator.TintType.NOT_TINTED);
		noAutoGen.add(block.asItem());
	}
	
	private void registerBars(Block block, BlockStateModelGenerator blockGen){
		Identifier postEnds = ModelIds.getBlockSubModelId(block, "_post_ends");
		Identifier post = ModelIds.getBlockSubModelId(block, "_post");
		Identifier cap = ModelIds.getBlockSubModelId(block, "_cap");
		Identifier capAlt = ModelIds.getBlockSubModelId(block, "_cap_alt");
		Identifier side = ModelIds.getBlockSubModelId(block, "_side");
		Identifier sideAlt = ModelIds.getBlockSubModelId(block, "_side_alt");
		blockGen.blockStateCollector.accept(
				MultipartBlockStateSupplier.create(block)
						.with(BlockStateVariant.create().put(VariantSettings.MODEL, postEnds))
						.with(
								When.create().set(Properties.NORTH, false).set(Properties.EAST, false).set(Properties.SOUTH, false).set(Properties.WEST, false),
								BlockStateVariant.create().put(VariantSettings.MODEL, post)
						)
						.with(
								When.create().set(Properties.NORTH, true).set(Properties.EAST, false).set(Properties.SOUTH, false).set(Properties.WEST, false),
								BlockStateVariant.create().put(VariantSettings.MODEL, cap)
						)
						.with(
								When.create().set(Properties.NORTH, false).set(Properties.EAST, true).set(Properties.SOUTH, false).set(Properties.WEST, false),
								BlockStateVariant.create().put(VariantSettings.MODEL, cap).put(VariantSettings.Y, VariantSettings.Rotation.R90)
						)
						.with(
								When.create().set(Properties.NORTH, false).set(Properties.EAST, false).set(Properties.SOUTH, true).set(Properties.WEST, false),
								BlockStateVariant.create().put(VariantSettings.MODEL, capAlt)
						)
						.with(
								When.create().set(Properties.NORTH, false).set(Properties.EAST, false).set(Properties.SOUTH, false).set(Properties.WEST, true),
								BlockStateVariant.create().put(VariantSettings.MODEL, capAlt).put(VariantSettings.Y, VariantSettings.Rotation.R90)
						)
						.with(When.create().set(Properties.NORTH, true), BlockStateVariant.create().put(VariantSettings.MODEL, side))
						.with(
								When.create().set(Properties.EAST, true),
								BlockStateVariant.create().put(VariantSettings.MODEL, side).put(VariantSettings.Y, VariantSettings.Rotation.R90)
						)
						.with(When.create().set(Properties.SOUTH, true), BlockStateVariant.create().put(VariantSettings.MODEL, sideAlt))
						.with(
								When.create().set(Properties.WEST, true),
								BlockStateVariant.create().put(VariantSettings.MODEL, sideAlt).put(VariantSettings.Y, VariantSettings.Rotation.R90)
						)
		);
		// extremely unclear why this is necessary here
		blockGen.excludeFromSimpleItemModelGeneration(block);
	}
}