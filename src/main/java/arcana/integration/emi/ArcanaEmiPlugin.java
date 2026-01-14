package arcana.integration.emi;

import arcana.ArcanaRegistry;
import arcana.aspects.Aspect;
import arcana.aspects.Aspects;
import arcana.aspects.ItemAspectRegistry;
import arcana.aura.Taint;
import arcana.items.WandItem;
import arcana.recipes.AlchemyRecipe;
import arcana.recipes.InfusionRecipe;
import arcana.recipes.ShapedArcaneCraftingRecipe;
import arcana.screens.ResearchEntryScreen;
import dev.emi.emi.api.EmiInitRegistry;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiInfoRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.EmiWorldInteractionRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.config.FluidUnit;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.registry.Registry;

import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static arcana.Arcana.arcId;

public final class ArcanaEmiPlugin implements EmiPlugin{
	
	public static final Random RNG = new Random();
	
	public static final EmiRecipeCategory ITEMS_BY_ASPECTS = new EmiRecipeCategory(arcId("items_by_aspects"), new AspectEmiStack(Aspects.ENERGY));
	public static final EmiRecipeCategory ASPECTS_BY_ITEMS = new EmiRecipeCategory(arcId("aspects_by_items"), new AspectEmiStack(Aspects.LIGHT));
	
	public static final EmiRecipeCategory TAINTING = new EmiRecipeCategory(arcId("tainting"), new AspectEmiStack(Aspects.TAINT));
	public static final EmiRecipeCategory UNTAINTING = new EmiRecipeCategory(arcId("untainting"), new AspectEmiStack(Aspects.AURA));
	
	public static final EmiRecipeCategory ARCANE_CRAFTING = new EmiRecipeCategory(arcId("arcane_crafting"), EmiStack.of(ArcanaRegistry.ARCANE_CRAFTING_TABLE.asItem()));
	public static final EmiRecipeCategory ALCHEMY = new EmiRecipeCategory(arcId("alchemy"), EmiStack.of(ArcanaRegistry.CRUCIBLE.asItem()));
	public static final EmiRecipeCategory INFUSION = new EmiRecipeCategory(arcId("infusion"), EmiStack.of(ArcanaRegistry.INFUSION_MATRIX.asItem()));
	public static final EmiRecipeCategory ASPECT_CRYSTALLIZATION = new EmiRecipeCategory(arcId("aspect_crystallization"), EmiStack.of(ArcanaRegistry.CRYSTALLIZATION_PRESS.asItem()));
	
	public static final Identifier WIDGETS = arcId("textures/gui/emi/widgets.png");
	
	public void initialize(EmiInitRegistry registry){
		registry.addIngredientSerializer(AspectEmiStack.class, new AspectEmiStack.AspectEmiStackSerializer());
	}
	
