package arcana.client.research.requirements;

import arcana.client.research.RequirementRenderer;
import arcana.research.requirements.ItemRequirement;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class ItemRequirementRenderer implements RequirementRenderer<ItemRequirement>{
	
	public void render(MatrixStack matrices, int x, int y, ItemRequirement requirement, int time, float delta){
		client().getItemRenderer().renderGuiItemIcon(new ItemStack(requirement.getItem()), x, y);
	}
	
	public List<Text> tooltip(ItemRequirement requirement, int time){
		var tooltips = new ItemStack(requirement.getItem()).getTooltip(
				client().player,
				client().options.advancedItemTooltips ? TooltipContext.Default.ADVANCED : TooltipContext.Default.NORMAL
		);
		tooltips = new ArrayList<>(tooltips);
		if(requirement.getAmount() != 0)
			tooltips.set(0, Text.translatable("research.requirement.arcana.item", requirement.getAmount(), tooltips.get(0)));
		else
			tooltips.set(0, Text.translatable("research.requirement.arcana.item.have", tooltips.get(0)));
		return tooltips;
	}
	
	public boolean shouldDrawTickOrCross(ItemRequirement requirement, int amount){
		return amount == 0;
	}
}