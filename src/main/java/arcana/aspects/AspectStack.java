package arcana.aspects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record AspectStack(Aspect type, int amount){
	
	public static final Codec<AspectStack> CODEC = RecordCodecBuilder.create(
			i -> i.group(
					Aspect.CODEC.fieldOf("type").forGetter(AspectStack::type),
					Codec.INT.fieldOf("amount").forGetter(AspectStack::amount)
			).apply(i, AspectStack::new)
	);
}