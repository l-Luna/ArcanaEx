package arcana.items;

import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class CapItem extends Item implements Cap{
	
	public CapItem(Settings settings){
		super(settings);
	}
	
	public Identifier id(){
		return Registry.ITEM.getId(this);
	}
}