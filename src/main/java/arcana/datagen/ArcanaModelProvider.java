package arcana.datagen;

import arcana.ArcanaRegistry;
import arcana.aspects.Aspects;
import arcana.blocks.CrystalClusterBlock;
import arcana.blocks.ResearchTableBlock;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.block.Block;
import net.minecraft.data.client.*;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ToolItem;

import java.util.ArrayList;
import java.util.List;

public final class ArcanaModelProvider extends FabricModelProvider{
	
	private final List<Item> noAutoGen = new ArrayList<>();
	
	public ArcanaModelProvider(FabricDataGenerator gen){
		super(gen);
	}
	
	public void generateBlockStateModels(BlockStateModelGenerator blockGen){
		blockGen.registerSimpleState(ArcanaRegistry.ARCANE_CRAFTING_TABLE);
		blockGen.registerSimpleState(ArcanaRegistry.NITOR);
		
		blockGen.registerSimpleCubeAll(ArcanaRegistry.ARCANIUM_BLOCK);
		blockGen.registerSimpleCubeAll(ArcanaRegistry.ARCANE_STONE);
		blockGen.registerSimpleCubeAll(ArcanaRegistry.ARCANE_STONE_BRICKS);
		
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
		noAutoGen.add(ArcanaRegistry.WAND);
		noAutoGen.add(ArcanaRegistry.NITOR.asItem());
		noAutoGen.add(ArcanaRegistry.TOME_OF_SHARING);
		
		itemGen.register(ArcanaRegistry.NITOR.asItem(), Models.GENERATED);
		
		for(CrystalClusterBlock value : Aspects.clusters.values()){
			noAutoGen.add(value.asItem());
			itemGen.register(value.asItem(), Models.GENERATED);
		}
		
		for(Item item : ArcanaRegistry.items)
			if(!noAutoGen.contains(item) && !(item instanceof BlockItem))
				if(item instanceof ToolItem)
					itemGen.register(item, Models.HANDHELD);
				else
					itemGen.register(item, Models.GENERATED);
		
		for(Block block : ArcanaRegistry.blocks)
			if(!(block instanceof ResearchTableBlock))
				if(!noAutoGen.contains(block.asItem()))
					itemGen.writer.accept(ModelIds.getItemModelId(block.asItem()), new SimpleModelSupplier(ModelIds.getBlockModelId(block)));
	}
	
	public String getName(){
		return "Arcana Blockstates and Models";
	}
}