package arcana.integration.emi;

import arcana.aspects.Aspect;
import arcana.aspects.Aspects;
import arcana.aspects.ItemAspectRegistry;
import arcana.integration.emi.AspectEmiStack.AspectEmiStackSerializer;
import dev.emi.emi.EmiStackSerializer;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;

import java.util.Comparator;
import java.util.stream.Collectors;

import static arcana.Arcana.arcId;

public class ArcanaEmiPlugin implements EmiPlugin{
	
	public static EmiRecipeCategory ASPECTS = new EmiRecipeCategory(arcId("aspects"), new AspectEmiStack(Aspects.LIGHT));
	
	public void register(EmiRegistry registry){
		registry.addCategory(ASPECTS);
		
		for(Aspect value : Aspects.ASPECTS.values())
			registry.addEmiStack(new AspectEmiStack(value));
		
		// This code takes all item-aspect assignments,
		// converts {Cobblestone -> 3x Earth, Entropy} into {Earth -> 3x Cobblestone, Entropy -> Cobblestone},
		// groups by aspects and turns those into recipes
		// TODO: ideally, we could display tags, tag bonuses, items, and inherited aspects separately
		ItemAspectRegistry.getAllItemAspects()
				.entrySet()
				.stream()
				.flatMap(entry ->
						entry.getValue()
								.stream()
								.map(stack -> new Pair<>(stack.type(), new ItemStack(entry.getKey(), stack.amount()))))
				.sorted(Comparator.comparingInt(x -> -x.getRight().getCount()))
				.collect(Collectors.groupingBy(Pair::getLeft))
				.forEach((aspect, stacks) ->
						registry.addRecipe(new EmiAspectsRecipe(
								stacks.stream()
										.map(Pair::getRight)
										.map(EmiStack::of)
										.toList(),
								aspect)));
		
		EmiStackSerializer.register(AspectEmiStackSerializer.ID, AspectEmiStack.class, new AspectEmiStackSerializer());
	}
}
