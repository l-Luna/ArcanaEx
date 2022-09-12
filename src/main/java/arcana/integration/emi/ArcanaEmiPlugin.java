package arcana.integration.emi;

import arcana.ArcanaRegistry;
import arcana.aspects.Aspect;
import arcana.aspects.Aspects;
import arcana.aspects.ItemAspectRegistry;
import arcana.integration.emi.AspectEmiStack.AspectEmiStackSerializer;
import arcana.items.WandItem;
import arcana.recipes.AlchemyRecipe;
import arcana.recipes.ShapedArcaneCraftingRecipe;
import arcana.screens.ResearchEntryScreen;
import dev.emi.emi.EmiStackSerializer;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.EmiWorldInteractionRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;

import java.util.Comparator;
import java.util.stream.Collectors;

import static arcana.Arcana.arcId;

public final class ArcanaEmiPlugin implements EmiPlugin{
	
	public static final EmiRecipeCategory ITEMS_BY_ASPECTS = new EmiRecipeCategory(arcId("aspects"), new AspectEmiStack(Aspects.LIGHT));
	public static final EmiRecipeCategory ARCANE_CRAFTING = new EmiRecipeCategory(arcId("arcane_crafting"), ArcanaRegistry.ARCANE_CRAFTING_TABLE.asItem().emi());
	public static final EmiRecipeCategory ALCHEMY = new EmiRecipeCategory(arcId("alchemy"), ArcanaRegistry.CRUCIBLE.asItem().emi());
	
	public void register(EmiRegistry registry){
		
		// TODO: cleanup
		
		registry.addCategory(ITEMS_BY_ASPECTS);
		registry.addCategory(ARCANE_CRAFTING);
		registry.addCategory(ALCHEMY);
		
		for(Aspect value : Aspects.aspects.values())
			registry.addEmiStack(new AspectEmiStack(value));
		
		// This code takes all item-aspect assignments,
		// converts {Cobblestone -> 3x Earth, Entropy} into {Earth -> 3x Cobblestone, Entropy -> Cobblestone},
		// groups by aspects and turns those into recipes
		// TODO: ideally, we could display tags, tag bonuses, items, and inherited aspects separately
		ItemAspectRegistry.getAllItemAspects()
				.entrySet()
				.stream()
				.flatMap(entry ->
						entry.getValue().asStacks()
								.stream()
								.map(stack -> new Pair<>(stack.type(), new ItemStack(entry.getKey(), stack.amount()))))
				.sorted(Comparator.comparingInt(x -> -x.getRight().getCount()))
				.collect(Collectors.groupingBy(Pair::getLeft))
				.forEach((aspect, stacks) ->
						registry.addRecipe(new EmiItemsByAspectsRecipe(
								stacks.stream()
										.map(Pair::getRight)
										.map(EmiStack::of)
										.toList(),
								aspect)));
		
		registry.addRecipe(new EmiWandRecipe(arcId("wand")));
		
		EmiStack basicWand = WandItem.basicWand().emi();
		registry.addRecipe(EmiWorldInteractionRecipe.builder()
				.id(arcId("world_convert_arcane_crafting_table"))
				.leftInput(Blocks.CRAFTING_TABLE.asItem().emi())
				.rightInput(basicWand, true)
				.output(ArcanaRegistry.ARCANE_CRAFTING_TABLE.asItem().emi())
				.build());
		registry.addRecipe(EmiWorldInteractionRecipe.builder()
				.id(arcId("world_convert_crucible"))
				.leftInput(Blocks.CAULDRON.asItem().emi())
				.rightInput(basicWand, true)
				.output(ArcanaRegistry.CRUCIBLE.asItem().emi())
				.build());
		
		registry.addWorkstation(VanillaEmiRecipeCategories.CRAFTING, ArcanaRegistry.ARCANE_CRAFTING_TABLE.asItem().emi());
		registry.addWorkstation(ARCANE_CRAFTING, ArcanaRegistry.ARCANE_CRAFTING_TABLE.asItem().emi());
		registry.addWorkstation(ALCHEMY, ArcanaRegistry.CRUCIBLE.asItem().emi());
		
		registry.addRecipeHandler(ArcanaRegistry.ARCANE_CRAFTING_SCREEN_HANDLER, new EmiArcaneCraftingRecipeHandler());
		registry.addStackProvider(ResearchEntryScreen.class, new ResearchEntryScreenStackProvider());
		
		EmiStackSerializer.register(AspectEmiStackSerializer.ID, AspectEmiStack.class, new AspectEmiStackSerializer());
		
		var manager = registry.getRecipeManager();
		for(var arcaneCraftingRecipe : manager.listAllOfType(ShapedArcaneCraftingRecipe.TYPE))
			registry.addRecipe(new EmiArcaneCraftingRecipe(arcaneCraftingRecipe));
		for(var alchemyRecipe : manager.listAllOfType(AlchemyRecipe.TYPE))
			registry.addRecipe(new EmiAlchemyRecipe(alchemyRecipe));
	}
}
