package arcana.datagen;

import arcana.ArcanaTags;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.block.Block;
import net.minecraft.data.family.BlockFamily;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.SmithingTransformRecipeJsonBuilder;
import net.minecraft.item.Item;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.resource.featuretoggle.FeatureSet;

import java.util.concurrent.CompletableFuture;

import static arcana.ArcanaRegistry.*;

public class ArcanaRecipeProvider extends FabricRecipeProvider{
	
	public ArcanaRecipeProvider(FabricDataOutput gen, CompletableFuture<RegistryWrapper.WrapperLookup> lookupFuture){
		super(gen, lookupFuture);
	}
	
	public void generate(RecipeExporter exporter){
		offerPlanksRecipe(exporter, SILVERWOOD_PLANKS, ArcanaTags.SILVERWOOD_LOGS, 4);
		offerBarkBlockRecipe(exporter, SILVERWOOD_WOOD, SILVERWOOD_LOG);
		offerBarkBlockRecipe(exporter, STRIPPED_SILVERWOOD_WOOD, STRIPPED_SILVERWOOD_LOG);
		
		offerPlanksRecipe(exporter, GREATWOOD_PLANKS, ArcanaTags.GREATWOOD_LOGS, 4);
		offerBarkBlockRecipe(exporter, GREATWOOD_WOOD, GREATWOOD_LOG);
		offerBarkBlockRecipe(exporter, STRIPPED_GREATWOOD_WOOD, STRIPPED_GREATWOOD_LOG);
		
		offerPlanksRecipe(exporter, TAINTWOOD_PLANKS, ArcanaTags.TAINTWOOD_LOGS, 4);
		offerBarkBlockRecipe(exporter, TAINTWOOD_WOOD, TAINTWOOD_LOG);
		offerPlanksRecipe(exporter, HOLLOWED_PLANKS, ArcanaTags.HOLLOWED_LOGS, 4);
		offerBarkBlockRecipe(exporter, HOLLOWED_WOOD, HOLLOWED_LOG);
		
		for(BlockFamily family : ArcanaBlockFamilies.ALL)
			generateFamily(exporter, family, FeatureSet.empty());
		
		offerStonecuttingRecipe(exporter, RecipeCategory.BUILDING_BLOCKS, ARCANE_STONE_BRICKS, ARCANE_STONE);
		offerStonecuttingRecipe(exporter, RecipeCategory.BUILDING_BLOCKS, ARCANE_STONE_TILES, ARCANE_STONE);
		
		offerStonecuttingRecipe(exporter, RecipeCategory.BUILDING_BLOCKS, ARCANE_STONE_SLAB, ARCANE_STONE, 2);
		offerStonecuttingRecipe(exporter, RecipeCategory.BUILDING_BLOCKS, ARCANE_STONE_STAIRS, ARCANE_STONE);
		offerStonecuttingRecipe(exporter, RecipeCategory.BUILDING_BLOCKS, ARCANE_STONE_WALL, ARCANE_STONE);
		
		offerStonecuttingRecipe(exporter, RecipeCategory.BUILDING_BLOCKS, ARCANE_STONE_BRICKS_SLAB, ARCANE_STONE_BRICKS, 2);
		offerStonecuttingRecipe(exporter, RecipeCategory.BUILDING_BLOCKS, ARCANE_STONE_BRICKS_STAIRS, ARCANE_STONE_BRICKS);
		offerStonecuttingRecipe(exporter, RecipeCategory.BUILDING_BLOCKS, ARCANE_STONE_BRICKS_WALL, ARCANE_STONE_BRICKS);
		
		offerStonecuttingRecipe(exporter, RecipeCategory.BUILDING_BLOCKS, ARCANE_STONE_TILES_SLAB, ARCANE_STONE_TILES, 2);
		offerStonecuttingRecipe(exporter, RecipeCategory.BUILDING_BLOCKS, ARCANE_STONE_TILES_STAIRS, ARCANE_STONE_TILES);
		offerStonecuttingRecipe(exporter, RecipeCategory.BUILDING_BLOCKS, ARCANE_STONE_TILES_WALL, ARCANE_STONE_TILES);
		
		offerReversibleCompactingRecipesWithReverseRecipeGroup(exporter, RecipeCategory.MISC, THAUMIUM_INGOT, RecipeCategory.BUILDING_BLOCKS, THAUMIUM_BLOCK, "thaumium_ingot_from_thaumium_block", "thaumium_ingot");
		offerReversibleCompactingRecipesWithCompactingRecipeGroup(exporter, RecipeCategory.MISC, THAUMIUM_NUGGET, RecipeCategory.MISC, THAUMIUM_INGOT, "thaumium_ingot_from_nuggets", "thaumium_ingot");
		offerReversibleCompactingRecipesWithReverseRecipeGroup(exporter, RecipeCategory.MISC, VOID_METAL_INGOT, RecipeCategory.BUILDING_BLOCKS, VOID_METAL_BLOCK, "void_metal_ingot_from_void_metal_block", "void_metal_ingot");
		offerReversibleCompactingRecipesWithCompactingRecipeGroup(exporter, RecipeCategory.MISC, VOID_METAL_NUGGET, RecipeCategory.MISC, VOID_METAL_INGOT, "void_metal_ingot_from_nuggets", "void_metal_ingot");
		offerReversibleCompactingRecipes(exporter, RecipeCategory.MISC, SILVERLEAF_AMALGAMATE, RecipeCategory.BUILDING_BLOCKS, SILVERLEAF_AMALGAMATE_BLOCK);
		
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
	
	protected void offerDoorRecipe(Block door, Block planks, RecipeExporter exporter){
		createDoorRecipe(door, Ingredient.ofItems(planks))
				.criterion(hasItem(planks), conditionsFromItem(planks))
				.offerTo(exporter);
	}
	
	protected void offerTrapdoorRecipe(Block door, Block planks, RecipeExporter exporter){
		createTrapdoorRecipe(door, Ingredient.ofItems(planks))
				.criterion(hasItem(planks), conditionsFromItem(planks))
				.offerTo(exporter);
	}
	
	protected void offerSignRecipe(Block door, Block planks, RecipeExporter exporter){
		createSignRecipe(door, Ingredient.ofItems(planks))
				.criterion(hasItem(planks), conditionsFromItem(planks))
				.offerTo(exporter);
	}
	
	public static void offerSmithingRecipe(RecipeExporter exporter, Item input, Item output, Item upgrade){
		SmithingTransformRecipeJsonBuilder.create(Ingredient.EMPTY, Ingredient.ofItems(input), Ingredient.ofItems(upgrade), RecipeCategory.TOOLS, output)
				.criterion(hasItem(upgrade), conditionsFromItem(upgrade))
				.offerTo(exporter, getItemPath(output) + "_smithing");
	}
}