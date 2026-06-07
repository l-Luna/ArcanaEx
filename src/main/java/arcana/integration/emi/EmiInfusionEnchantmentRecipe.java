package arcana.integration.emi;

import arcana.aspects.AspectMap;
import arcana.aspects.AspectStack;
import arcana.recipes.infusion.InfusionEnchantmentRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EmiInfusionEnchantmentRecipe extends AbstractEmiInfusionRecipe{
	
	protected final Identifier id;
	protected final Enchantment enchantment;
	protected final List<EmiIngredient> baseOuters;
	protected final AspectMap baseAspects;
	protected final int baseInstability;
	protected final ItemStack previewCental;
	
	protected final List<EmiIngredient> catalysts;
	protected final List<EmiStack> outputs;
	
	public EmiInfusionEnchantmentRecipe(InfusionEnchantmentRecipe recipe){
		this.id = recipe.getId();
		this.enchantment = recipe.getEnchantment();
		this.baseOuters = recipe.getBaseIngredients().stream().map(EmiXIngredient::of).toList();
		this.baseAspects = recipe.getBaseAspects().copy();
		this.baseInstability = recipe.getBaseInstability();
		this.previewCental = recipe.getPreviewStack();
		
		catalysts = new ArrayList<>();
		outputs = new ArrayList<>();
		for(int i = 1; i <= enchantment.getMaxLevel(); i++){
			ItemStack bookCatalyst = new ItemStack(Items.ENCHANTED_BOOK);
			EnchantmentHelper.set(Map.of(enchantment, i), bookCatalyst);
			outputs.add(EmiStack.of(bookCatalyst));
		}
		catalysts.addAll(baseOuters);
		for(AspectStack aspectStack : baseAspects)
			catalysts.add(new AspectEmiStack(aspectStack));
	}
	
	public @Nullable Identifier getId(){
		return id;
	}
	
	public List<EmiIngredient> getInputs(){
		return List.of();
	}
	
	public List<EmiStack> getOutputs(){
		return outputs;
	}
	
	public List<EmiIngredient> getCatalysts(){
		return catalysts;
	}
	
	public boolean supportsRecipeTree(){
		return false;
	}
	
	public void addWidgets(WidgetHolder widgets){
		widgets.add(new DynamicWidgets(this, widgets, (group, key) -> {
			int reps = (int)(key % enchantment.getMaxLevel()) + 1;
			ItemStack output = previewCental.copy();
			EnchantmentHelper.set(Map.of(enchantment, reps), output);
			List<EmiIngredient> outers = new ArrayList<>(baseOuters.size() * reps);
			for(int i = 0; i < reps; i++)
				outers.addAll(baseOuters);
			AspectMap aspects = baseAspects.copy();
			aspects.multiply(__ -> (float)reps);
			addBaseWidgets(group, EmiStack.of(previewCental), outers, baseInstability + reps, aspects, EmiStack.of(output));
		}));
	}
}