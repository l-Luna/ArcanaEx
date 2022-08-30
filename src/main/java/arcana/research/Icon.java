package arcana.research;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public record Icon(ItemStack stack, Identifier texture){
	
	public Icon(Item item){
		this(new ItemStack(item), null);
	}
	
	public Icon(ItemStack stack){
		this(stack, null);
	}
	
	public Icon(Identifier texture){
		this(null, texture);
	}
	
	// for use by serialization; does NOT mirror the JSON representation
	
	public String asString(){
		return stack != null ? "item:" + Registry.ITEM.getId(stack.getItem()) :
		       texture != null ? "tex:" + texture :
		       "null";
	}
	
	public static Icon fromString(String string){
		if("null".equals(string))
			return new Icon(null, null);
		else if(string.startsWith("item:"))
			return new Icon(Registry.ITEM.get(new Identifier(string.substring(5))));
		else if(string.startsWith("tex:"))
			return new Icon(new Identifier(string.substring(4)));
		else throw new IllegalArgumentException("Illegal icon string: " + string);
	}
	
	public String toString(){
		return asString();
	}
}