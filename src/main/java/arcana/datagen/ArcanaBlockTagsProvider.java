package arcana.datagen;

import arcana.ArcanaRegistry;
import arcana.blocks.ArcanaBlockSettings;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.block.Block;
import net.minecraft.util.registry.Registry;

public class ArcanaBlockTagsProvider extends FabricTagProvider<Block>{
	
	public ArcanaBlockTagsProvider(FabricDataGenerator dataGenerator){
		super(dataGenerator, Registry.BLOCK);
	}
	
	protected void generateTags(){
		for(Block block : ArcanaRegistry.blocks)
			if(block.settings instanceof ArcanaBlockSettings abs)
				if(abs.getToolTag() != null)
					getOrCreateTagBuilder(abs.getToolTag()).add(block);
	}
}