package arcana.items;

import arcana.aspects.Aspect;
import arcana.aspects.AspectMap;
import arcana.aspects.Aspects;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class CoreItem extends Item implements Core{
	
	private final CoreProperties properties;
	
	public CoreItem(Settings settings){
		this(settings, coreProperties());
	}
	
	public CoreItem(Settings settings, CoreProperties properties){
		super(settings);
		this.properties = properties;
	}
	
	public Identifier id(){
		return Registry.ITEM.getId(this);
	}
	
	public int capacity(){
		return properties.capacity;
	}
	
	public int strength(){
		return properties.strength;
	}
	
	public int warping(){
		return properties.warping;
	}
	
	public int complexity(){
		return properties.complexityBonus;
	}
	
	public int percentOff(Aspect aspect){
		return properties.discountPercents.get(aspect);
	}
	
	public static CoreProperties coreProperties(){
		return new CoreProperties();
	}
	
	public static class CoreProperties{
		private final AspectMap discountPercents = new AspectMap();
		private int capacity, strength;
		private int warping = 0, complexityBonus = 0;
		
		public CoreProperties capacity(int capacity){
			this.capacity = capacity;
			return this;
		}
		
		public CoreProperties strength(int strength){
			this.strength = strength;
			return this;
		}
		
		public CoreProperties discountAll(int percentOff){
			for(Aspect primal : Aspects.primals)
				discountFor(primal, percentOff);
			return this;
		}
		
		public CoreProperties discountFor(Aspect aspect, int percentOff){
			discountPercents.add(aspect, percentOff);
			return this;
		}
		
		public CoreProperties cmplxBonus(int complexityBonus){
			this.complexityBonus = complexityBonus;
			return this;
		}
		
		public CoreProperties warping(int warping){
			this.warping = warping;
			return this;
		}
	}
}