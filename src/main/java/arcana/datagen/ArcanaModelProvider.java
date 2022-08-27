package arcana.datagen;

import arcana.ArcanaRegistry;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.client.BlockStateModelGenerator;
import net.minecraft.data.client.ItemModelGenerator;
import net.minecraft.data.client.Models;
import net.minecraft.item.Item;

public class ArcanaModelProvider extends FabricModelProvider{
	
	public ArcanaModelProvider(FabricDataGenerator gen){
		super(gen);
	}
	
	public void generateBlockStateModels(BlockStateModelGenerator blockGen){
	
	}
	
	public void generateItemModels(ItemModelGenerator itemGen){
		for(Item item : ArcanaRegistry.ITEMS)
			itemGen.register(item, Models.GENERATED);
	}
}