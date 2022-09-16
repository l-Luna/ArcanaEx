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
import dev.emi.emi.api.recipe.EmiInfoRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.EmiWorldInteractionRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.TagEmiIngredient;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static arcana.Arcana.arcId;

@SuppressWarnings("UnstableApiUsage") // EmiInfoRecipe & .emi()
public final class ArcanaEmiPlugin implements EmiPlugin{
	
	public static final EmiRecipeCategory ITEMS_BY_ASPECTS = new EmiRecipeCategory(arcId("items_by_aspects"), new AspectEmiStack(Aspects.ENERGY));
	public static final EmiRecipeCategory ASPECTS_BY_ITEMS = new EmiRecipeCategory(arcId("aspects_by_items"), new AspectEmiStack(Aspects.LIGHT));
	public static final EmiRecipeCategory ARCANE_CRAFTING = new EmiRecipeCategory(arcId("arcane_crafting"), ArcanaRegistry.ARCANE_CRAFTING_TABLE.asItem().emi());
	public static final EmiRecipeCategory ALCHEMY = new EmiRecipeCategory(arcId("alchemy"), ArcanaRegistry.CRUCIBLE.asItem().emi());
	
	public void register(EmiRegistry registry){
		
		// TODO: cleanup
		
		registry.addCategory(ITEMS_BY_ASPECTS);
		registry.addCategory(ASPECTS_BY_ITEMS);
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
		
		// add tags first
		ItemAspectRegistry.getAllTagAspects().entrySet().stream()
				.map(x -> new EmiAspectsByItemsRecipe(new TagEmiIngredient(x.getKey(), 1), x.getValue().asStacks()))
				.forEach(registry::addRecipe);
		
		ItemAspectRegistry.getAllItemAspects().entrySet().stream()
				.filter(x -> x.getValue().size() > 0)
				// skip items that can be grouped under a tag
				.filter(x -> !ItemAspectRegistry.usesTagAspects(x.getKey()) || ItemAspectRegistry.hasAnyBonusAspects(x.getKey()))
				.map(x -> new EmiAspectsByItemsRecipe(x.getKey().emi(), x.getValue().asStacks()))
				.forEach(registry::addRecipe);
		
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
		
		registry.addRecipe(new EmiInfoRecipe(
				List.of(ArcanaRegistry.SCRIBBLED_NOTES.emi(), ArcanaRegistry.ARCANUM.emi()),
				List.of(Text.translatable("emi.info.arcana.arcanum")),
				null
		));
		
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
