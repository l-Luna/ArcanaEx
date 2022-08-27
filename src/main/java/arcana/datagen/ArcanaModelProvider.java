package arcana.datagen;

import arcana.ArcanaRegistry;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.client.BlockStateModelGenerator;
import net.minecraft.data.client.ItemModelGenerator;
import net.minecraft.data.client.Models;
import net.minecraft.item.Item;

import java.util.ArrayList;
import java.util.List;

public class ArcanaModelProvider extends FabricModelProvider{
	
	private final List<Item> noAutoGen = new ArrayList<>();
	
	public ArcanaModelProvider(FabricDataGenerator gen){
		super(gen);
	}
	
	public void generateBlockStateModels(BlockStateModelGenerator blockGen){
	
	}
	
	public void generateItemModels(ItemModelGenerator itemGen){
		noAutoGen.add(ArcanaRegistry.WAND);
		
		for(Item item : ArcanaRegistry.ITEMS)
			if(!noAutoGen.contains(item))
				itemGen.register(item, Models.GENERATED);
	}
}