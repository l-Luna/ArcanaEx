package arcana.integration.emi;

import arcana.recipes.XIngredient;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;

public class EmiXIngredient{
	
	public static EmiIngredient of(XIngredient ingredient){
		// TODO: consider stack matchers for multiple possible stacks
		// TODO: stack matcher tooltips
		var stacks = ingredient.getMatchingStacks();
		if(stacks.length == 1)
			return EmiStack.of(stacks[0]);
		return ingredient.getContent().map(EmiStack::of, EmiIngredient::of);
	}
}