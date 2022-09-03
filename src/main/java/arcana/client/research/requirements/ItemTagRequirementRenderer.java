package arcana.client.research.requirements;

import arcana.ArcanaTags;
import arcana.client.research.RequirementRenderer;
import arcana.research.requirements.ItemTagRequirement;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class ItemTagRequirementRenderer implements RequirementRenderer<ItemTagRequirement>{
	
	public void render(MatrixStack matrices, int x, int y, ItemTagRequirement requirement, int time, float delta){
		List<Item> choices = ArcanaTags.itemsIn(requirement.getTag());
		ItemStack choice = new ItemStack(choices.get((time / 30) % choices.size()));
		
		client().getItemRenderer().renderGuiItemIcon(choice, x, y);
	}
	
	public List<Text> tooltip(ItemTagRequirement requirement, int time){
		List<Item> choices = ArcanaTags.itemsIn(requirement.getTag());
		ItemStack choice = new ItemStack(choices.get((time / 30) % choices.size()));
		
		var tooltips = choice.getTooltip(
				client().player,
				client().options.advancedItemTooltips ? TooltipContext.Default.ADVANCED : TooltipContext.Default.NORMAL
		);
		tooltips = new ArrayList<>(tooltips);
		if(requirement.getAmount() != 0)
			tooltips.set(0, Text.translatable("research.requirement.arcana.item", requirement.getAmount(), tooltips.get(0)));
		else
			tooltips.set(0, Text.translatable("research.requirement.arcana.item.have", tooltips.get(0)));
		tooltips.add(Text.translatable("research.requirement.arcana.tag", name(requirement.getTag())).formatted(Formatting.DARK_GRAY));
		return tooltips;
	}
	
	public boolean shouldDrawTickOrCross(ItemTagRequirement requirement, int amount){
		return amount == 0;
	}
	
	// thanks EMI
	private static Text name(TagKey<Item> tag){
		// if tag.$namespace.$path exists, use it
		var id = tag.id();
		String key = "tag." + id.getNamespace() + "." + id.getPath().replace("/", ".");
		if(I18n.hasTranslation(key))
			return Text.translatable(key);
		else
			return Text.literal("#" + id);
	}
}