	public void register(EmiRegistry registry){
		// TODO: cleanup
		
		registry.addCategory(ITEMS_BY_ASPECTS);
		registry.addCategory(ASPECTS_BY_ITEMS);
		
		registry.addCategory(TAINTING);
		registry.addCategory(UNTAINTING);
		
		registry.addCategory(ARCANE_CRAFTING);
		registry.addCategory(ALCHEMY);
		registry.addCategory(INFUSION);
		registry.addCategory(ASPECT_CRYSTALLIZATION);
		
		for(Aspect value : Aspects.aspects.values())
			registry.addEmiStack(new AspectEmiStack(value));
		
		// take all item-aspect assignments,
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
				.map(x -> new EmiAspectsByItemsRecipe(EmiIngredient.of(x.getKey(), 1), x.getValue().asStacks(), x.getKey().id()))
				.forEach(registry::addRecipe);
		
		ItemAspectRegistry.getAllItemAspects().entrySet().stream()
				.filter(x -> !x.getValue().isEmpty())
				// skip items that can be grouped under a tag
				.filter(x -> !ItemAspectRegistry.usesTagAspects(x.getKey()) || ItemAspectRegistry.hasAnyBonusAspects(x.getKey()))
				.map(x -> new EmiAspectsByItemsRecipe(EmiStack.of(x.getKey()), x.getValue().asStacks(), Registry.ITEM.getId(x.getKey())))
				.forEach(registry::addRecipe);
		
		Taint.TAINT_MAP.forEach((from, to) -> registry.addRecipe(new EmiTaintingRecipe(EmiStack.of(from.asItem()), to.asItem(), Registry.BLOCK.getId(from))));
		Taint.UNTAINT_MAP.forEach((from, to) -> registry.addRecipe(new EmiUntaintingRecipe(EmiStack.of(from.asItem()), to.asItem(), Registry.BLOCK.getId(from))));
		for(Pair<TagKey<Block>, Block> pair : Taint.TAINT_TAGS)
			registry.addRecipe(new EmiTaintingRecipe(EmiIngredient.of(pair.getLeft()), pair.getRight().asItem(), pair.getLeft().id()));
		for(Pair<TagKey<Block>, Block> pair : Taint.UNTAINT_TAGS)
			registry.addRecipe(new EmiUntaintingRecipe(EmiIngredient.of(pair.getLeft()), pair.getRight().asItem(), pair.getLeft().id()));
		
		Aspects.getOrderedAspects().stream().map(EmiAspectCrystallizationRecipe::new).forEach(registry::addRecipe);
		
		registry.addRecipe(new EmiWandRecipe(arcId("wand")));
		registry.addRecipe(new EmiVoidPuttyRepairRecipe(arcId("void_putty_repair")));
		registry.addRecipe(new EmiVoidPuttyAnvilRepairRecipe(arcId("/void_putty_anvil_repair")));
		
		registry.addRecipe(new EmiAltBrewingRecipe(
				EmiStack.of(Items.POTION.getDefaultStack()),
				EmiStack.of(ArcanaRegistry.SILVERLEAF, 8),
				EmiStack.of(ArcanaRegistry.SILVERLEAF_BREW),
				arcId("/brewing/silverleaf_brew")
		));
		
		EmiStack basicWand = EmiStack.of(WandItem.basicWand());
		registry.addRecipe(EmiWorldInteractionRecipe.builder()
				.id(arcId("/world_convert_arcane_crafting_table"))
				.leftInput(EmiStack.of(Blocks.CRAFTING_TABLE.asItem()))
				.rightInput(basicWand, true)
				.output(EmiStack.of(ArcanaRegistry.ARCANE_CRAFTING_TABLE.asItem()))
				.build());
		registry.addRecipe(EmiWorldInteractionRecipe.builder()
				.id(arcId("/world_convert_crucible"))
				.leftInput(EmiStack.of(Blocks.CAULDRON.asItem()))
				.rightInput(basicWand, true)
				.output(EmiStack.of(ArcanaRegistry.CRUCIBLE.asItem()))
				.build());
		
		registry.addRecipe(EmiWorldInteractionRecipe.builder()
				.id(arcId("/fluid_interaction/taint_goo"))
				.leftInput(EmiStack.of(ArcanaRegistry.STILL_TAINT_GOO, FluidUnit.BUCKET))
				.rightInput(EmiStack.of(Fluids.LAVA, FluidUnit.BUCKET), true)
				.output(EmiStack.of(ArcanaRegistry.TAINT_CRUST.asItem()))
				.build());
		
		for(Aspect aspect : Aspects.hasCluster){
			// this is a bit silly, but does get across the general idea
			EmiIngredient aspectStack;
			if(aspect.equals(Aspects.AURA))
				aspectStack = EmiIngredient.of(Aspects.primals.stream().map(AspectEmiStack::new).toList(), 24);
			else
				aspectStack = new AspectEmiStack(aspect, 8);
			registry.addRecipe(EmiWorldInteractionRecipe.builder()
					.id(arcId("/cluster_growth/" + aspect.id().getPath()))
					.leftInput(EmiStack.of(Aspects.clusterSeeds.get(aspect)))
					.rightInput(aspectStack, false)
					.output(EmiStack.of(Aspects.clusters.get(aspect).asItem()))
					.supportsRecipeTree(false)
					.build());
		}
		
		registry.addRecipe(new EmiInfoRecipe(
				List.of(EmiStack.of(ArcanaRegistry.SCRIBBLED_NOTES), EmiStack.of(ArcanaRegistry.ARCANUM)),
				List.of(Text.translatable("emi.info.arcana.arcanum")),
				arcId("/info/arcanum")
		));
		
		registry.addWorkstation(VanillaEmiRecipeCategories.CRAFTING, EmiStack.of(ArcanaRegistry.ARCANE_CRAFTING_TABLE.asItem()));
		registry.addWorkstation(ARCANE_CRAFTING, EmiStack.of(ArcanaRegistry.ARCANE_CRAFTING_TABLE.asItem()));
		registry.addWorkstation(ALCHEMY, EmiStack.of(ArcanaRegistry.CRUCIBLE.asItem()));
		registry.addWorkstation(INFUSION, EmiStack.of(ArcanaRegistry.INFUSION_MATRIX.asItem()));
		registry.addWorkstation(ASPECT_CRYSTALLIZATION, EmiStack.of(ArcanaRegistry.CRYSTALLIZATION_PRESS.asItem()));
		
		registry.addRecipeHandler(ArcanaRegistry.ARCANE_CRAFTING_SCREEN_HANDLER, new EmiArcaneCraftingRecipeHandler());
		registry.addStackProvider(ResearchEntryScreen.class, new ResearchEntryScreenStackProvider());
		
		var manager = registry.getRecipeManager();
		manager.listAllOfType(ShapedArcaneCraftingRecipe.TYPE).stream().map(EmiArcaneCraftingRecipe::new).forEach(registry::addRecipe);
		manager.listAllOfType(AlchemyRecipe.TYPE).stream().map(EmiAlchemyRecipe::new).forEach(registry::addRecipe);
		manager.listAllOfType(InfusionRecipe.TYPE).stream().map(EmiInfusionRecipe::new).forEach(registry::addRecipe);
	}
}