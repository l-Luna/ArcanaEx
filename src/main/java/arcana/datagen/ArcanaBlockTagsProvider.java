package arcana.datagen;

import arcana.ArcanaRegistry;
import arcana.blocks.ArcanaBlockSettings;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.block.Block;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class ArcanaBlockTagsProvider extends FabricTagProvider<Block>{
	
	public ArcanaBlockTagsProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture){
		super(output, RegistryKeys.BLOCK, registriesFuture);
	}
	
	protected void configure(RegistryWrapper.WrapperLookup lookup){
		for(Block block : ArcanaRegistry.BLOCKS)
			if(block.getSettings() instanceof ArcanaBlockSettings abs)
				if(abs.getToolTag() != null)
					getOrCreateTagBuilder(abs.getToolTag()).add(block);
	}
}