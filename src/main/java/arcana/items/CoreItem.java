package arcana.items;

import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class CoreItem extends Item implements Core{
	
	private final int warping;
	
	public CoreItem(Settings settings){
		this(settings, 0);
	}
	
	public CoreItem(Settings settings, int warping){
		super(settings);
		this.warping = warping;
	}
	
	public int warping(){
		return warping;
	}
	
	public Identifier id(){
		return Registry.ITEM.getId(this);
	}
}