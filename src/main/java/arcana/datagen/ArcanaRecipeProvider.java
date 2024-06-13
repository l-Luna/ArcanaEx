package arcana.datagen;

import arcana.ArcanaTags;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.block.Block;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.recipe.Ingredient;

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
		
		offerDoorRecipe(SILVERWOOD_DOOR, SILVERWOOD_PLANKS, exporter);
		offerTrapdoorRecipe(SILVERWOOD_TRAPDOOR, SILVERWOOD_PLANKS, exporter);
		offerSignRecipe(SILVERWOOD_SIGN, SILVERWOOD_PLANKS, exporter);
		
		offerPlanksRecipe(exporter, GREATWOOD_PLANKS, ArcanaTags.GREATWOOD_LOGS);
		offerBarkBlockRecipe(exporter, GREATWOOD_WOOD, GREATWOOD_LOG);
		offerBarkBlockRecipe(exporter, STRIPPED_GREATWOOD_WOOD, STRIPPED_GREATWOOD_LOG);
		
		offerDoorRecipe(GREATWOOD_DOOR, GREATWOOD_PLANKS, exporter);
		offerTrapdoorRecipe(GREATWOOD_TRAPDOOR, GREATWOOD_PLANKS, exporter);
		offerSignRecipe(GREATWOOD_SIGN, GREATWOOD_PLANKS, exporter);
	}
	
	protected void offerDoorRecipe(Block door, Block planks, Consumer<RecipeJsonProvider> exporter){
		createDoorRecipe(door, Ingredient.ofItems(planks))
				.criterion(hasItem(planks), conditionsFromItem(planks))
				.offerTo(exporter);
	}
	
	protected void offerTrapdoorRecipe(Block door, Block planks, Consumer<RecipeJsonProvider> exporter){
		createTrapdoorRecipe(door, Ingredient.ofItems(planks))
				.criterion(hasItem(planks), conditionsFromItem(planks))
				.offerTo(exporter);
	}
	
	protected void offerSignRecipe(Block door, Block planks, Consumer<RecipeJsonProvider> exporter){
		createSignRecipe(door, Ingredient.ofItems(planks))
				.criterion(hasItem(planks), conditionsFromItem(planks))
				.offerTo(exporter);
	}
}