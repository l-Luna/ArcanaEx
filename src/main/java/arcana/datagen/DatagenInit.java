package arcana.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public final class DatagenInit implements DataGeneratorEntrypoint{
	
	public void onInitializeDataGenerator(FabricDataGenerator gen){
		FabricDataGenerator.Pack pack = gen.createPack();
		pack.addProvider(ArcanaModelProvider::new);
		pack.addProvider(AspectsProvider::new);
		pack.addProvider(ArcanaLootTablesProvider::new);
		pack.addProvider(ArcanaBlockTagsProvider::new);
		pack.addProvider(ArcanaRecipeProvider::new);
	}
}