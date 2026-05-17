package arcana.integration.emi;

import arcana.ArcanaRegistry;
import arcana.aspects.Aspect;
import arcana.aspects.AspectStack;
import arcana.aspects.Aspects;
import arcana.aspects.ItemAspectRegistry;
import arcana.aura.Taint;
import arcana.items.WandItem;
import arcana.recipes.alchemy.AlchemyRecipe;
import arcana.recipes.arcane_crafting.ShapedArcaneCraftingRecipe;
import arcana.recipes.infusion.SimpleInfusionRecipe;
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
import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.*;

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
		
		Map<Aspect, List<EmiItemsByAspectsRecipe.Entry>> ibaData = new HashMap<>(Aspects.aspects.size());
		for(Aspect aspect : Aspects.aspects.values())
			ibaData.put(aspect, new ArrayList<>());
		ItemAspectRegistry.getAllItemAspects().forEach((item, aspects) -> {
			for(AspectStack aspectStack : aspects)
				ibaData.get(aspectStack.type()).add(new EmiItemsByAspectsRecipe.Entry(
						item,
						EmiStack.of(item.getDefaultStack()),
						aspectStack.amount(),
						aspectStack.amount() / (float)aspects.total()
				));
		});
		ibaData.forEach((aspect, entries) -> registry.addRecipe(new EmiItemsByAspectsRecipe(entries, aspect)));
		
		Taint.TAINT_MAP.getEntryMap().forEach((from, to) -> {
			registry.addRecipe(new EmiTaintingRecipe(EmiStack.of(from.asItem()), to.asItem(), Registry.BLOCK.getId(from)));
		});
		Taint.UNTAINT_MAP.getEntryMap().forEach((from, to) -> {
			registry.addRecipe(new EmiUntaintingRecipe(EmiStack.of(from.asItem()), to.asItem(), Registry.BLOCK.getId(from)));
		});
		Taint.TAINT_MAP.getTagMap().forEach((key, block) -> {
			registry.addRecipe(new EmiTaintingRecipe(EmiIngredient.of(key), block.asItem(), key.id()));
		});
		Taint.UNTAINT_MAP.getTagMap().forEach((key, block) -> {
			registry.addRecipe(new EmiUntaintingRecipe(EmiIngredient.of(key), block.asItem(), key.id()));
		});
		
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
		
		RecipeManager manager = registry.getRecipeManager();
		manager.listAllOfType(ShapedArcaneCraftingRecipe.TYPE).stream().filter(ShapedArcaneCraftingRecipe.class::isInstance).map(it -> new EmiArcaneCraftingRecipe((ShapedArcaneCraftingRecipe)it)).forEach(registry::addRecipe);
		manager.listAllOfType(AlchemyRecipe.TYPE).stream().map(EmiAlchemyRecipe::new).forEach(registry::addRecipe);
		manager.listAllOfType(SimpleInfusionRecipe.TYPE).stream().filter(SimpleInfusionRecipe.class::isInstance).map(recipe -> new EmiInfusionRecipe((SimpleInfusionRecipe)recipe)).forEach(registry::addRecipe);
	}
}