package arcana.integration.emi;

import arcana.ArcanaRegistry;
import arcana.aspects.Aspect;
import arcana.aspects.Aspects;
import arcana.aspects.ItemAspectRegistry;
import arcana.items.WandItem;
import arcana.recipes.AlchemyRecipe;
import arcana.recipes.InfusionRecipe;
import arcana.recipes.ShapedArcaneCraftingRecipe;
import arcana.screens.ResearchEntryScreen;
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

@SuppressWarnings("UnstableApiUsage") // TagEmiIngredient
public final class ArcanaEmiPlugin implements EmiPlugin{
	
	public static final EmiRecipeCategory ITEMS_BY_ASPECTS = new EmiRecipeCategory(arcId("items_by_aspects"), new AspectEmiStack(Aspects.ENERGY));
	public static final EmiRecipeCategory ASPECTS_BY_ITEMS = new EmiRecipeCategory(arcId("aspects_by_items"), new AspectEmiStack(Aspects.LIGHT));
	
	public static final EmiRecipeCategory ARCANE_CRAFTING = new EmiRecipeCategory(arcId("arcane_crafting"), EmiStack.of(ArcanaRegistry.ARCANE_CRAFTING_TABLE.asItem()));
	public static final EmiRecipeCategory ALCHEMY = new EmiRecipeCategory(arcId("alchemy"), EmiStack.of(ArcanaRegistry.CRUCIBLE.asItem()));
	public static final EmiRecipeCategory INFUSION = new EmiRecipeCategory(arcId("infusion"), EmiStack.of(ArcanaRegistry.INFUSION_MATRIX.asItem()));
	
	public void register(EmiRegistry registry){
		
		// TODO: cleanup
		
		registry.addCategory(ITEMS_BY_ASPECTS);
		registry.addCategory(ASPECTS_BY_ITEMS);
		
		registry.addCategory(ARCANE_CRAFTING);
		registry.addCategory(ALCHEMY);
		registry.addCategory(INFUSION);
		
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
				.map(x -> new EmiAspectsByItemsRecipe(EmiStack.of(x.getKey()), x.getValue().asStacks()))
				.forEach(registry::addRecipe);
		
		registry.addRecipe(new EmiWandRecipe(arcId("wand")));
		
		EmiStack basicWand = EmiStack.of(WandItem.basicWand());
		registry.addRecipe(EmiWorldInteractionRecipe.builder()
				.id(arcId("world_convert_arcane_crafting_table"))
				.leftInput(EmiStack.of(Blocks.CRAFTING_TABLE.asItem()))
				.rightInput(basicWand, true)
				.output(EmiStack.of(ArcanaRegistry.ARCANE_CRAFTING_TABLE.asItem()))
				.build());
		registry.addRecipe(EmiWorldInteractionRecipe.builder()
				.id(arcId("world_convert_crucible"))
				.leftInput(EmiStack.of(Blocks.CAULDRON.asItem()))
				.rightInput(basicWand, true)
				.output(EmiStack.of(ArcanaRegistry.CRUCIBLE.asItem()))
				.build());
		
		registry.addRecipe(new EmiInfoRecipe(
				List.of(EmiStack.of(ArcanaRegistry.SCRIBBLED_NOTES), EmiStack.of(ArcanaRegistry.ARCANUM)),
				List.of(Text.translatable("emi.info.arcana.arcanum")),
				null
		));
		
		registry.addWorkstation(VanillaEmiRecipeCategories.CRAFTING, EmiStack.of(ArcanaRegistry.ARCANE_CRAFTING_TABLE.asItem()));
		registry.addWorkstation(ARCANE_CRAFTING, EmiStack.of(ArcanaRegistry.ARCANE_CRAFTING_TABLE.asItem()));
		registry.addWorkstation(ALCHEMY, EmiStack.of(ArcanaRegistry.CRUCIBLE.asItem()));
		registry.addWorkstation(INFUSION, EmiStack.of(ArcanaRegistry.INFUSION_MATRIX.asItem()));
		
		registry.addRecipeHandler(ArcanaRegistry.ARCANE_CRAFTING_SCREEN_HANDLER, new EmiArcaneCraftingRecipeHandler());
		registry.addStackProvider(ResearchEntryScreen.class, new ResearchEntryScreenStackProvider());
		
		registry.addIngredientSerializer(AspectEmiStack.class, new AspectEmiStack.AspectEmiStackSerializer());
		
		var manager = registry.getRecipeManager();
		manager.listAllOfType(ShapedArcaneCraftingRecipe.TYPE).stream().map(EmiArcaneCraftingRecipe::new).forEach(registry::addRecipe);
		manager.listAllOfType(AlchemyRecipe.TYPE).stream().map(EmiAlchemyRecipe::new).forEach(registry::addRecipe);
		manager.listAllOfType(InfusionRecipe.TYPE).stream().map(EmiInfusionRecipe::new).forEach(registry::addRecipe);
	}
}
