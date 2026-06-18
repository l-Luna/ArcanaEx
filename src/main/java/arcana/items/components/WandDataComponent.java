package arcana.items.components;

import arcana.ArcanaRegistry;
import arcana.api.Cap;
import arcana.api.Core;
import arcana.aspects.AspectMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;

import java.util.function.Consumer;
import java.util.function.Function;

public class WandDataComponent{
	
	public static final Codec<WandDataComponent> CODEC = RecordCodecBuilder.create(i -> i.group(
			Cap.CODEC.lenientOptionalFieldOf("cap", ArcanaRegistry.MISSING_CAP).forGetter(x -> x.cap),
			Core.CODEC.lenientOptionalFieldOf("core", ArcanaRegistry.MISSING_CORE).forGetter(x -> x.core),
			ItemStack.OPTIONAL_CODEC.fieldOf("focus").forGetter(x -> x.focus),
			AspectMap.CODEC.fieldOf("stored").forGetter(x -> x.stored)
	).apply(i, WandDataComponent::new));
	
	public Cap cap;
	public Core core;
	public ItemStack focus;
	public AspectMap stored;
	
	public WandDataComponent(Cap cap, Core core, ItemStack focus, AspectMap stored){
		this.cap = cap;
		this.core = core;
		this.focus = focus;
		this.stored = stored;
	}
	
	public WandDataComponent copy(){
		return new WandDataComponent(cap, core, focus, stored);
	}
	
	public static WandDataComponent createDefault(){
		return new WandDataComponent(ArcanaRegistry.MISSING_CAP, ArcanaRegistry.MISSING_CORE, ItemStack.EMPTY, new AspectMap());
	}
	
	public static WandDataComponent withCapAndCore(Cap cap, Core core){
		return new WandDataComponent(cap, core, ItemStack.EMPTY, new AspectMap());
	}
	
	public static <T> T apply(ItemStack stack, Function<WandDataComponent, T> body){
		WandDataComponent copy = stack.getOrDefault(ArcanaItemComponentTypes.WAND_DATA, WandDataComponent.createDefault()).copy();
		T ret = body.apply(copy);
		stack.set(ArcanaItemComponentTypes.WAND_DATA, copy);
		return ret;
	}
	
	public static void run(ItemStack stack, Consumer<WandDataComponent> body){
		WandDataComponent.apply(stack, component -> {
			body.accept(component);
			return null;
		});
	}
}