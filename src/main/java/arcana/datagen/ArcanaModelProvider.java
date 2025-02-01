package arcana.datagen;

import arcana.aspects.Aspects;
import arcana.blocks.CrystalClusterBlock;
import arcana.blocks.SymbolBlock;
import arcana.entities.locomotive.Symbol;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
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
import java.util.Optional;

import static arcana.Arcana.arcId;
import static arcana.ArcanaRegistry.*;

public final class ArcanaModelProvider extends FabricModelProvider{
	
	private static final Model symbolModel = new Model(Optional.of(arcId("block/locomotive_symbols/parent")), Optional.empty(), TextureKey.TEXTURE);
	
	private final List<Item> noAutoGen = new ArrayList<>();
	
	public ArcanaModelProvider(FabricDataGenerator gen){
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
		blockGen.registerSimpleCubeAll(TAINT_CRUST);
		
		blockGen.registerTintableCross(SILVERWOOD_SAPLING, BlockStateModelGenerator.TintType.NOT_TINTED);
		noAutoGen.add(SILVERWOOD_SAPLING.asItem());
		blockGen.registerTintableCross(GREATWOOD_SAPLING, BlockStateModelGenerator.TintType.NOT_TINTED);
		noAutoGen.add(GREATWOOD_SAPLING.asItem());
		
		blockGen.registerLog(SILVERWOOD_LOG).log(SILVERWOOD_LOG).wood(SILVERWOOD_WOOD);
		blockGen.registerLog(STRIPPED_SILVERWOOD_LOG).log(STRIPPED_SILVERWOOD_LOG).wood(STRIPPED_SILVERWOOD_WOOD);
		blockGen.registerLog(GREATWOOD_LOG).log(GREATWOOD_LOG).wood(GREATWOOD_WOOD);
		blockGen.registerLog(STRIPPED_GREATWOOD_LOG).log(STRIPPED_GREATWOOD_LOG).wood(STRIPPED_GREATWOOD_WOOD);
		
		for(BlockFamily family : ArcanaBlockFamilies.ALL){
			blockGen.registerCubeAllModelTexturePool(family.getBaseBlock()).family(family);
			for(Block value : family.getVariants().values())
				noAutoGen.add(value.asItem());
		}
		
		blockGen.registerCooker(ARCANE_FURNACE, TexturedModel.ORIENTABLE);
		
		blockGen.registerLantern(CRIMSON_LANTERN);
		registerBars(CHAIN_WALL, blockGen);
		
		for(CrystalClusterBlock value : Aspects.clusters.values()){
			blockGen.blockStateCollector.accept(VariantsBlockStateSupplier
					.create(value, BlockStateVariant.create())
					.coordinate(blockGen.createUpDefaultFacingVariantMap())
					.coordinate(BlockStateVariantMap.create(CrystalClusterBlock.size).register(size -> {
						String suffix = size == 3 ? "" : "_" + (size + 1);
						return BlockStateVariant.create()
								.put(VariantSettings.MODEL, blockGen.createSubModel(value, suffix, Models.CROSS, TextureMap::cross));
					}))
			);
		}
		
		for(SymbolBlock block : Symbol.blocks){
			blockGen.registerNorthDefaultHorizontalRotation(block);
			symbolModel.upload(block, TextureMap.texture(block), blockGen.modelCollector);
		}
	}
	
	public void generateItemModels(ItemModelGenerator itemGen){
		noAutoGen.add(WAND);
		noAutoGen.add(TOME_OF_SHARING);
		noAutoGen.add(DRINKABLE_TAINT);
		noAutoGen.add(CRIMSON_LONGBOW);
		noAutoGen.add(NITOR.asItem());
		noAutoGen.add(INFUSION_PILLAR.asItem());
		noAutoGen.add(ESSENTIA_TUBE.asItem());
		noAutoGen.add(ESSENTIA_VALVE.asItem());
		noAutoGen.add(ESSENTIA_WINDOW.asItem());
		noAutoGen.add(ESSENTIA_PUMP.asItem());
		noAutoGen.add(ESSENTIA_ROUTER.asItem());
		noAutoGen.add(ESSENTIA_REDIRECT.asItem());
		noAutoGen.add(WARDED_CAMPFIRE.asItem());
		noAutoGen.add(RESEARCH_TABLE.asItem());
		noAutoGen.add(THAUMIC_HALO.asItem());
		noAutoGen.add(CRIMSON_LANTERN.asItem());
		noAutoGen.add(CHAIN_WALL.asItem());
		noAutoGen.add(METAL_LADDER.asItem());
		
		itemGen.register(NITOR.asItem(), Models.GENERATED);
		itemGen.register(THAUMIC_HALO.asItem(), Models.GENERATED);
		
		for(CrystalClusterBlock value : Aspects.clusters.values()){
			noAutoGen.add(value.asItem());
			itemGen.register(value.asItem(), Models.GENERATED);
		}
		for(Block value : Symbol.blocks){
			noAutoGen.add(value.asItem());
			Models.GENERATED.upload(ModelIds.getItemModelId(value.asItem()), TextureMap.layer0(value), itemGen.writer);
		}
		
		for(Item item : items)
			if(!(noAutoGen.contains(item) || item instanceof BlockItem))
				if(item instanceof ToolItem)
					itemGen.register(item, Models.HANDHELD);
				else
					itemGen.register(item, Models.GENERATED);
		
		for(Block block : blocks)
			if(!noAutoGen.contains(block.asItem()) && block.asItem() != Items.AIR)
				itemGen.writer.accept(ModelIds.getItemModelId(block.asItem()), new SimpleModelSupplier(ModelIds.getBlockModelId(block)));
	}
	
	public String getName(){
		return "Arcana Blockstates and Models";
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