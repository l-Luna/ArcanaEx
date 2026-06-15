package arcana.client.research.requirements;

import arcana.client.research.RequirementRenderer;
import arcana.items.WandItem;
import arcana.research.requirements.ItemRequirement;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class ItemRequirementRenderer implements RequirementRenderer<ItemRequirement>{
	
	public void render(DrawContext ctx, int x, int y, ItemRequirement requirement, int time, float delta){
		var stack = new ItemStack(requirement.getItem());
		stack = requirement.getMatcher().preview(stack);
		if(requirement.getItem() instanceof WandItem)
			stack = WandItem.basicWand();
		ctx.drawItem(stack, x, y);
	}
	
	public List<Text> tooltip(ItemRequirement requirement, int time){
		ItemStack stack = new ItemStack(requirement.getItem());
		stack = requirement.getMatcher().preview(stack);
		if(requirement.getItem() instanceof WandItem)
			stack = WandItem.basicWand();
		List<Text> tooltips = stack.getTooltip(
				Item.TooltipContext.create(client().world),
				client().player,
				client().options.advancedItemTooltips ? TooltipType.Default.ADVANCED : TooltipType.Default.BASIC
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