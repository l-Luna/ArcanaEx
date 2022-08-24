package arcana;

import arcana.aspects.ItemAspectRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Arcana implements ModInitializer{
	
	public static final String modid = "arcana";
	public static final Logger logger = LoggerFactory.getLogger(modid);
	
	public static final ItemAspectRegistry aspectRegistry = new ItemAspectRegistry();
	
	@Override
	public void onInitialize(){
		logger.info("Loading Arcana");
		
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(aspectRegistry);
	}
	
	public static Identifier arcId(String s){
		return new Identifier(modid, s);
	}
}