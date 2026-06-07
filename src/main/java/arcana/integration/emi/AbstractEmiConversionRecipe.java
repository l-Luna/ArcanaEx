package arcana.integration.emi;

import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static arcana.Arcana.arcId;

public abstract class AbstractEmiConversionRecipe implements EmiRecipe{
	
	private final EmiIngredient item;
	private final List<EmiStack> outputs;
	private final Identifier baseId;
	
	public AbstractEmiConversionRecipe(EmiIngredient item, List<EmiStack> outputs, Identifier baseId){
		this.item = item;
		this.outputs = outputs;
		this.baseId = baseId;
	}
	
	protected abstract String typeId();
	
	protected EmiTexture arrowTexture(){
		return EmiTexture.EMPTY_ARROW;
	}
	
	public @Nullable Identifier getId(){
		return arcId("/" + typeId() + "/" + EmiUtil.subId(baseId));
	}
	
	public List<EmiIngredient> getInputs(){
		return List.of(item);
	}
	
	public List<EmiStack> getOutputs(){
		return outputs;
	}
	
	public int getDisplayWidth(){
		return 27 + 32 + 2 + Math.min(outputs.size(), 3) * 20;
	}
	
	public int getDisplayHeight(){
		return 20 + Math.max(0, (int)(Math.ceil(outputs.size() / 3f) - 1)) * 20;
	}
	
	public void addWidgets(WidgetHolder widgets){
		widgets.addSlot(item, 0, 1);
		widgets.addTexture(arrowTexture(), 27, 2);
		for(int i = 0; i < outputs.size(); i++)
			widgets.addSlot(outputs.get(i), 27 + 32 + (i % 3) * 20, 1 + (i / 3) * 20).recipeContext(this);
	}
}