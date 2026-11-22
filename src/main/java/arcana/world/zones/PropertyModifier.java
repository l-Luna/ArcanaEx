package arcana.world.zones;

import arcana.util.Ease;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record PropertyModifier<T>(T target, float maxEffect, Ease ease){
	
	public PropertyModifier(T target){
		this(target, 1, Ease.LINEAR);
	}
	
	public static <T> Codec<PropertyModifier<T>> createCodec(Codec<T> innerCodec){
		return Codec.withAlternative(createDirectCodec(innerCodec), innerCodec.xmap(PropertyModifier::new, it -> it.target));
	}
	
	public static <T> Codec<PropertyModifier<T>> createDirectCodec(Codec<T> innerCodec){
		return RecordCodecBuilder.create(i -> i.group(
				innerCodec.fieldOf("target").forGetter(PropertyModifier::target),
				Codec.floatRange(0, 1).optionalFieldOf("max_effect", 1f).forGetter(PropertyModifier::maxEffect),
				Ease.CODEC.optionalFieldOf("ease", Ease.LINEAR).forGetter(PropertyModifier::ease)
		).apply(i, PropertyModifier::new));
	}
	
	public float factor(float baseFactor){
		return maxEffect * ease.apply(baseFactor);
	}
}