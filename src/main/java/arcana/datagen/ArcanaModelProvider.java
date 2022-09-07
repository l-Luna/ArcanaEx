package arcana.datagen;

import arcana.ArcanaRegistry;
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
	}
	
	public void generateItemModels(ItemModelGenerator itemGen){
		noAutoGen.add(ArcanaRegistry.WAND);
		
		for(Item item : ArcanaRegistry.items)
			if(!noAutoGen.contains(item) && !(item instanceof BlockItem))
				if(item instanceof ToolItem)
					itemGen.register(item, Models.HANDHELD);
				else
					itemGen.register(item, Models.GENERATED);
		
		for(Block block : ArcanaRegistry.blocks)
			if(!(block instanceof ResearchTableBlock))
				itemGen.writer.accept(ModelIds.getItemModelId(block.asItem()), new SimpleModelSupplier(ModelIds.getBlockModelId(block)));
	}
}