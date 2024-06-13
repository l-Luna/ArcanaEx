package arcana.datagen;

import arcana.ArcanaTags;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.block.Block;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.data.server.recipe.SmithingRecipeJsonBuilder;
import net.minecraft.item.Item;
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
		
		offerReversibleCompactingRecipes(exporter, VOID_METAL_INGOT, VOID_METAL_BLOCK);
		offerReversibleCompactingRecipes(exporter, SILVERLEAF_AMALGAMATE, SILVERLEAF_AMALGAMATE_BLOCK);
		
		offerSmithingRecipe(exporter, ARCANIUM_SWORD, VOID_METAL_SWORD, VOID_METAL_INGOT);
		offerSmithingRecipe(exporter, ARCANIUM_SHOVEL, VOID_METAL_SHOVEL, VOID_METAL_INGOT);
		offerSmithingRecipe(exporter, ARCANIUM_PICKAXE, VOID_METAL_PICKAXE, VOID_METAL_INGOT);
		offerSmithingRecipe(exporter, ARCANIUM_AXE, VOID_METAL_AXE, VOID_METAL_INGOT);
		offerSmithingRecipe(exporter, ARCANIUM_HOE, VOID_METAL_HOE, VOID_METAL_INGOT);
		offerSmithingRecipe(exporter, ARCANIUM_HELMET, VOID_METAL_HELMET, VOID_METAL_INGOT);
		offerSmithingRecipe(exporter, ARCANIUM_CHESTPLATE, VOID_METAL_CHESTPLATE, VOID_METAL_INGOT);
		offerSmithingRecipe(exporter, ARCANIUM_LEGGINGS, VOID_METAL_LEGGINGS, VOID_METAL_INGOT);
		offerSmithingRecipe(exporter, ARCANIUM_BOOTS, VOID_METAL_BOOTS, VOID_METAL_INGOT);
		
		offerSmithingRecipe(exporter, ARCANIUM_SWORD, SILVERLEAF_SWORD, SILVERLEAF_AMALGAMATE);
		offerSmithingRecipe(exporter, ARCANIUM_SHOVEL, SILVERLEAF_SHOVEL, SILVERLEAF_AMALGAMATE);
		offerSmithingRecipe(exporter, ARCANIUM_PICKAXE, SILVERLEAF_PICKAXE, SILVERLEAF_AMALGAMATE);
		offerSmithingRecipe(exporter, ARCANIUM_AXE, SILVERLEAF_AXE, SILVERLEAF_AMALGAMATE);
		offerSmithingRecipe(exporter, ARCANIUM_HOE, SILVERLEAF_HOE, SILVERLEAF_AMALGAMATE);
		offerSmithingRecipe(exporter, ARCANIUM_HELMET, SILVERLEAF_HELMET, SILVERLEAF_AMALGAMATE);
		offerSmithingRecipe(exporter, ARCANIUM_CHESTPLATE, SILVERLEAF_CHESTPLATE, SILVERLEAF_AMALGAMATE);
		offerSmithingRecipe(exporter, ARCANIUM_LEGGINGS, SILVERLEAF_LEGGINGS, SILVERLEAF_AMALGAMATE);
		offerSmithingRecipe(exporter, ARCANIUM_BOOTS, SILVERLEAF_BOOTS, SILVERLEAF_AMALGAMATE);
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
	
	public static void offerSmithingRecipe(Consumer<RecipeJsonProvider> exporter, Item input, Item output, Item upgrade){
		SmithingRecipeJsonBuilder.create(Ingredient.ofItems(input), Ingredient.ofItems(upgrade), output)
				.criterion(hasItem(upgrade), conditionsFromItem(upgrade))
				.offerTo(exporter, getItemPath(output) + "_smithing");
	}
}