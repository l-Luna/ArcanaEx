package arcana.items;

import arcana.aspects.Aspect;
import arcana.aspects.AspectMap;
import arcana.aspects.Aspects;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class CapItem extends Item implements Cap{
	
	private final CapProperties properties;
	
	public CapItem(Settings settings){
		this(settings, capProperties());
	}
	
	public CapItem(Settings settings, CapProperties capProperties){
		super(settings);
		properties = capProperties;
	}
	
	public Identifier id(){
		return Registry.ITEM.getId(this);
	}
	
	public int warping(){
		return properties.warping;
	}
	
	public int capacity(){
		return properties.capacity;
	}
	
	public int complexity(){
		return properties.complexity;
	}
	
	public int strength(){
		return properties.strBonus;
	}
	
	public int percentOff(Aspect aspect){
		return properties.discountPercents.get(aspect);
	}
	
	public static CapProperties capProperties(){
		return new CapProperties();
	}
	
	public static class CapProperties{
		private final AspectMap discountPercents = new AspectMap();
		private int capacity, complexity;
		private int warping = 0, strBonus = 0;
		
		public CapProperties capacity(int capacity){
			this.capacity = capacity;
			return this;
		}
		
		public CapProperties complexity(int complexity){
			this.complexity = complexity;
			return this;
		}
		
		public CapProperties warping(int warping){
			this.warping = warping;
			return this;
		}
		
		public CapProperties discountAll(int percentOff){
			for(Aspect primal : Aspects.primals)
				discountFor(primal, percentOff);
			return this;
		}
		
		public CapProperties discountFor(Aspect aspect, int percentOff){
			discountPercents.add(aspect, percentOff);
			return this;
		}
		
		public CapProperties strBonus(int strengthBonus){
			this.strBonus = strengthBonus;
			return this;
		}
	}
}