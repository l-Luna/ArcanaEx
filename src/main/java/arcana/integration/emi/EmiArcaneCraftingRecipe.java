package arcana.integration.emi;

import arcana.aspects.Aspect;
import arcana.aspects.AspectMap;
import arcana.aspects.AspectStack;
import arcana.aspects.Aspects;
import arcana.recipes.ShapedArcaneCraftingRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec2f;
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static arcana.Arcana.arcId;

public class EmiArcaneCraftingRecipe implements EmiRecipe{
	
	private static final Identifier texture = arcId("textures/gui/emi/arcane_crafting.png");
	private static final EmiTexture background = new EmiTexture(texture, 0, 0, 129, 120);
	
	private static final Map<Aspect, Vec2f> aspectPositions = Map.of(
			Aspects.AIR, new Vec2f(46, 3),
			Aspects.FIRE, new Vec2f(3, 27),
			Aspects.WATER, new Vec2f(89, 27),
			Aspects.EARTH, new Vec2f(3, 77),
			Aspects.ORDER, new Vec2f(89, 77),
			Aspects.ENTROPY, new Vec2f(46, 101)
	);
	
	protected final Identifier id;
	protected final List<EmiIngredient> input;
	protected final EmiStack output;
	protected final AspectMap aspects;
	
	public EmiArcaneCraftingRecipe(ShapedArcaneCraftingRecipe recipe){
		this(recipe.getId(), padIngredients(recipe), recipe.getOutput().emi(), recipe.aspects());
	}
	
	public EmiArcaneCraftingRecipe(Identifier id, List<EmiIngredient> input, EmiStack output, AspectMap aspects){
		this.id = id;
		this.input = input;
		this.output = output;
		this.aspects = aspects;
	}
	
	public EmiRecipeCategory getCategory(){
		return ArcanaEmiPlugin.ARCANE_CRAFTING;
	}
	
	public @Nullable Identifier getId(){
		return id;
	}
	
	public List<EmiIngredient> getInputs(){
		return Stream.concat(input.stream(), aspects.asStacks().stream().map(AspectEmiStack::new)).toList();
	}
	
	public List<EmiStack> getOutputs(){
		return List.of(output);
	}
	
	// TODO: consider using a smaller visual
	
	public int getDisplayWidth(){
		return 129;
	}
	
	public int getDisplayHeight(){
		return 120;
	}
	
	public void addWidgets(WidgetHolder widgets){
		widgets.addTexture(background, 0, 0);
		for(int x = 0; x < 3; x++)
			for(int y = 0; y < 3; y++){
				int idx = x + y * 3;
				widgets.addSlot(idx < input.size() ? input.get(idx) : EmiStack.EMPTY, 23 + x * (16 + 7), 29 + y * (16 + 7))
						.customBackground(texture, 23, 29, 16, 16);
			}
		for(AspectStack stack : aspects.asStacks())
			widgets.add(aspectWidget(stack));
		widgets.addSlot(output, 108, 52).customBackground(texture, 23, 29, 16, 16).output(true).recipeContext(this);
	}
	
	private Widget aspectWidget(AspectStack stack){
		int x = (int)aspectPositions.get(stack.type()).x;
		int y = (int)aspectPositions.get(stack.type()).y;
		if(stack.amount() == 0)
			return new SlotWidget(EmiStack.EMPTY, x - 1, y - 1).customBackground(texture, 129, 0, 18, 18);
		else
			return new SlotWidget(new AspectEmiStack(stack), x - 1, y - 1).customBackground(texture, 129, 0, 18, 18);
	}
	
	// from EmiShapedRecipe
	private static List<EmiIngredient> padIngredients(ShapedRecipe recipe){
		List<EmiIngredient> list = Lists.newArrayList();
		int i = 0;
		
		for(int y = 0; y < 3; y++)
			for(int x = 0; x < 3; x++)
				if(x < recipe.getWidth() && y < recipe.getHeight() && i < recipe.getIngredients().size())
					list.add(EmiIngredient.of(recipe.getIngredients().get(i++)));
				else
					list.add(EmiStack.EMPTY);
		
		return list;
	}
}