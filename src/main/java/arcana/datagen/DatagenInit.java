package arcana.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class DatagenInit implements DataGeneratorEntrypoint{
	
	public void onInitializeDataGenerator(FabricDataGenerator gen){
		gen.addProvider(new ArcanaModelProvider(gen));
	}
}