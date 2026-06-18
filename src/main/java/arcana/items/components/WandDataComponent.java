package arcana.items.components;

import arcana.aspects.AspectMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import static arcana.Arcana.arcId;

public class WandDataComponent{
	
	public static final Codec<WandDataComponent> CODEC = RecordCodecBuilder.create(i -> i.group(
			Identifier.CODEC.fieldOf("cap").forGetter(x -> x.cap),
			Identifier.CODEC.fieldOf("core").forGetter(x -> x.core),
			ItemStack.OPTIONAL_CODEC.fieldOf("focus").forGetter(x -> x.focus),
			AspectMap.CODEC.fieldOf("stored").forGetter(x -> x.stored)
	).apply(i, WandDataComponent::new));
	
	public Identifier cap, core;
	public ItemStack focus;
	public AspectMap stored;
	
	public WandDataComponent(Identifier cap, Identifier core, ItemStack focus, AspectMap stored){
		this.cap = cap;
		this.core = core;
		this.focus = focus;
		this.stored = stored;
	}
	
	public static WandDataComponent createDefault(){
		return new WandDataComponent(arcId("undefined"), arcId("undefined"), ItemStack.EMPTY, new AspectMap());
	}
	
	public static WandDataComponent withCapAndCore(Identifier cap, Identifier core){
		return new WandDataComponent(cap, core, ItemStack.EMPTY, new AspectMap());
	}
}