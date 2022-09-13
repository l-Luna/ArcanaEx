package arcana.datagen;

import arcana.ArcanaTags;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.server.recipe.RecipeJsonProvider;

import java.util.function.Consumer;

import static arcana.ArcanaRegistry.*;

public class ArcanaRecipeProvider extends FabricRecipeProvider{
	
	public ArcanaRecipeProvider(FabricDataGenerator gen){
		super(gen);
	}
	
	protected void generateRecipes(Consumer<RecipeJsonProvider> exporter){
		offerPlanksRecipe(exporter, SILVERWOOD_PLANKS, ArcanaTags.SILVERWOOD_LOGS);
		offerBarkBlockRecipe(exporter, SILVERWOOD_WOOD, SILVERWOOD_LOG);
		offerBarkBlockRecipe(exporter, STRIPPED_SILVERWOOD_WOOD, STRIPPED_SILVERWOOD_LOG);
	}
}
