package arcana.items;

import arcana.aspects.Aspect;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class PhialItem extends Item{
	
	@Nullable("if empty")
	private final Aspect aspect;
	
	public PhialItem(Settings settings, @Nullable Aspect aspect){
		super(settings);
		this.aspect = aspect;
	}
	
	public @Nullable Aspect getAspect(){
		return aspect;
	}
	
	public Text getName(ItemStack stack){
		return getName();
	}
	
	public Text getName(){
		return aspect != null ? Text.translatable("item.arcana.phial", aspect.name()) : Text.translatable("item.arcana.empty_phial");
	}
}