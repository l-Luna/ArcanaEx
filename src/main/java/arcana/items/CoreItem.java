package arcana.items;

import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class CoreItem extends Item implements Core{
	
	public CoreItem(Settings settings){
		super(settings);
	}
	
	public Identifier id(){
		return Registry.ITEM.getId(this);
	}
}