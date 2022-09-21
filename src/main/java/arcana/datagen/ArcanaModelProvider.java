package arcana.datagen;

import arcana.aspects.Aspects;
import arcana.blocks.CrystalClusterBlock;
import arcana.blocks.ResearchTableBlock;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.block.Block;
import net.minecraft.data.client.*;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.ToolItem;

import java.util.ArrayList;
import java.util.List;

import static arcana.ArcanaRegistry.*;

public final class ArcanaModelProvider extends FabricModelProvider{
	
	private final List<Item> noAutoGen = new ArrayList<>();
	
	public ArcanaModelProvider(FabricDataGenerator gen){
		super(gen);
	}
	
	public void generateBlockStateModels(BlockStateModelGenerator blockGen){
		blockGen.registerSimpleState(ARCANE_CRAFTING_TABLE);
		blockGen.registerSimpleState(NITOR);
		blockGen.registerSimpleState(PEDESTAL);
		blockGen.registerSimpleState(ARCANE_LEVITATOR);
		
		blockGen.registerSimpleCubeAll(ARCANIUM_BLOCK);
		blockGen.registerSimpleCubeAll(ARCANE_STONE);
		blockGen.registerSimpleCubeAll(ARCANE_STONE_BRICKS);
		blockGen.registerSimpleCubeAll(SILVERWOOD_PLANKS);
		blockGen.registerSimpleCubeAll(SILVERWOOD_LEAVES);
		blockGen.registerSimpleCubeAll(GREATWOOD_PLANKS);
		blockGen.registerSingleton(GREATWOOD_LEAVES, TexturedModel.LEAVES);
		blockGen.registerSimpleCubeAll(HARDENED_GLASS);
		blockGen.registerSimpleCubeAll(LUMINIFEROUS_GLASS);
		blockGen.registerSimpleCubeAll(STATIC_GLASS);
		blockGen.registerSimpleCubeAll(PAVING_STONE_OF_TRAVEL);
		blockGen.registerSimpleCubeAll(PAVING_STONE_OF_WARDING);
		
		blockGen.registerTintableCross(SILVERWOOD_SAPLING, BlockStateModelGenerator.TintType.NOT_TINTED);
		noAutoGen.add(SILVERWOOD_SAPLING.asItem());
		blockGen.registerTintableCross(GREATWOOD_SAPLING, BlockStateModelGenerator.TintType.NOT_TINTED);
		noAutoGen.add(GREATWOOD_SAPLING.asItem());
		
		blockGen.registerLog(SILVERWOOD_LOG).log(SILVERWOOD_LOG).wood(SILVERWOOD_WOOD);
		blockGen.registerLog(STRIPPED_SILVERWOOD_LOG).log(STRIPPED_SILVERWOOD_LOG).wood(STRIPPED_SILVERWOOD_WOOD);
		blockGen.registerLog(GREATWOOD_LOG).log(GREATWOOD_LOG).wood(GREATWOOD_WOOD);
		blockGen.registerLog(STRIPPED_GREATWOOD_LOG).log(STRIPPED_GREATWOOD_LOG).wood(STRIPPED_GREATWOOD_WOOD);
		
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
	}
	
	public void generateItemModels(ItemModelGenerator itemGen){
		noAutoGen.add(WAND);
		noAutoGen.add(NITOR.asItem());
		noAutoGen.add(TOME_OF_SHARING);
		
		itemGen.register(NITOR.asItem(), Models.GENERATED);
		
		for(CrystalClusterBlock value : Aspects.clusters.values()){
			noAutoGen.add(value.asItem());
			itemGen.register(value.asItem(), Models.GENERATED);
		}
		
		for(Item item : items)
			if(!noAutoGen.contains(item) && !(item instanceof BlockItem))
				if(item instanceof ToolItem)
					itemGen.register(item, Models.HANDHELD);
				else
					itemGen.register(item, Models.GENERATED);
		
		for(Block block : blocks)
			if(!(block instanceof ResearchTableBlock))
				if(!noAutoGen.contains(block.asItem()) && block.asItem() != Items.AIR)
					itemGen.writer.accept(ModelIds.getItemModelId(block.asItem()), new SimpleModelSupplier(ModelIds.getBlockModelId(block)));
	}
	
	public String getName(){
		return "Arcana Blockstates and Models";
	}
}