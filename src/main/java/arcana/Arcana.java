package arcana;

import arcana.aspects.ItemAspectRegistry;
import arcana.commands.NodeCommand;
import arcana.commands.ResearchCommand;
import arcana.commands.WarpCommand;
import arcana.recipes.AlchemyRecipe;
import arcana.recipes.ShapedArcaneCraftingRecipe;
import arcana.recipes.WandRecipe;
import arcana.research.Research;
import arcana.research.ResearchLoader;
import arcana.warp.WarpEvents;
import arcana.worldgen.NodalGeodes;
import arcana.worldgen.SurfaceNodeFeature;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Arcana implements ModInitializer{
	
	public static final String modid = "arcana";
	public static final Logger logger = LoggerFactory.getLogger(modid);
	
	public static final ItemAspectRegistry aspectRegistry = new ItemAspectRegistry();
	public static final ResearchLoader researchLoader = new ResearchLoader();
	
	@Override
	public void onInitialize(){
		logger.info("Loading Arcana");
		
		ArcanaRegistry.setup();
		
		WandRecipe.setup();
		ShapedArcaneCraftingRecipe.setup();
		AlchemyRecipe.setup();
		Research.setup();
		WarpEvents.setup();
		
		SurfaceNodeFeature.addToWorldgen();
		NodalGeodes.addToWorldgen();
		
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(aspectRegistry);
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(researchLoader);
		CommonLifecycleEvents.TAGS_LOADED.register((registries, client) -> aspectRegistry.applyAssociations());
		CommandRegistrationCallback.EVENT.register(NodeCommand::register);
		CommandRegistrationCallback.EVENT.register(ResearchCommand::register);
		CommandRegistrationCallback.EVENT.register(WarpCommand::register);
	}
	
	public static Identifier arcId(String s){
		return new Identifier(modid, s);
	}
	
	// resolves non-namespaced IDs in the arcana namespace, otherwise uses the given namespace
	public static Identifier maybeArcId(String s){
		if(s.contains(":"))
			return new Identifier(s);
		else
			return arcId(s);
	}
}