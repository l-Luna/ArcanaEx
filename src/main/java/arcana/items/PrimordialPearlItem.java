package arcana.items;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PrimordialPearlItem extends Item{
	
	public PrimordialPearlItem(Settings settings){
		super(settings);
	}
	
	@Environment(EnvType.CLIENT)
	public Text getName(){
		String[] nouns = I18n.translate("item.arcana.primordial_pearl.nouns").split(",");
		String noun = nouns[(int)((MinecraftClient.getInstance().world.getTime() / 2) % nouns.length)];
		return Text.translatable(getTranslationKey(), noun);
	}
	
	@Environment(EnvType.CLIENT)
	public Text getName(ItemStack stack){
		return getName();
	}
	
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context){
		super.appendTooltip(stack, world, tooltip, context);
		tooltip.add(Text.translatable("item.arcana.primordial_pearl.desc").setStyle(Style.EMPTY.withItalic(true)));
	}
}