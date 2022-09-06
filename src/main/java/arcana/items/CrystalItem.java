package arcana.items;

import arcana.aspects.Aspect;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class CrystalItem extends Item{
	
	private final Aspect aspect;
	
	public CrystalItem(Settings settings, Aspect aspect){
		super(settings);
		this.aspect = aspect;
	}
	
	public Text getName(ItemStack stack){
		return getName();
	}
	
	public Text getName(){
		return Text.translatable("item.arcana.aspect_crystal", aspect.name());
	}
}