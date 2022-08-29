package arcana.integration.emi;

import arcana.aspects.AspectMap;
import arcana.aspects.AspectStack;
import arcana.recipes.AlchemyRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static arcana.Arcana.arcId;

public class EmiAlchemyRecipe implements EmiRecipe{
	
	private static final Identifier texture = arcId("textures/gui/emi/alchemy.png");
	private static final EmiTexture background = new EmiTexture(texture, 0, 0, 94, 102);
	
	protected final Identifier id;
	protected final EmiIngredient input;
	protected final EmiStack output;
	protected final AspectMap aspects;
	
	public EmiAlchemyRecipe(AlchemyRecipe recipe){
		this(recipe.getId(), EmiIngredient.of(recipe.getIngredients().get(0)), recipe.getOutput().emi(), recipe.getAspects());
	}
	
	public EmiAlchemyRecipe(Identifier id, EmiIngredient input, EmiStack output, AspectMap aspects){
		this.id = id;
		this.input = input;
		this.output = output;
		this.aspects = aspects;
	}
	
	public EmiRecipeCategory getCategory(){
		return ArcanaEmiPlugin.ALCHEMY;
	}
	
	public @Nullable Identifier getId(){
		return id;
	}
	
	public List<EmiIngredient> getInputs(){
		return Stream.concat(Stream.of(input), aspects.asStacks().stream().map(AspectEmiStack::new)).toList();
	}
	
	public List<EmiStack> getOutputs(){
		return List.of(output);
	}
	
	public int getDisplayWidth(){
		return 94;
	}
	
	public int getDisplayHeight(){
		return 102;
	}
	
	public void addWidgets(WidgetHolder widgets){
		widgets.addTexture(background, 0, 0);
		widgets.addSlot(input, 5, 24).drawBack(false);
		widgets.addSlot(output, 49, 5).drawBack(false).recipeContext(this);
		positionAspects(aspects).forEach((stack, pos) ->
				widgets.addSlot(new AspectEmiStack(stack), pos.getLeft(), pos.getRight()).drawBack(false));
	}
	
	// will be used in/moved to research code, hence the indirection
	// TODO: cleanup
	public static Map<AspectStack, Pair<Integer, Integer>> positionAspects(AspectMap map){
		// vertically centre rows, horizontally centre within rows, sort by size
		var stacks = map.asStacks();
		Map<AspectStack, Pair<Integer, Integer>> ret = new HashMap<>();
		stacks.sort(Comparator.comparingInt(AspectStack::amount).reversed());
		int rows = (int)Math.ceil(stacks.size() / 3f);
		for(int row = 0; row < rows; row++){
			int aspectsOnRow = Math.min(3, stacks.size() - row * 3);
			int x = 36 + (48 - aspectsOnRow * 19) / 2;
			int y = 50 + (48 - rows * 19) / 2 + row * 19;
			for(int i = 0; i < aspectsOnRow; i++){
				var idx = row * 3 + i;
				if(idx < stacks.size())
					ret.put(stacks.get(idx), new Pair<>(x + i * 19, y));
			}
		}
		return ret;
	}
}