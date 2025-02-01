package arcana.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public final class DatagenInit implements DataGeneratorEntrypoint{
	
	public void onInitializeDataGenerator(FabricDataGenerator gen){
		gen.addProvider(ArcanaModelProvider::new);
		gen.addProvider(AspectsProvider::new);
		gen.addProvider(ArcanaLootTablesProvider::new);
		gen.addProvider(ArcanaBlockTagsProvider::new);
		gen.addProvider(ArcanaRecipeProvider::new);
	}
}